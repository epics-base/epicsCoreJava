/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca;

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

import org.epics.pvaccess.client.ChannelPut;
import org.epics.pvaccess.client.ChannelPutRequester;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.ByteArrayData;
import org.epics.pvdata.pv.DoubleArrayData;
import org.epics.pvdata.pv.FloatArrayData;
import org.epics.pvdata.pv.IntArrayData;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVByte;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVFloat;
import org.epics.pvdata.pv.PVFloatArray;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVShort;
import org.epics.pvdata.pv.PVShortArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ShortArrayData;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.StatusCreate;
import org.epics.pvdata.pv.StringArrayData;

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
    private static final Status channelDestroyedStatus = statusCreate.createStatus(StatusType.ERROR, "channel destroyed", null);
    private static final Status channelNotConnectedStatus = statusCreate.createStatus(StatusType.ERROR, "channel not connected", null);
    private static final Status disconnectedWhileActiveStatus = statusCreate.createStatus(StatusType.ERROR, "disconnected while active", null);
    private static final Status createChannelStructureStatus = statusCreate.createStatus(StatusType.ERROR, "createChannelStructure failed", null);

    private final ChannelPutRequester channelPutRequester;
    private final V3Channel v3Channel;
    private final V3ChannelStructure v3ChannelStructure;
    private final gov.aps.jca.Channel jcaChannel;

    private volatile int elementCount;

    private final ReentrantLock lock = new ReentrantLock();

    private volatile boolean isDestroyed = false;
    private volatile boolean lastRequest = false;

    private final ByteArrayData byteArrayData = new ByteArrayData();
    private final ShortArrayData shortArrayData = new ShortArrayData();
    private final IntArrayData intArrayData = new IntArrayData();
    private final FloatArrayData floatArrayData = new FloatArrayData();
    private final DoubleArrayData doubleArrayData = new DoubleArrayData();
    private final StringArrayData stringArrayData = new StringArrayData();

    private final AtomicBoolean isActive = new AtomicBoolean(false);
    private final AtomicBoolean isGetActive = new AtomicBoolean(false);

    private final PVStructure pvRequest;
    private boolean block = false;

    /**
     * Constructor.
     * @param channelPutRequester The channelPutRequester.
     * @param v3Channel The V3Channel
     * @param pvRequest The request structure.
     */
    public BaseV3ChannelPut(ChannelPutRequester channelPutRequester,
    		V3Channel v3Channel,PVStructure pvRequest)
    {
        this.channelPutRequester = channelPutRequester;
        this.v3Channel = v3Channel;
        this.pvRequest = pvRequest;
        v3Channel.add(this);
        v3ChannelStructure = new BaseV3ChannelStructure(v3Channel);

        this.jcaChannel = v3Channel.getJCAChannel();
        try {
            jcaChannel.addConnectionListener(this);
        } catch (Throwable th) {
            elementCount = 1;
            channelPutRequester.channelPutConnect(statusCreate.createStatus(StatusType.ERROR, "addConnectionListener failed", th),this,null);
            destroy();
            return;
        }

        // there is a possible run condition, but it's OK
		if (jcaChannel.getConnectionState() == Channel.CONNECTED)
			connectionChanged(new ConnectionEvent(jcaChannel, true));
    }

    protected void initializePut()
    {
        PVString pvString = pvRequest.getSubField(PVString.class,"record._options.block");
        if(pvString!=null) {
            String value = pvString.get();
            if(value.equals("true")) block = true;
        }
        if(v3ChannelStructure.createPVStructure(pvRequest,true)==null) {
            elementCount = 1;
            channelPutRequester.channelPutConnect(createChannelStructureStatus,this,null);
            destroy();
            return;
        }

        elementCount = jcaChannel.getElementCount();

        DBRType nativeDBRType = v3ChannelStructure.getNativeDBRType();
        if(nativeDBRType.isENUM() && elementCount != 1) {
            channelPutRequester.channelPutConnect(statusCreate.createStatus(StatusType.ERROR, "array of ENUM not supported", null),this,null);
            destroy();
            return;
        }

        channelPutRequester.channelPutConnect(okStatus,this,
            v3ChannelStructure.getPVStructure().getStructure());
    }
    /* (non-Javadoc)
     * @see org.epics.ioc.ca.ChannelPut#destroy()
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
     * @see org.epics.pvaccess.client.ChannelPut#get()
     */
    public void get() {
        if(isDestroyed) {
            getDone(channelDestroyedStatus);
            return;
        }
        if(jcaChannel.getConnectionState()!=Channel.ConnectionState.CONNECTED) {
            getDone(channelNotConnectedStatus);
            return;
        }

        isGetActive.set(true);
        try {
            jcaChannel.get(v3ChannelStructure.getRequestDBRType(), elementCount, this);
        } catch (Throwable th) {
            getDone(statusCreate.createStatus(StatusType.ERROR, "failed to get", th));
        }
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
    private void getDone(Status success) {
    	if (!isGetActive.getAndSet(false)) return;
        if (lastRequest) destroy();
        channelPutRequester.getDone(success, this, v3ChannelStructure.getPVStructure(), v3ChannelStructure.getBitSet());
    }

    public void put(PVStructure pvPutStructure, BitSet bitSet) {
        if(isDestroyed) {
    		putDone(channelDestroyedStatus);
            return;
        }
        if(jcaChannel.getConnectionState()!=Channel.ConnectionState.CONNECTED) {
    		putDone(channelNotConnectedStatus);
        	return;
        };

        DBRType nativeDBRType = v3ChannelStructure.getNativeDBRType();

        PVField pvField = pvPutStructure.getSubField("value");
        if (pvField == null)
        {
        	channelPutRequester.putDone(statusCreate.createStatus(StatusType.ERROR, "invalid put structure, value field required", null), this);
        	return;
        }

        PVInt pvIndex;
        if(nativeDBRType.isENUM())
            pvIndex = ((PVStructure)pvField).getIntField("index");
        else
        	pvIndex = null;

        // do a simple bitSet check
        boolean bitSetCheck = bitSet.get(0) || bitSet.get(pvField.getFieldOffset()) ||
        					  (pvIndex != null && bitSet.get(pvIndex.getFieldOffset()));
        if (!bitSetCheck)
        {
        	channelPutRequester.putDone(statusCreate.createStatus(StatusType.ERROR, "invalid bitSet, only value can be put for CA", null), this);
        	return;
        }
        PutListener callback = (block ? this : null);
        isActive.set(true);
        try {
            if(pvIndex!=null) {
                short index = (short)pvIndex.get();
                jcaChannel.put(index, callback);
            } else if(nativeDBRType==DBRType.BYTE) {
                if(elementCount==1) {
                    PVByte pvFrom = (PVByte)pvField;
                    byte from = pvFrom.get();
                    jcaChannel.put(from, callback);
                } else {
                    PVByteArray fromArray =(PVByteArray)pvField;
                    int len = fromArray.get(0, elementCount, byteArrayData);
                    byte[] from = byteArrayData.data;
                    int capacity = fromArray.getCapacity();
                    for (int i=len; i<capacity; i++) from[i] = 0;
                    jcaChannel.put(from, callback);
                }
            } else if(nativeDBRType==DBRType.SHORT) {
                if(elementCount==1) {
                    PVShort pvFrom = (PVShort)pvField;
                    short from = pvFrom.get();
                    jcaChannel.put(from, callback);
                } else {
                    PVShortArray fromArray =(PVShortArray)pvField;
                    int len = fromArray.get(0, elementCount, shortArrayData);
                    short[] from = shortArrayData.data;
                    int capacity = fromArray.getCapacity();
                    for (int i=len; i<capacity; i++) from[i] = 0;
                    jcaChannel.put(from, callback);
                }
            } else if(nativeDBRType==DBRType.INT) {
                if(elementCount==1) {
                    PVInt pvFrom = (PVInt)pvField;
                    int from = pvFrom.get();
                    jcaChannel.put(from, callback);
                } else {
                    PVIntArray fromArray =(PVIntArray)pvField;
                    int len = fromArray.get(0, elementCount, intArrayData);
                    int[] from = intArrayData.data;
                    int capacity = fromArray.getCapacity();
                    for (int i=len; i<capacity; i++) from[i] = 0;
                    jcaChannel.put(from, callback);
                }
            } else if(nativeDBRType==DBRType.FLOAT) {
                if(elementCount==1) {
                    PVFloat pvFrom = (PVFloat)pvField;
                    float from = pvFrom.get();
                    jcaChannel.put(from, callback);
                } else {
                    PVFloatArray fromArray =(PVFloatArray)pvField;
                    int len = fromArray.get(0, elementCount, floatArrayData);
                    float[] from = floatArrayData.data;
                    int capacity = fromArray.getCapacity();
                    for (int i=len; i<capacity; i++) from[i] = 0;
                    jcaChannel.put(from, callback);
                }
            } else if(nativeDBRType==DBRType.DOUBLE) {
                if(elementCount==1) {
                    PVDouble pvFrom = (PVDouble)pvField;
                    double from = pvFrom.get();
                    jcaChannel.put(from, callback);
                } else {
                    PVDoubleArray fromArray =(PVDoubleArray)pvField;
                    int len = fromArray.get(0, elementCount, doubleArrayData);
                    double[] from = doubleArrayData.data;
                    int capacity = fromArray.getCapacity();
                    for (int i=len; i<capacity; i++) from[i] = 0;
                    jcaChannel.put(from, callback);
                }
            } else if(nativeDBRType==DBRType.STRING) {
                if(elementCount==1) {
                    PVString pvFrom = (PVString)pvField;
                    String from = pvFrom.get();
                    jcaChannel.put(from, callback);
                } else {
                    PVStringArray fromArray =(PVStringArray)pvField;
                    int len = fromArray.get(0, elementCount, stringArrayData);
                    String[] from = stringArrayData.data;
                    int capacity = fromArray.getCapacity();
                    for (int i=len; i<capacity; i++) from[i] = "";
                    jcaChannel.put(from, callback);
                }
            } else {
            	throw new IllegalArgumentException("unknown DBRType " + nativeDBRType.getName());
            }
        } catch (Throwable th) {
            putDone(statusCreate.createStatus(StatusType.ERROR, "failed to put", th));
            return;
        }
        if(!block) {
            putDone(okStatus);
        }
    }

    /* (non-Javadoc)
     * @see gov.aps.jca.event.PutListener#putCompleted(gov.aps.jca.event.PutEvent)
     */
    public void putCompleted(PutEvent event) {
        CAStatus caStatus = event.getStatus();
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
     * @see org.epics.pvdata.pv.Requester#message(java.lang.String, org.epics.pvdata.pv.MessageType)
     */
    public void message(String message, MessageType messageType) {
        channelPutRequester.message(message, messageType);
    }

    /* (non-Javadoc)
     * @see gov.aps.jca.event.ConnectionListener#connectionChanged(gov.aps.jca.event.ConnectionEvent)
     */
    public void connectionChanged(ConnectionEvent event) {
        if(!event.isConnected()) {
    		putDone(disconnectedWhileActiveStatus);
    		getDone(disconnectedWhileActiveStatus);
        }
        else
        {
        	initializePut();
        }
    }

    private void putDone(Status success) {
        if(!isActive.getAndSet(false)) return;
        if(lastRequest) destroy();
        channelPutRequester.putDone(success,this);
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
