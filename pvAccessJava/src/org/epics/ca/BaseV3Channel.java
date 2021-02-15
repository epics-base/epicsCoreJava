/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca;

import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Context;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.event.ConnectionEvent;
import gov.aps.jca.event.ConnectionListener;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.epics.pvaccess.client.AccessRights;
import org.epics.pvaccess.client.ChannelArray;
import org.epics.pvaccess.client.ChannelArrayRequester;
import org.epics.pvaccess.client.ChannelFind;
import org.epics.pvaccess.client.ChannelFindRequester;
import org.epics.pvaccess.client.ChannelGet;
import org.epics.pvaccess.client.ChannelGetRequester;
import org.epics.pvaccess.client.ChannelProcess;
import org.epics.pvaccess.client.ChannelProcessRequester;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelPut;
import org.epics.pvaccess.client.ChannelPutGet;
import org.epics.pvaccess.client.ChannelPutGetRequester;
import org.epics.pvaccess.client.ChannelPutRequester;
import org.epics.pvaccess.client.ChannelRPC;
import org.epics.pvaccess.client.ChannelRPCRequester;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.client.GetFieldRequester;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.monitor.Monitor;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StandardField;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.StatusCreate;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Type;

/**
 * Base class that implements V3Channel.
 * @author mrk
 *
 */
public class BaseV3Channel implements
ChannelFind,org.epics.pvaccess.client.Channel,
V3Channel,ConnectionListener
{
    private static final StandardField standardField = StandardFieldFactory.getStandardField();

    private static final StatusCreate statusCreate = StatusFactory.getStatusCreate();
    private static final Status okStatus = statusCreate.getStatusOK();
    private static final Status notSupportedStatus = statusCreate.createStatus(StatusType.ERROR, "not supported", null);
    private static final Status channelNotConnectedStatus = statusCreate.createStatus(StatusType.ERROR, "channel not connected", null);
    private static final Status subFieldDoesNotExistStatus = statusCreate.createStatus(StatusType.ERROR, "subField does not exist", null);

    private final ChannelProvider channelProvider;
    private final ChannelFindRequester channelFindRequester;
    private final ChannelRequester channelRequester;
    private final Context context;
    private final String channelName;

    private final AtomicBoolean gotFirstConnection = new AtomicBoolean(false);

    private final LinkedList<ChannelGet> channelGetList = new LinkedList<ChannelGet>();
    private final LinkedList<ChannelPut> channelPutList = new LinkedList<ChannelPut>();
    private final LinkedList<Monitor> monitorList =  new LinkedList<Monitor>();

    private volatile gov.aps.jca.Channel jcaChannel = null;
    private boolean isDestroyed = false;
    /**
     * The constructor.
     * @param channelProvider The channelProvider.
     * @param channelFindRequester The channelFind requester.
     * @param channelRequester The channel requester.
     * @param context The context.
     * @param channelName The channelName.
     */
    BaseV3Channel(
    		ChannelProvider channelProvider,
            ChannelFindRequester channelFindRequester,
            ChannelRequester channelRequester,
            Context context,
            String channelName)
    {
    	this.channelProvider = channelProvider;
        this.channelFindRequester = channelFindRequester;
        this.channelRequester = channelRequester;
        this.context = context;
        this.channelName = channelName;
    }

    // should be called only once
    public void connectCaV3() {
        try {
            jcaChannel = context.createChannel(channelName,this);
        } catch (CAException e) {
            if(channelFindRequester!=null)
            	channelFindRequester.channelFindResult(
            		statusCreate.createStatus(StatusType.FATAL, "failed to create channel", e),
            		this, false);
            else
            	channelRequester.channelCreated(channelNotConnectedStatus, null);
            jcaChannel = null;
        };
    }

    /* (non-Javadoc)
     * @see org.epics.ioc.channelAccess.ChannelFind#cancel()
     */
    public void cancel() {
        jcaChannel.dispose();
        jcaChannel = null;
    }

    /* (non-Javadoc)
     * @see org.epics.ioc.channelAccess.ChannelFind#getChannelProvider()
     */
    public ChannelProvider getChannelProvider() {
        return channelProvider;
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#getConnectionState()
     */
    public ConnectionState getConnectionState() {
        gov.aps.jca.Channel.ConnectionState connectionState = jcaChannel.getConnectionState();
        if(connectionState==gov.aps.jca.Channel.ConnectionState.DISCONNECTED) return ConnectionState.DISCONNECTED;
        else if(connectionState==gov.aps.jca.Channel.ConnectionState.CONNECTED) return ConnectionState.CONNECTED;
        else if(connectionState==gov.aps.jca.Channel.ConnectionState.NEVER_CONNECTED) return ConnectionState.NEVER_CONNECTED;
        else if(connectionState==gov.aps.jca.Channel.ConnectionState.CLOSED) return ConnectionState.DESTROYED;
        else throw new RuntimeException("unknown connection state");
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#getRemoteAddress()
     */
    public String getRemoteAddress() {
        return jcaChannel.getHostName();
    }

    /* (non-Javadoc)
     * @see org.epics.ioc.caV3.V3Channel#add(org.epics.pvaccess.client.ChannelGet)
     */
    public boolean add(ChannelGet channelGet)
    {
        boolean result = false;
        synchronized(channelGetList) {
            result = channelGetList.add(channelGet);
        }
        return result;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.caV3.V3Channel#add(org.epics.pvaccess.client.ChannelPut)
     */
    public boolean add(ChannelPut channelPut)
    {
        boolean result = false;
        synchronized(channelPutList) {
            result = channelPutList.add(channelPut);
        }
        return result;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.caV3.V3Channel#add(org.epics.pvdata.monitor.Monitor)
     */
    public boolean add(Monitor monitor)
    {
        boolean result = false;
        synchronized(monitorList) {
            result = monitorList.add(monitor);
        }
        return result;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.caV3.V3Channel#remove(org.epics.pvaccess.client.ChannelGet)
     */
    public boolean remove(ChannelGet channelGet) {
        boolean result = false;
        synchronized(channelGetList) {
            result = channelGetList.remove(channelGet);
        }
        return result;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.caV3.V3Channel#remove(org.epics.pvaccess.client.ChannelPut)
     */
    public boolean remove(ChannelPut channelPut) {
        boolean result = false;
        synchronized(channelPutList) {
            result = channelPutList.remove(channelPut);
        }
        return result;
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.caV3.V3Channel#remove(org.epics.pvdata.monitor.Monitor)
     */
    public boolean remove(Monitor monitor) {
        boolean result = false;
        synchronized(monitorList) {
            result = monitorList.remove(monitor);
        }
        return result;
    }
    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#createChannelArray(org.epics.pvaccess.client.ChannelArrayRequester, java.lang.String, org.epics.pvdata.pv.PVStructure)
     */
    public ChannelArray createChannelArray(
            ChannelArrayRequester channelArrayRequester, PVStructure pvRequest)
    {
        channelArrayRequester.channelArrayConnect(notSupportedStatus, null, null);
        return null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#createChannelGet(org.epics.pvaccess.client.ChannelGetRequester, org.epics.pvdata.pv.PVStructure, boolean, boolean, org.epics.pvdata.pv.PVStructure)
     */
    public ChannelGet createChannelGet(ChannelGetRequester channelGetRequester,
            PVStructure pvRequest)
    {
        return new BaseV3ChannelGet(channelGetRequester, this, pvRequest);
    }
    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#createMonitor(org.epics.pvdata.monitor.MonitorRequester, org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.PVStructure)
     */
    public Monitor createMonitor(
            MonitorRequester monitorRequester,
            PVStructure pvRequest)
    {
        return new BaseV3Monitor(monitorRequester, this, pvRequest);
    }
    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#createChannelProcess(org.epics.pvaccess.client.ChannelProcessRequester, org.epics.pvdata.pv.PVStructure)
     */
    public ChannelProcess createChannelProcess(
            ChannelProcessRequester channelProcessRequester,
            PVStructure pvRequest)
    {
        channelProcessRequester.channelProcessConnect(notSupportedStatus,null);
        return null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#createChannelPut(org.epics.pvaccess.client.ChannelPutRequester, org.epics.pvdata.pv.PVStructure, boolean, boolean, org.epics.pvdata.pv.PVStructure)
     */
    public ChannelPut createChannelPut(ChannelPutRequester channelPutRequester,
            PVStructure pvRequest)
    {
        return new BaseV3ChannelPut(channelPutRequester, this, pvRequest);
    }
    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#createChannelPutGet(org.epics.pvaccess.client.ChannelPutGetRequester, org.epics.pvdata.pv.PVStructure, boolean, org.epics.pvdata.pv.PVStructure, boolean, boolean, org.epics.pvdata.pv.PVStructure)
     */
    public ChannelPutGet createChannelPutGet(
            ChannelPutGetRequester channelPutGetRequester,
            PVStructure pvRequest)
    {
        channelPutGetRequester.channelPutGetConnect(notSupportedStatus, null, null, null);
        return null;
    }
	public ChannelRPC createChannelRPC(ChannelRPCRequester channelRPCRequester,
			PVStructure pvRequest)
    {
    	channelRPCRequester.channelRPCConnect(notSupportedStatus,null);
		return null;
	}

	/* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#destroy()
     */
    public void destroy() {
        synchronized(this) {
            if(isDestroyed) return;
            isDestroyed = true;
        }
        while(!channelGetList.isEmpty()) {
            ChannelGet channelGet = channelGetList.getFirst();
            channelGet.destroy();
        }
        while(!channelPutList.isEmpty()) {
            ChannelPut channelPut = channelPutList.getFirst();
            channelPut.destroy();
        }
        while(!monitorList.isEmpty()) {
            Monitor monitor = monitorList.getFirst();
            monitor.destroy();
        }
        try {
            jcaChannel.destroy();
        } catch (CAException e) {
            if(channelRequester!=null) channelRequester.message("destroy caused CAException " + e.getMessage(), MessageType.error);
        }
        jcaChannel = null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#getAccessRights(org.epics.pvdata.pv.PVField)
     */
    public AccessRights getAccessRights(PVField pvField) {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#getChannelName()
     */
    public String getChannelName() {
        return channelName;
    }
    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#getChannelRequester()
     */
    public ChannelRequester getChannelRequester() {
        return channelRequester;
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#getField(org.epics.pvaccess.client.GetFieldRequester, java.lang.String)
     */
    public void getField(GetFieldRequester requester, String subField) {
        if(subField==null || subField.length()==0) subField = "value";
        else if(!subField.equals("value")) {
            requester.getDone(subFieldDoesNotExistStatus, null);
            return;
        }
        DBRType nativeDBRType = jcaChannel.getFieldType();
        boolean extraProperties = true;
        Type valueType = null;
        ScalarType valueScalarType = null;
        if(nativeDBRType==DBRType.ENUM) {
            valueType = Type.structure;
            extraProperties = false;
        } else if(nativeDBRType==DBRType.STRING) {
            valueScalarType = ScalarType.pvString;
            extraProperties = false;
        } else if(nativeDBRType==DBRType.BYTE) {
            valueScalarType = ScalarType.pvByte;
        } else if(nativeDBRType==DBRType.SHORT) {
            valueScalarType = ScalarType.pvShort;
        } else if(nativeDBRType==DBRType.INT) {
            valueScalarType = ScalarType.pvInt;
        } else if(nativeDBRType==DBRType.FLOAT) {
            valueScalarType = ScalarType.pvFloat;
        } else if(nativeDBRType==DBRType.DOUBLE) {
            valueScalarType = ScalarType.pvDouble;
        }
        if(valueType==null) {
            if(jcaChannel.getElementCount()>1) {
                valueType = Type.scalarArray;
            } else {
                valueType = Type.scalar;
            }
        }
        String properties = "timeStamp,alarm";
        if(extraProperties) properties += ",display,control,valueAlarm";
        Structure structure =  null;
        switch(valueType) {
        case scalar:
            structure = standardField.scalar(valueScalarType,properties);
            break;
        case scalarArray:
            structure = standardField.scalarArray(valueScalarType,properties);
            break;
        case structure:
            structure = standardField.enumerated(properties);
        }

        requester.getDone(okStatus,structure);
    }
    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#getProvider()
     */
    public ChannelProvider getProvider() {
        return channelProvider;
    }
    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#isConnected()
     */
    public boolean isConnected() {
    	Channel ch = jcaChannel;
    	if (ch != null)
    		return (ch.getConnectionState() == Channel.ConnectionState.CONNECTED);
    	else
    		return false;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Requester#getRequesterName()
     */
    public String getRequesterName() {
        return channelRequester.getRequesterName();
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Requester#message(java.lang.String, org.epics.pvdata.pv.MessageType)
     */
    public void message(String message, MessageType messageType) {
        channelRequester.message(message, messageType);
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.caV3.V3Channel#getJcaChannel()
     */
    public Channel getJCAChannel() {
        return jcaChannel;
    }
    /* (non-Javadoc)
     * @see gov.aps.jca.event.ConnectionListener#connectionChanged(gov.aps.jca.event.ConnectionEvent)
     */
    public void connectionChanged(ConnectionEvent arg0) {
        boolean isConnected = arg0.isConnected();
        if(isConnected) {
            if(gotFirstConnection.getAndSet(true)) {
                channelRequester.channelStateChange(this, ConnectionState.CONNECTED);
                return;
            }
            else {
                if(channelFindRequester!=null) {
                    channelFindRequester.channelFindResult(okStatus, this, true);
                    destroy();
                    return;
                }
                channelRequester.channelCreated(okStatus, this);
                channelRequester.channelStateChange(this, ConnectionState.CONNECTED);
            }
        } else {
            channelRequester.channelStateChange(this, ConnectionState.DISCONNECTED);
        }
    }
}
