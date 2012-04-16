/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.caV3;

import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.event.ConnectionEvent;
import gov.aps.jca.event.ConnectionListener;
import gov.aps.jca.event.GetEvent;
import gov.aps.jca.event.GetListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.epics.ca.client.ChannelPut;
import org.epics.ca.client.ChannelPutRequester;
import org.epics.pvData.factory.StatusFactory;
import org.epics.pvData.pv.ByteArrayData;
import org.epics.pvData.pv.DoubleArrayData;
import org.epics.pvData.pv.FloatArrayData;
import org.epics.pvData.pv.IntArrayData;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVByte;
import org.epics.pvData.pv.PVByteArray;
import org.epics.pvData.pv.PVDouble;
import org.epics.pvData.pv.PVDoubleArray;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVFloat;
import org.epics.pvData.pv.PVFloatArray;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVIntArray;
import org.epics.pvData.pv.PVShort;
import org.epics.pvData.pv.PVShortArray;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStringArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.ShortArrayData;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.Status.StatusType;
import org.epics.pvData.pv.StatusCreate;
import org.epics.pvData.pv.StringArrayData;

/**
 * Base class that implements ChannelPut for communicating with a V3 IOC.
 * @author mrk
 *
 */
public class BaseV3ChannelPut
implements ChannelPut,GetListener,PutListener,ConnectionListener
{
    private static final StatusCreate statusCreate = StatusFactory.getStatusCreate();
    private static final Status okStatus = statusCreate.getStatusOK();
    private static Status channelDestroyedStatus = statusCreate.createStatus(StatusType.ERROR, "channel destroyed", null);
    private static Status channelNotConnectedStatus = statusCreate.createStatus(StatusType.ERROR, "channel not connected", null);
    private static Status disconnectedWhileActiveStatus = statusCreate.createStatus(StatusType.ERROR, "disconnected while active", null);
    private static Status createChannelStructureStatus = statusCreate.createStatus(StatusType.ERROR, "createChannelStructure failed", null);

    private V3Channel v3Channel = null;
    private gov.aps.jca.Channel jcaChannel = null;
    private int elementCount = 1;
    private final ChannelPutRequester channelPutRequester;
    private V3ChannelStructure v3ChannelStructure = null;

    private final ReentrantLock lock = new ReentrantLock();

    private volatile boolean isDestroyed = false;
    private PVField pvField = null;
    private PVInt pvIndex = null; // only if nativeDBRType.isENUM()
    private ByteArrayData byteArrayData = new ByteArrayData();
    private ShortArrayData shortArrayData = new ShortArrayData();
    private IntArrayData intArrayData = new IntArrayData();
    private FloatArrayData floatArrayData = new FloatArrayData();
    private DoubleArrayData doubleArrayData = new DoubleArrayData();
    private StringArrayData stringArrayData = new StringArrayData();
    
    private AtomicBoolean isActive = new AtomicBoolean(false);

    /**
     * Constructor.
     * @param channelPutRequester The channelPutRequester.
     */
    public BaseV3ChannelPut(ChannelPutRequester channelPutRequester)
    {
        this.channelPutRequester = channelPutRequester;
    }
    /**
     * Initialize the channelPut.
     * @param v3Channel The V3Channel
     */
    public void init(V3Channel v3Channel,PVStructure pvRequest)
    {
        this.v3Channel = v3Channel;
        v3Channel.add(this);
        v3ChannelStructure = new BaseV3ChannelStructure(v3Channel);
        if(v3ChannelStructure.createPVStructure(pvRequest,true)==null) {
            channelPutRequester.channelPutConnect(createChannelStructureStatus,null,null,null);
        }
        DBRType nativeDBRType = v3ChannelStructure.getNativeDBRType();
        jcaChannel = v3Channel.getJCAChannel();
        try {
            jcaChannel.addConnectionListener(this);
        } catch (CAException e) {
            channelPutRequester.channelPutConnect(statusCreate.createStatus(StatusType.ERROR, "addConnectionListener failed", e),null,null,null);
            jcaChannel = null;
            return;
        }
        PVStructure pvStructure = v3ChannelStructure.getPVStructure();
        pvField = pvStructure.getSubField("value");
        elementCount = jcaChannel.getElementCount();
        if(nativeDBRType.isENUM()) {
            if(elementCount!=1) {
                channelPutRequester.channelPutConnect(statusCreate.createStatus(StatusType.ERROR, "array of ENUM not supported", null),null,null,null);
                return;
            }
            PVStructure pvStruct = (PVStructure)pvField;
            pvIndex = pvStruct.getIntField("index");
        }
        channelPutRequester.channelPutConnect(okStatus,this,
            v3ChannelStructure.getPVStructure(),v3ChannelStructure.getBitSet());
    } 
    /* (non-Javadoc)
     * @see org.epics.ioc.ca.ChannelPut#destroy()
     */
    public void destroy() {
        isDestroyed = true;
        v3Channel.remove(this);
    }
    
    /* (non-Javadoc)
     * @see org.epics.ca.client.ChannelPut#get()
     */
    @Override
    public void get() {
        if(isDestroyed) {
            getDone(channelDestroyedStatus);
            return;
        }
        if(jcaChannel.getConnectionState()!=Channel.ConnectionState.CONNECTED) {
            getDone(channelNotConnectedStatus);
            return;
        }
        
        try {
            jcaChannel.get(v3ChannelStructure.getRequestDBRType(), elementCount, this);
        } catch (Throwable th) {
            getDone(statusCreate.createStatus(StatusType.ERROR, "failed to get", th));
        }
    }
    /* (non-Javadoc)
     * @see gov.aps.jca.event.GetListener#getCompleted(gov.aps.jca.event.GetEvent)
     */
    @Override
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
    private void getDone(Status success) {
        channelPutRequester.getDone(success);
    }
    /* (non-Javadoc)
     * @see org.epics.ca.client.ChannelPut#put(boolean)
     */
    @Override
    public void put(boolean lastRequest) {
        if(isDestroyed) {
    		putDone(channelDestroyedStatus);
            return;
        }
        if(jcaChannel.getConnectionState()!=Channel.ConnectionState.CONNECTED) {
    		putDone(channelNotConnectedStatus);
        	return;
        };
        DBRType nativeDBRType = v3ChannelStructure.getNativeDBRType();
        isActive.set(true);
        try {
            if(pvIndex!=null) {
                short index = (short)pvIndex.get();
                jcaChannel.put(index, this);
            } else if(nativeDBRType==DBRType.BYTE) {
                if(elementCount==1) {
                    PVByte pvFrom = (PVByte)pvField;
                    byte from = pvFrom.get();
                    jcaChannel.put(from, this);
                } else {
                    PVByteArray fromArray =(PVByteArray)pvField;
                    int len = fromArray.get(0, elementCount, byteArrayData);
                    byte[] from = byteArrayData.data;
                    int capacity = fromArray.getCapacity();
                    for (int i=len; i<capacity; i++) from[i] = 0;
                    jcaChannel.put(from, this);
                }
            } else if(nativeDBRType==DBRType.SHORT) {
                if(elementCount==1) {
                    PVShort pvFrom = (PVShort)pvField;
                    short from = pvFrom.get();
                    jcaChannel.put(from, this);
                } else {
                    PVShortArray fromArray =(PVShortArray)pvField;
                    int len = fromArray.get(0, elementCount, shortArrayData);
                    short[] from = shortArrayData.data;
                    int capacity = fromArray.getCapacity();
                    for (int i=len; i<capacity; i++) from[i] = 0;
                    jcaChannel.put(from, this);
                }
            } else if(nativeDBRType==DBRType.INT) {
                if(elementCount==1) {
                    PVInt pvFrom = (PVInt)pvField;
                    int from = pvFrom.get();
                    jcaChannel.put(from, this);
                } else {
                    PVIntArray fromArray =(PVIntArray)pvField;
                    int len = fromArray.get(0, elementCount, intArrayData);
                    int[] from = intArrayData.data;
                    int capacity = fromArray.getCapacity();
                    for (int i=len; i<capacity; i++) from[i] = 0;
                    jcaChannel.put(from, this);
                }
            } else if(nativeDBRType==DBRType.FLOAT) {
                if(elementCount==1) {
                    PVFloat pvFrom = (PVFloat)pvField;
                    float from = pvFrom.get();
                    jcaChannel.put(from, this);
                } else {
                    PVFloatArray fromArray =(PVFloatArray)pvField;
                    int len = fromArray.get(0, elementCount, floatArrayData);
                    float[] from = floatArrayData.data;
                    int capacity = fromArray.getCapacity();
                    for (int i=len; i<capacity; i++) from[i] = 0;
                    jcaChannel.put(from, this);
                }
            } else if(nativeDBRType==DBRType.DOUBLE) {
                if(elementCount==1) {
                    PVDouble pvFrom = (PVDouble)pvField;
                    double from = pvFrom.get();
                    jcaChannel.put(from, this);
                } else {
                    PVDoubleArray fromArray =(PVDoubleArray)pvField;
                    int len = fromArray.get(0, elementCount, doubleArrayData);
                    double[] from = doubleArrayData.data;
                    int capacity = fromArray.getCapacity();
                    for (int i=len; i<capacity; i++) from[i] = 0;
                    jcaChannel.put(from, this);
                }
            } else if(nativeDBRType==DBRType.STRING) {
                if(elementCount==1) {
                    PVString pvFrom = (PVString)pvField;
                    String from = pvFrom.get();
                    jcaChannel.put(from, this);
                } else {
                    PVStringArray fromArray =(PVStringArray)pvField;
                    int len = fromArray.get(0, elementCount, stringArrayData);
                    String[] from = stringArrayData.data;
                    int capacity = fromArray.getCapacity();
                    for (int i=len; i<capacity; i++) from[i] = "";
                    jcaChannel.put(from, this);
                }
            } else {
            	throw new IllegalArgumentException("unknown DBRType " + nativeDBRType.getName());
            }
        } catch (Throwable th) {
            putDone(statusCreate.createStatus(StatusType.ERROR, "failed to put", th));
            return;
        }
    }
    
    /* (non-Javadoc)
     * @see gov.aps.jca.event.PutListener#putCompleted(gov.aps.jca.event.PutEvent)
     */
    public void putCompleted(PutEvent arg0) {
    	CAStatus caStatus = arg0.getStatus();
        if(!caStatus.isSuccessful()) {
            putDone(statusCreate.createStatus(StatusType.ERROR, caStatus.toString(), null));
            return;
        }
        putDone(okStatus);
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.util.Requester#getRequesterName()
     */
    public String getRequesterName() {
        return channelPutRequester.getRequesterName();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Requester#message(java.lang.String, org.epics.pvData.pv.MessageType)
     */
    public void message(String message, MessageType messageType) {
        channelPutRequester.message(message, messageType);
    }
   
    /* (non-Javadoc)
     * @see gov.aps.jca.event.ConnectionListener#connectionChanged(gov.aps.jca.event.ConnectionEvent)
     */
    public void connectionChanged(ConnectionEvent arg0) {
        if(!arg0.isConnected()) {
    		putDone(disconnectedWhileActiveStatus);
        }
    }
    
    private void putDone(Status success) {
        if(!isActive.getAndSet(false)) return;
        channelPutRequester.putDone(success);
    }

    @Override
	public void lock() {
		lock.lock();
	}

	@Override
	public void unlock() {
		lock.unlock();
	}
}
