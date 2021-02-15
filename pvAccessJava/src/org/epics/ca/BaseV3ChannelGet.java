/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca;

import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.event.ConnectionEvent;
import gov.aps.jca.event.ConnectionListener;
import gov.aps.jca.event.GetEvent;
import gov.aps.jca.event.GetListener;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.epics.pvaccess.client.ChannelGet;
import org.epics.pvaccess.client.ChannelGetRequester;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.StatusCreate;



/**
 * Base class that implements ChannelGet for communicating with a V3 IOC.
 * @author mrk
 *
 */


public class BaseV3ChannelGet
implements ChannelGet,GetListener,ConnectionListener
{
    private static final StatusCreate statusCreate = StatusFactory.getStatusCreate();
    private static final Status okStatus = statusCreate.getStatusOK();
    private static final Status channelDestroyedStatus = statusCreate.createStatus(StatusType.ERROR, "channel destroyed", null);
    private static final Status channelNotConnectedStatus = statusCreate.createStatus(StatusType.ERROR, "channel not connected", null);
    private static final Status disconnectedWhileActiveStatus = statusCreate.createStatus(StatusType.ERROR, "disconnected while active", null);
    private static final Status createChannelStructureStatus = statusCreate.createStatus(StatusType.ERROR, "createChannelStructure failed", null);

    private final ChannelGetRequester channelGetRequester;

    private final V3Channel v3Channel;
    private final V3ChannelStructure v3ChannelStructure;
    private final gov.aps.jca.Channel jcaChannel;

    private volatile boolean isDestroyed = false;
    private volatile boolean lastRequest = false;

    private final ReentrantLock lock = new ReentrantLock();

    private final AtomicBoolean isActive = new AtomicBoolean(false);

    private final PVStructure pvRequest;
    /**
     * Constructor.
     * @param channelGetRequester The channelGetRequester.
     * @param v3Channel The V3Channel
     * @param pvRequest The request structure.
     */
    public BaseV3ChannelGet(ChannelGetRequester channelGetRequester, V3Channel v3Channel,PVStructure pvRequest)
    {
        this.channelGetRequester = channelGetRequester;
        this.v3Channel = v3Channel;
        this.pvRequest = pvRequest;
        v3Channel.add(this);
        v3ChannelStructure = new BaseV3ChannelStructure(v3Channel);

    	this.jcaChannel = v3Channel.getJCAChannel();

    	try {
			jcaChannel.addConnectionListener(this);
		} catch (Throwable th) {
            channelGetRequester.channelGetConnect(statusCreate.createStatus(StatusType.ERROR, "addConnectionListener failed", th), this, null);
            destroy();
            return;
		}

		// there is a possible run condition, but it's OK
		if (jcaChannel.getConnectionState() == Channel.CONNECTED)
			connectionChanged(new ConnectionEvent(jcaChannel, true));
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.ca.ChannelGet#destroy()
     */
    public void destroy() {
        isDestroyed = true;
        v3Channel.remove(this);
        try {
			jcaChannel.removeConnectionListener(this);
		} catch (Throwable th) {
			// noop
		}
    }
    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.ChannelGet#get()
     */
    public void get() {
        if(isDestroyed) {
            getDone(channelDestroyedStatus);
            return;
        }
        gov.aps.jca.Channel jcaChannel = v3Channel.getJCAChannel();
        if(jcaChannel.getConnectionState()!=Channel.ConnectionState.CONNECTED) {
            getDone(channelNotConnectedStatus);
            return;
        }
        isActive.set(true);
        try {
        	// we use count == 0, to get actual (not maximum) number of elements
            jcaChannel.get(v3ChannelStructure.getRequestDBRType(),0,this);
        } catch (Throwable th) {
            getDone(statusCreate.createStatus(StatusType.ERROR, "failed to get", th));
        }
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.util.Requester#getRequesterName()
     */
    public String getRequesterName() {
        return v3Channel.getRequesterName();
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.util.Requester#message(java.lang.String, org.epics.ioc.util.MessageType)
     */
    public void message(String message, MessageType messageType) {
        v3Channel.message(message, messageType);
    }
    /* (non-Javadoc)
     * @see gov.aps.jca.event.GetListener#getCompleted(gov.aps.jca.event.GetEvent)
     */
    public void getCompleted(GetEvent getEvent) {
        DBR fromDBR = getEvent.getDBR();
        if(fromDBR==null) {
            CAStatus caStatus = getEvent.getStatus();
            getDone(statusCreate.createStatus(StatusType.ERROR, caStatus.toString(), null));
            return;
        }
        lock();
        try {
        	v3ChannelStructure.toStructure(fromDBR);
        } finally {
        	unlock();
        }
        getDone(okStatus);
    }
    /* (non-Javadoc)
     * @see gov.aps.jca.event.ConnectionListener#connectionChanged(gov.aps.jca.event.ConnectionEvent)
     */
    public void connectionChanged(ConnectionEvent event) {
        if(!event.isConnected()) {
    		getDone(disconnectedWhileActiveStatus);
        }
        else
        {
            if(v3ChannelStructure.createPVStructure(pvRequest,true)==null) {
                channelGetRequester.channelGetConnect(createChannelStructureStatus,this,null);
                destroy();
            } else {
                channelGetRequester.channelGetConnect(okStatus, this, v3ChannelStructure.getPVStructure().getStructure());
            }
        }
    }

    private void getDone(Status success) {
        if(!isActive.getAndSet(false)) return;
        if (lastRequest) destroy();
        channelGetRequester.getDone(success, this, v3ChannelStructure.getPVStructure(), v3ChannelStructure.getBitSet());
    }

	public void lock() {
		lock.lock();
	}

	public void unlock() {
		lock.unlock();
	}

	public void cancel() {
		// noop, not supported
	}

	public void lastRequest() {
		lastRequest = true;
	}

	public org.epics.pvaccess.client.Channel getChannel() {
		return v3Channel;
	}

}
