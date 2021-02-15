/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca;


import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.event.ConnectionEvent;
import gov.aps.jca.event.ConnectionListener;
import gov.aps.jca.event.GetEvent;
import gov.aps.jca.event.GetListener;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.monitor.MonitorElement;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.StatusCreate;


/**
 * Base class that implements ChannelMonitor for communicating with a V3 IOC.
 * @author mrk
 *
 */
public class BaseV3Monitor implements org.epics.pvdata.monitor.Monitor,MonitorListener,GetListener,ConnectionListener
{
    private static final StatusCreate statusCreate = StatusFactory.getStatusCreate();
    private static final Status okStatus = statusCreate.getStatusOK();
    private static final Status channelDestroyedStatus = statusCreate.createStatus(StatusType.ERROR, "channel destroyed", null);
    private static final Status createChannelStructureStatus = statusCreate.createStatus(StatusType.ERROR, "createChannelStructure failed", null);
    private static final Status getInitialStatus = statusCreate.createStatus(StatusType.ERROR, "get initial failed", null);

    private final MonitorRequester monitorRequester;

    private final V3Channel v3Channel;
    private final gov.aps.jca.Channel jcaChannel;
    private final V3ChannelStructure v3ChannelStructure;

    private volatile Monitor monitor = null;
    private volatile boolean isDestroyed = false;

    private volatile BitSet overrunBitSet;
    private volatile MonitorElement monitorElement;

    private final PVStructure pvRequest;

    /**
     * Constructor.
     * @param monitorRequester The monitorRequester.
     * @param v3Channel The V3Channel
     * @param pvRequest The request structure.
     */
    public BaseV3Monitor(MonitorRequester monitorRequester,V3Channel v3Channel,PVStructure pvRequest) {
        this.monitorRequester = monitorRequester;
        this.v3Channel = v3Channel;
        this.pvRequest = pvRequest;
        v3Channel.add(this);
        v3ChannelStructure = new BaseV3ChannelStructure(v3Channel);

        jcaChannel = v3Channel.getJCAChannel();
        try {
            jcaChannel.addConnectionListener(this);
        } catch (CAException e) {
        	overrunBitSet = null; monitorElement = null;
            monitorRequester.monitorConnect(statusCreate.createStatus(StatusType.ERROR, "addConnectionListener failed", e), null,null);
            destroy();
            return;
        };

		// there is a possible run condition, but it's OK
		if (jcaChannel.getConnectionState() == Channel.CONNECTED)
			connectionChanged(new ConnectionEvent(jcaChannel, true));

    }

    protected void initializeMonitor()
    {
        if(v3ChannelStructure.createPVStructure(pvRequest,true)==null) {
        	overrunBitSet = null; monitorElement = null;
            monitorRequester.monitorConnect(createChannelStructureStatus,null,null);
            destroy();
            return;
        }
        try {
        	// we use count == 0, to get actual (not maximum) number of elements
            jcaChannel.get(v3ChannelStructure.getRequestDBRType(),0,this);
        } catch (Throwable th) {
        	overrunBitSet = null; monitorElement = null;
            monitorRequester.monitorConnect(getInitialStatus,null,null);
            destroy();
            return;
        }
        PVStructure pvStructure = v3ChannelStructure.getPVStructure();
        overrunBitSet = new BitSet(pvStructure.getNumberFields());
        monitorElement = new MonitorElementImpl(pvStructure,v3ChannelStructure.getBitSet(),overrunBitSet);
    }

    /* (non-Javadoc)
     * @see org.epics.ioc.ca.ChannelMonitor#destroy()
     */
    public void destroy() {
        if(monitor!=null) stop();
        isDestroyed = true;
        v3Channel.remove(this);
        try {
			jcaChannel.removeConnectionListener(this);
		} catch (Throwable th) {
			// noop
		}
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.ca.ChannelMonitor#start()
     */
    public Status start() {
        if(isDestroyed) return channelDestroyedStatus;
        try {
            monitor = jcaChannel.addMonitor(v3ChannelStructure.getRequestDBRType(), jcaChannel.getElementCount(), 0x0ff, this);
        } catch (CAException e) {
        	return statusCreate.createStatus(StatusType.ERROR, "failed to start monitor", e);
        }
        return okStatus;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.ca.ChannelMonitor#stop()
     */
    public Status stop() {
        if(isDestroyed) return channelDestroyedStatus;
        try {
            monitor.clear();
        } catch (CAException e) {
        	return statusCreate.createStatus(StatusType.ERROR, "failed to stop monitor", e);
        }
        monitor = null;
        return okStatus;
    }
    /* (non-Javadoc)
     * @see gov.aps.jca.event.MonitorListener#monitorChanged(gov.aps.jca.event.MonitorEvent)
     */
    public void monitorChanged(MonitorEvent monitorEvent) {
        CAStatus caStatus = monitorEvent.getStatus();
        if(!caStatus.isSuccessful()) {
            monitorRequester.message(caStatus.getMessage(),MessageType.error);
            return;
        }
        DBR fromDBR = monitorEvent.getDBR();
        if(fromDBR==null) {
            monitorRequester.message("fromDBR is null", MessageType.error);
        } else {
            v3ChannelStructure.toStructure(fromDBR);
            monitorRequester.monitorEvent(this);
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.monitor.Monitor#poll()
     */
    public MonitorElement poll() {
        if(v3ChannelStructure.getBitSet().nextSetBit(0)<0) return null;
        return monitorElement;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.monitor.Monitor#release(org.epics.pvdata.monitor.MonitorElement)
     */
    public void release(MonitorElement monitorElement) {
        v3ChannelStructure.getBitSet().clear();
    }
    /* (non-Javadoc)
     * @see gov.aps.jca.event.GetListener#getCompleted(gov.aps.jca.event.GetEvent)
     */
    public void getCompleted(GetEvent getEvent) {
        DBR fromDBR = getEvent.getDBR();
        if(fromDBR==null) {
            CAStatus caStatus = getEvent.getStatus();
            monitorRequester.monitorConnect(
                    statusCreate.createStatus(StatusType.ERROR, caStatus.getMessage(),null),null,null);
        } else {
            v3ChannelStructure.toStructure(fromDBR);
            monitorRequester.monitorConnect(okStatus, this, v3ChannelStructure.getPVStructure().getStructure());
            monitorRequester.monitorEvent(this);
        }
    }
    /* (non-Javadoc)
     * @see gov.aps.jca.event.ConnectionListener#connectionChanged(gov.aps.jca.event.ConnectionEvent)
     */
    public void connectionChanged(ConnectionEvent event) {
    	if (event.isConnected())
    		initializeMonitor();
    }

    private static class MonitorElementImpl implements MonitorElement {
        private final PVStructure pvStructure;
        private final BitSet changedBitSet;
        private final BitSet overrunBitSet;

        MonitorElementImpl(PVStructure pvStructure,BitSet changedBitSet, BitSet overrunBitSet) {
            this.pvStructure = pvStructure;
            this.changedBitSet = changedBitSet;
            this.overrunBitSet = overrunBitSet;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.monitor.MonitorElement#getChangedBitSet()
         */
        public BitSet getChangedBitSet() {
            return changedBitSet;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.monitor.MonitorElement#getOverrunBitSet()
         */
        public BitSet getOverrunBitSet() {
            return overrunBitSet;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.monitor.MonitorElement#getPVStructure()
         */
        public PVStructure getPVStructure() {
            return pvStructure;
        }

    }
}
