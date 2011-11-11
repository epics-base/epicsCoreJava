/*
 * Copyright (c) 2004 by Cosylab
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file "LICENSE-CAJ". If the license is not included visit Cosylab web site,
 * <http://www.cosylab.com>.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package org.epics.ca.server.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.epics.ca.client.AccessRights;
import org.epics.ca.client.Channel;
import org.epics.ca.client.ChannelArray;
import org.epics.ca.client.ChannelArrayRequester;
import org.epics.ca.client.ChannelFind;
import org.epics.ca.client.ChannelFindRequester;
import org.epics.ca.client.ChannelGet;
import org.epics.ca.client.ChannelGetRequester;
import org.epics.ca.client.ChannelProcess;
import org.epics.ca.client.ChannelProcessRequester;
import org.epics.ca.client.ChannelProvider;
import org.epics.ca.client.ChannelPut;
import org.epics.ca.client.ChannelPutGet;
import org.epics.ca.client.ChannelPutGetRequester;
import org.epics.ca.client.ChannelPutRequester;
import org.epics.ca.client.ChannelRPC;
import org.epics.ca.client.ChannelRPCRequester;
import org.epics.ca.client.ChannelRequest;
import org.epics.ca.client.ChannelRequester;
import org.epics.ca.client.GetFieldRequester;
import org.epics.ca.client.Lockable;
import org.epics.ca.server.test.TestChannelProviderImpl.PVTopStructure.PVTopStructureListener;
import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.factory.StatusFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.misc.ThreadPriority;
import org.epics.pvData.misc.Timer;
import org.epics.pvData.misc.Timer.TimerCallback;
import org.epics.pvData.misc.Timer.TimerNode;
import org.epics.pvData.misc.TimerFactory;
import org.epics.pvData.monitor.Monitor;
import org.epics.pvData.monitor.MonitorRequester;
import org.epics.pvData.property.PVTimeStamp;
import org.epics.pvData.property.PVTimeStampFactory;
import org.epics.pvData.property.TimeStamp;
import org.epics.pvData.property.TimeStampFactory;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVBoolean;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.Status.StatusType;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.Type;

/**
 * Implementation of a channel provider for tests.
 * @author msekoranja
 */
public class TestChannelProviderImpl implements ChannelProvider
{
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

    private static final Status okStatus = StatusFactory.getStatusCreate().getStatusOK();
	private static final Status fieldDoesNotExistStatus =
		StatusFactory.getStatusCreate().createStatus(StatusType.ERROR, "field does not exist", null);
	private static final Status destroyedStatus =
		StatusFactory.getStatusCreate().createStatus(StatusType.ERROR, "channel destroyed", null);


    private static boolean getProcess(PVStructure pvRequest) {
    	PVField pvField = pvRequest.getSubField("record.process");
    	if(pvField==null || pvField.getField().getType()!=Type.scalar) return false;
    	Scalar scalar = (Scalar)pvField.getField();
    	if(scalar.getScalarType()==ScalarType.pvString) {
    		PVString pvString = (PVString)pvField;
    		return (pvString.get().equalsIgnoreCase("true")) ? true : false;
    	} else if(scalar.getScalarType()==ScalarType.pvBoolean) {
    		PVBoolean pvBoolean = (PVBoolean)pvField;
    		return pvBoolean.get();
    	}
    	return false;
    }

    static class Mapper
	{
		final static Convert convert = ConvertFactory.getConvert();
		
		final PVStructure originStructure;
		final PVStructure copyStructure;
        final int[] toOriginStructure;
        final int[] toCopyStructure;

        public Mapper(PVStructure originStructure, PVStructure pvRequest)
		{
        	this.originStructure = originStructure;
        	
			ArrayList<Integer> indexMapping = new ArrayList<Integer>(originStructure.getNumberFields());
			
            if(pvRequest.getPVFields().length==0)
            {
            	copyStructure = pvDataCreate.createPVStructure(null, originStructure.getStructure());
				// 1-1 mapping
				int fieldCount = copyStructure.getNumberFields();
				for (int i = 0; i < fieldCount; i++)
					indexMapping.add(i);
            }
            else
            {
    			indexMapping.add(-1);	// top

    			if(pvRequest.getSubField("field")!=null) {
					pvRequest = pvRequest.getStructureField("field");
				}
				Structure structure = createStructure(originStructure, indexMapping, pvRequest, "");
				this.copyStructure = pvDataCreate.createPVStructure(null, structure);
            }
        	
        	
        	
            toOriginStructure = new int[copyStructure.getNumberFields()];
            toCopyStructure = new int[originStructure.getNumberFields()];
            Arrays.fill(toCopyStructure, -1);

            int ix = 0;
            for (Integer i : indexMapping)
            {
            	int iv = i.intValue();
            	toOriginStructure[ix] = iv;
            	if (iv != -1)
            		toCopyStructure[iv] = ix;
            	ix++;
            }
		}
       
        public PVStructure getCopyStructure()
        {
        	return copyStructure;
        }
        
        public int getCopyStructureIndex(int ix)
        {
        	return toCopyStructure[ix];
        }

        public int getOriginStructureIndex(int ix)
        {
        	return toOriginStructure[ix];
        }
        
		void updateCopyStructure(BitSet copyStructureBitSet)
		{
			boolean doAll = copyStructureBitSet.get(0);
			if (doAll)
			{
				for (int i = 1; i < toOriginStructure.length;)
				{
					final PVField copyField = copyStructure.getSubField(i);
					final PVField originField = originStructure.getSubField(toOriginStructure[i]);
					convert.copy(originField, copyField);
					i = copyField.getNextFieldOffset();
				}
			}
			else
			{
				int i = copyStructureBitSet.nextSetBit(1);
				while (i != -1)
				{
					final PVField copyField = copyStructure.getSubField(i);
					final PVField originField = originStructure.getSubField(toOriginStructure[i]);
					convert.copy(originField, copyField);
					i = copyStructureBitSet.nextSetBit(copyField.getNextFieldOffset());
				}
			}
		}

		void updateCopyStructureOriginBitSet(BitSet originStructureBitSet)
		{
			boolean doAll = originStructureBitSet.get(0);
			if (doAll)
			{
				for (int i = 1; i < toOriginStructure.length;)
				{
					final PVField copyField = copyStructure.getSubField(i);
					final PVField originField = originStructure.getSubField(toOriginStructure[i]);
					convert.copy(originField, copyField);
					i = copyField.getNextFieldOffset();
				}
			}
			else
			{
				int i = originStructureBitSet.nextSetBit(1);
				while (i != -1)
				{
					final PVField copyField = copyStructure.getSubField(toCopyStructure[i]);
					final PVField originField = originStructure.getSubField(i);
					convert.copy(originField, copyField);
					i = originStructureBitSet.nextSetBit(originField.getNextFieldOffset());
				}
			}
		}

		private static final Pattern commaPattern = Pattern.compile("[,]");

		private static void addMapping(PVField pvRecordField, ArrayList<Integer> indexMapping) {
			if (pvRecordField.getField().getType() == Type.structure)
			{
				indexMapping.add(pvRecordField.getFieldOffset());
				PVStructure struct = (PVStructure)pvRecordField;
				for (PVField pvField : struct.getPVFields())
					addMapping(pvField, indexMapping);
			}
			else
			{
				indexMapping.add(pvRecordField.getFieldOffset());
			}
		}

        private static Structure createStructure(PVStructure pvRecord, ArrayList<Integer> indexMapping, PVStructure pvFromRequest,String fieldName) {
            PVField[] pvFromFields = pvFromRequest.getPVFields();
            int length = pvFromFields.length;
            ArrayList<Field> fieldList = new ArrayList<Field>(length);
            for(int i=0; i<length; i++) {
            	PVField pvField = pvFromFields[i];
            	if(pvField.getField().getType()==Type.structure) {
            		PVStructure pvStruct = (PVStructure)pvField;
            		PVField pvLeaf = pvStruct.getSubField("leaf.source");
            		if(pvLeaf!=null && (pvLeaf instanceof PVString)){
            			PVString pvString = (PVString)pvLeaf;
            			PVField pvRecordField = pvRecord.getSubField(pvString.get());
            			if(pvRecordField!=null) {
            				Field field = fieldCreate.create(pvField.getField().getFieldName(),pvRecordField.getField());
            				addMapping(pvRecordField, indexMapping);
            				fieldList.add(field);
            			}
            		} else {
        				indexMapping.add(-1);		// fake structure, will not be mapped
            			fieldList.add(createStructure(pvRecord,indexMapping,pvStruct,pvField.getField().getFieldName()));
            		}
            	} else {
            		PVString pvString = (PVString)pvFromFields[i];
            		if(pvString.getField().getFieldName().equals("fieldList")) {
            			String[] fieldNames = commaPattern.split(pvString.get());
            			for(int j=0; j<fieldNames.length; j++) {
            				PVField pvRecordField = pvRecord.getSubField(fieldNames[j].trim());
            				if(pvRecordField!=null) {
                				addMapping(pvRecordField, indexMapping);
            					fieldList.add(pvRecordField.getField());
            				}
            			}
            		} else {
            			PVField pvRecordField = pvRecord.getSubField(pvString.get().trim());
            			if(pvRecordField!=null) {
            				Field field = fieldCreate.create(pvField.getField().getFieldName(),pvRecordField.getField());
            				addMapping(pvRecordField, indexMapping);
            				fieldList.add(field);
            			}
            		}
            	}
            }
            Field[] fields = new Field[fieldList.size()];
            fields = fieldList.toArray(fields);
            return fieldCreate.createStructure(fieldName, fields);
        }
        
	}

    static class PVTopStructure implements Lockable
    {
    	public interface PVTopStructureListener {
    		public void topStructureChanged(BitSet changedBitSet);
    	}
    	
    	private final Lock lock = new ReentrantLock();
    	private final PVStructure pvStructure;
    	private final ArrayList<PVTopStructureListener> listeners = new ArrayList<PVTopStructureListener>();
    	
    	public PVTopStructure(Field valueType)
    	{
    		// TODO use PVStandard when available
    		
			PVStructure timeStampStructure;
			{
		        Field[] fields = new Field[3];
		        fields[0] = fieldCreate.createScalar("secondsPastEpoch", ScalarType.pvLong);
		        fields[1] = fieldCreate.createScalar("nanoSeconds", ScalarType.pvInt);
		        fields[2] = fieldCreate.createScalar("userTag", ScalarType.pvInt);
		        timeStampStructure = pvDataCreate.createPVStructure(null, "timeStamp", fields);
			}
		
			PVStructure alarmStructure;
			{
		        Field[] fields = new Field[3];
		        fields[0] = fieldCreate.createScalar("severity", ScalarType.pvInt);
		        fields[1] = fieldCreate.createScalar("status", ScalarType.pvInt);
		        fields[2] = fieldCreate.createScalar("message", ScalarType.pvString);
		        alarmStructure = pvDataCreate.createPVStructure(null, "alarm", fields);
			}
			
	        Field[] fields = new Field[3];
	        fields[0] = fieldCreate.create("value", valueType);
	        fields[1] = timeStampStructure.getField();
	        fields[2] = alarmStructure.getField();
	        
	        pvStructure = pvDataCreate.createPVStructure(null, "", fields);
    	}
    	
    	public PVStructure getPVStructure()
    	{
    		return pvStructure;
    	}
    	
    	public void process()
    	{
    		// default is noop
    	}
    	
    	public void lock()
    	{
    		lock.lock();
    	}
    	
    	public void unlock()
    	{
    		lock.unlock();
    	}
    	
    	public void registerListener(PVTopStructureListener listener)
    	{
    		synchronized (listeners) {
				listeners.add(listener);
			}
    	}
    	
       	public void unregisterListener(PVTopStructureListener listener)
    	{
    		synchronized (listeners) {
				listeners.remove(listener);
			}
    	}
       	
       	public void notifyListeners(BitSet changedBitSet)
    	{
    		synchronized (listeners) {
    			for (PVTopStructureListener listener : listeners)
    			{
    				try {
    					listener.topStructureChanged(changedBitSet);
    				}
    				catch (Throwable th) {
    					Writer writer = new StringWriter();
    					PrintWriter printWriter = new PrintWriter(writer);
    					th.printStackTrace(printWriter);
    					pvStructure.message("Unexpected exception caught: " + writer, MessageType.fatalError);
    				}
    			}
			}
    	}
       	
    }
	
	class TestChannelImpl implements Channel
	{
		
		class TestChannelGetImpl implements ChannelGet, PVTopStructureListener
		{
			private final PVTopStructure pvTopStructure;
			private final ChannelGetRequester channelGetRequester;
			private final AtomicBoolean destroyed = new AtomicBoolean();
			private final PVStructure pvGetStructure;
			private final Mapper mapper;
			private final BitSet bitSet;		// for user
			private final BitSet activeBitSet;		// changed monitoring
			private final boolean process;
			private final ReentrantLock lock = new ReentrantLock();
			private final AtomicBoolean firstGet = new AtomicBoolean(true);
			
			public TestChannelGetImpl(PVTopStructure pvTopStructure, ChannelGetRequester channelGetRequester, PVStructure pvRequest)
			{
				this.pvTopStructure = pvTopStructure;
				this.channelGetRequester = channelGetRequester;
			
				process = getProcess(pvRequest);
				
				mapper = new Mapper(pvTopStructure.getPVStructure(), pvRequest);
				
				pvGetStructure = mapper.getCopyStructure();
				activeBitSet = new BitSet(pvGetStructure.getNumberFields());
	            activeBitSet.set(0);	// initial get gets all

				bitSet = new BitSet(pvGetStructure.getNumberFields());
				
				registerRequest(this);
				
				channelGetRequester.channelGetConnect(okStatus, this, pvGetStructure, bitSet);
			}
			
			@Override
			public void lock() {
				lock.lock();
			}

			@Override
			public void unlock() {
				lock.unlock();
			}

			@Override
			public void get(boolean lastRequest) {
				if (destroyed.get())
				{
					channelGetRequester.getDone(destroyedStatus);
					return;
				}

				lock();
				pvTopStructure.lock();
				try
				{
					if (process)
						pvTopStructure.process();
				
					bitSet.clear();
					bitSet.set(activeBitSet);
					activeBitSet.clear();
					if (firstGet.getAndSet(false))
						pvTopStructure.registerListener(this);
					mapper.updateCopyStructureOriginBitSet(bitSet);
					channelGetRequester.getDone(okStatus);
				}
				finally {
					pvTopStructure.unlock();
					unlock();
				}

				
				if (lastRequest)
					destroy();
			}

			@Override
			public void destroy() {
				if (destroyed.getAndSet(true))
					return;
				pvTopStructure.unregisterListener(this);
				unregisterRequest(this);
			}

			@Override
			public void topStructureChanged(BitSet changedBitSet) {
				lock();
				activeBitSet.or(changedBitSet);
				unlock();
			}

		}
		
		
		
		
		private final String channelName;
		private final ChannelRequester channelRequester;
		private final PVTopStructure pvTopStructure;
		
		private final ArrayList<ChannelRequest> channelRequests = new ArrayList<ChannelRequest>();

		TestChannelImpl(String channelName, ChannelRequester channelRequester, PVTopStructure pvTopStructure)
		{
			this.channelName = channelName;
			this.channelRequester = channelRequester;
			
			this.pvTopStructure = pvTopStructure;
			
			setConnectionState(ConnectionState.CONNECTED);
		}
		
		public void registerRequest(ChannelRequest request)
		{
			synchronized (channelRequests) {
				channelRequests.add(request);
			}
		}
		
		public void unregisterRequest(ChannelRequest request)
		{
			synchronized (channelRequests) {
				channelRequests.remove(request);
			}
		}

		private void destroyRequests()
		{
			synchronized (channelRequests) {
				while (!channelRequests.isEmpty())
					channelRequests.get(channelRequests.size() - 1).destroy();
			}
		}
		@Override
		public String getRequesterName() {
			return channelRequester.getRequesterName();
		}

		@Override
		public void message(String message, MessageType messageType) {
			System.err.println("[" + messageType + "] " + message);
		}

		@Override
		public ChannelProvider getProvider() {
			return TestChannelProviderImpl.this;
		}

		@Override
		public String getRemoteAddress() {
			return "local";
		}

		private volatile ConnectionState connectionState = ConnectionState.NEVER_CONNECTED;
		private void setConnectionState(ConnectionState state)
		{
			this.connectionState = state;
			channelRequester.channelStateChange(this, state);
		}
		
		@Override
		public ConnectionState getConnectionState() {
			return connectionState;
		}

		@Override
		public boolean isConnected() {
			return getConnectionState() == ConnectionState.CONNECTED;
		}

		private final AtomicBoolean destroyed = new AtomicBoolean(false);
		
		@Override
		public void destroy() {
			if (destroyed.getAndSet(true) == false)
			{
				destroyRequests();

				setConnectionState(ConnectionState.DISCONNECTED);
				setConnectionState(ConnectionState.DESTROYED);
			}
		}

		@Override
		public String getChannelName() {
			return channelName;
		}

		@Override
		public ChannelRequester getChannelRequester() {
			return channelRequester;
		}

		@Override
		public void getField(GetFieldRequester requester, String subField) {
			
			if (requester == null)
				throw new IllegalArgumentException("requester");
			
			if (destroyed.get())
			{
				requester.getDone(destroyedStatus, null);
				return;
			}
			
			Field field;
			if (subField == null)
				field = pvTopStructure.getPVStructure().getStructure();
			else
				field = pvTopStructure.getPVStructure().getStructure().getField(subField);
			
			if (field != null)
				requester.getDone(okStatus, field);
			else
				requester.getDone(fieldDoesNotExistStatus, null);
		}

		@Override
		public AccessRights getAccessRights(PVField pvField) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ChannelProcess createChannelProcess(
				ChannelProcessRequester channelProcessRequester,
				PVStructure pvRequest) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ChannelGet createChannelGet(
				ChannelGetRequester channelGetRequester, PVStructure pvRequest) {
			
			if (channelGetRequester == null)
				throw new IllegalArgumentException("channelGetRequester");
			
			if (pvRequest == null)
				throw new IllegalArgumentException("pvRequest");
			
			if (destroyed.get())
			{
				channelGetRequester.channelGetConnect(destroyedStatus, null, null, null);
				return null;
			}

			return new TestChannelGetImpl(pvTopStructure, channelGetRequester, pvRequest); 
		}

		@Override
		public ChannelPut createChannelPut(
				ChannelPutRequester channelPutRequester, PVStructure pvRequest) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ChannelPutGet createChannelPutGet(
				ChannelPutGetRequester channelPutGetRequester,
				PVStructure pvRequest) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ChannelRPC createChannelRPC(
				ChannelRPCRequester channelRPCRequester, PVStructure pvRequest) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Monitor createMonitor(MonitorRequester monitorRequester,
				PVStructure pvRequest) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ChannelArray createChannelArray(
				ChannelArrayRequester channelArrayRequester,
				PVStructure pvRequest) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	
	
	
	
	public static final String PROVIDER_NAME = "test";

	public TestChannelProviderImpl()
	{
	}
	
	@Override
	public String getProviderName() {
		return PROVIDER_NAME;
	}

	private ChannelFind channelFind = new ChannelFind() {
		
		@Override
		public ChannelProvider getChannelProvider() {
			return getChannelProvider();
		}
		
		@Override
		public void cancelChannelFind() {
			// noop, sync call
		}
	};
	
	private boolean isSupported(String channelName)
	{
		return
			channelName.equals("counter") ||
			channelName.equals("simpleCounter") ||
			channelName.equals("valueOnly");
	}

	static class CounterTopStructure extends PVTopStructure implements TimerCallback
	{
		private final PVInt valueField;
		private final int timeStampFieldOffset;
		private final PVTimeStamp timeStampField;
		private final TimerNode timerNode;
		
		private final TimeStamp timeStamp = TimeStampFactory.create();

		private final BitSet changedBitSet;
		
		public CounterTopStructure(double scanPeriodHz, Timer timer) {
			super(fieldCreate.createScalar("value", ScalarType.pvInt));

			changedBitSet = new BitSet(getPVStructure().getNumberFields());
			
			valueField = getPVStructure().getIntField("value");
			
			timeStampField = PVTimeStampFactory.create();
			PVField ts = getPVStructure().getStructureField("timeStamp");
			timeStampField.attach(ts);
			timeStampFieldOffset = ts.getFieldOffset();
			if (scanPeriodHz > 0.0)
			{
				timerNode = TimerFactory.createNode(this);
				timer.schedulePeriodic(timerNode, 0.0, scanPeriodHz);
			}
			else
				timerNode = null;
			
		}

		/* (non-Javadoc)
		 * @see org.epics.ca.server.test.TestChannelProviderImpl.PVTopStructure#process()
		 */
		@Override
		public void process() {
			changedBitSet.clear();
			
			valueField.put(valueField.get() + 1);
			changedBitSet.set(valueField.getFieldOffset());
			
			timeStamp.getCurrentTime();
			timeStampField.set(timeStamp);
			changedBitSet.set(timeStampFieldOffset);
			
			notifyListeners(changedBitSet);
		}

		@Override
		public void callback() {
			lock();
			try
			{
				process();
			} 
			finally
			{
				unlock();
			}
		}

		@Override
		public void timerStopped() {
		}

		public void cancel()
		{
			if (timerNode != null)
				timerNode.cancel();
		}
	}
	
	private static final Timer timer = TimerFactory.create("counter timer", ThreadPriority.middle);
	private final HashMap<String, PVTopStructure> tops = new HashMap<String, PVTopStructure>();
		
	private synchronized PVTopStructure getTopStructure(String channelName)
	{
		//synchronized (tops) {
			PVTopStructure cached = tops.get(channelName);
			if (cached != null)
				return cached;
		//}
		
		PVTopStructure retVal;
		
		// inc with 1Hz
		if (channelName.equals("counter"))
		{
			retVal = new CounterTopStructure(1.0, timer);
		}
		// inc on process only
		else if (channelName.equals("simpleCounter"))
		{
			retVal =  new CounterTopStructure(0.0, timer);
		}
		else if (channelName.equals("valueOnly"))
		{
			retVal =  new PVTopStructure(fieldCreate.createScalar("value", ScalarType.pvDouble));
		}
		else
		{
			// default
			retVal =  new PVTopStructure(fieldCreate.createScalar("value", ScalarType.pvDouble));
		}

		//synchronized (tops) {
			tops.put(channelName, retVal);
		//}
		
		return retVal;
	}

	@Override
	public ChannelFind channelFind(String channelName,
			ChannelFindRequester channelFindRequester) {
		
		if (channelName == null)
			throw new IllegalArgumentException("channelName");

		if (channelFindRequester == null)
			throw new IllegalArgumentException("channelFindRequester");
		
		boolean found = isSupported(channelName);
		channelFindRequester.channelFindResult(
				okStatus,
				channelFind,
				found);
		
		return channelFind;
	}

	private static final Status channelNotFoundStatus =
		StatusFactory.getStatusCreate().createStatus(StatusType.ERROR, "channel not found", null);

	@Override
	public Channel createChannel(String channelName,
			ChannelRequester channelRequester, short priority) {
		
		if (channelName == null)
			throw new IllegalArgumentException("channelName");

		if (channelRequester == null)
			throw new IllegalArgumentException("channelRequester");
		
		if (priority < ChannelProvider.PRIORITY_MIN ||
			priority > ChannelProvider.PRIORITY_MAX)
			throw new IllegalArgumentException("priority out of range");
			
		Channel channel = isSupported(channelName) ?
				new TestChannelImpl(channelName, channelRequester, getTopStructure(channelName)) :
				null;
		
		Status status = (channel == null) ? channelNotFoundStatus : okStatus;
		channelRequester.channelCreated(status, channel);
		
		return channel;
	}

	@Override
	public Channel createChannel(String channelName,
			ChannelRequester channelRequester, short priority,
			String address) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void destroy() {
	}

}