/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.caV3;


import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.event.ConnectionEvent;
import gov.aps.jca.event.ConnectionListener;
import gov.aps.jca.event.GetEvent;
import gov.aps.jca.event.GetListener;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import org.epics.pvData.factory.StatusFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.monitor.MonitorElement;
import org.epics.pvData.monitor.MonitorRequester;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.Status.StatusType;
import org.epics.pvData.pv.StatusCreate;


/**
 * Base class that implements ChannelMonitor for communicating with a V3 IOC.
 * @author mrk
 *
 */
public class BaseV3Monitor implements org.epics.pvData.monitor.Monitor,MonitorListener,GetListener,ConnectionListener
{
    private static final StatusCreate statusCreate = StatusFactory.getStatusCreate();
    private static final Status okStatus = statusCreate.getStatusOK();
    private static Status channelDestroyedStatus = statusCreate.createStatus(StatusType.ERROR, "channel destroyed", null);
    private static Status createChannelStructureStatus = statusCreate.createStatus(StatusType.ERROR, "createChannelStructure failed", null);
    private static Status getInitialStatus = statusCreate.createStatus(StatusType.ERROR, "get initial failed", null);

    private MonitorRequester monitorRequester;
    
    private V3Channel v3Channel = null;
    private gov.aps.jca.Channel jcaChannel = null;
    private V3ChannelStructure v3ChannelStructure = null;
   
    private Monitor monitor = null;
    private boolean isDestroyed = false;
    
    private BitSet overrunBitSet = null;
    private MonitorElement monitorElement = null;
    /**
     * Constructor.
     * @param monitorRequester The monitorRequester.
     */
    public BaseV3Monitor(MonitorRequester monitorRequester) {
        this.monitorRequester = monitorRequester;
    }
    /**
     * Initialize the channelMonitor.
     * @param v3Channel The V3Channel
     * @param pvRequest The request structure.
     */
    public void init(V3Channel v3Channel,PVStructure pvRequest)
    {
        this.v3Channel = v3Channel;
        v3Channel.add(this);
        v3ChannelStructure = new BaseV3ChannelStructure(v3Channel);
        if(v3ChannelStructure.createPVStructure(pvRequest,true)==null) {
            monitorRequester.monitorConnect(createChannelStructureStatus,null,null);
            destroy();
        }
        jcaChannel = v3Channel.getJCAChannel();
        try {
            jcaChannel.addConnectionListener(this);
        } catch (CAException e) {
            monitorRequester.monitorConnect(statusCreate.createStatus(StatusType.ERROR, "addConnectionListener failed", e), null,null);
            jcaChannel = null;
            return;
        };
        try {
            jcaChannel.get(v3ChannelStructure.getRequestDBRType(),jcaChannel.getElementCount(), this);
        } catch (Throwable th) {
            monitorRequester.monitorConnect(getInitialStatus,null,null);
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
     * @see org.epics.pvData.monitor.Monitor#poll()
     */
    @Override
    public MonitorElement poll() {
        if(v3ChannelStructure.getBitSet().nextSetBit(0)<0) return null;
        return monitorElement;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.monitor.Monitor#release(org.epics.pvData.monitor.MonitorElement)
     */
    @Override
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
    public void connectionChanged(ConnectionEvent arg0) {
        if(!arg0.isConnected()) {
            if(monitor!=null) stop();
        }
    }
    
    private static class MonitorElementImpl implements MonitorElement {
        private PVStructure pvStructure;
        private BitSet changedBitSet;
        private BitSet overrunBitSet;
        

        MonitorElementImpl(PVStructure pvStructure,BitSet changedBitSet, BitSet overrunBitSet) {
            super();
            this.pvStructure = pvStructure;
            this.changedBitSet = changedBitSet;
            this.overrunBitSet = overrunBitSet;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.monitor.MonitorElement#getChangedBitSet()
         */
        @Override
        public BitSet getChangedBitSet() {
            return changedBitSet;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.monitor.MonitorElement#getOverrunBitSet()
         */
        @Override
        public BitSet getOverrunBitSet() {
            return overrunBitSet;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.monitor.MonitorElement#getPVStructure()
         */
        @Override
        public PVStructure getPVStructure() {
            return pvStructure;
        }
        
    }
}