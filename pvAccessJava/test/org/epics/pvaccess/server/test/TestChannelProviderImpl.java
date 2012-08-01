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

package org.epics.pvaccess.server.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.epics.pvaccess.server.test.helpers.CounterTopStructure;
import org.epics.pvaccess.server.test.helpers.Mapper;
import org.epics.pvaccess.server.test.helpers.PVRequestUtils;
import org.epics.pvaccess.server.test.helpers.PVTopStructure;
import org.epics.pvaccess.server.test.helpers.PVTopStructure.PVTopStructureListener;
import org.epics.pvaccess.server.test.helpers.RPCTopStructure;
import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.client.AccessRights;
import org.epics.pvaccess.client.Channel;
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
import org.epics.pvaccess.client.ChannelRequest;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.client.GetFieldRequester;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.misc.ThreadPriority;
import org.epics.pvdata.misc.Timer;
import org.epics.pvdata.misc.TimerFactory;
import org.epics.pvdata.monitor.Monitor;
import org.epics.pvdata.monitor.MonitorElement;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.StatusCreate;
import org.epics.pvdata.pv.Type;

/**
 * Implementation of a channel provider for tests.
 * @author msekoranja
 */
public class TestChannelProviderImpl implements ChannelProvider
{
    private static final FieldCreate fieldCreate = PVFactory.getFieldCreate();
    private static final PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();
    private static final StatusCreate statusCreate = PVFactory.getStatusCreate();
    private static final Convert convert = ConvertFactory.getConvert();

    private static final Status okStatus = statusCreate.getStatusOK();
	private static final Status fieldDoesNotExistStatus =
		statusCreate.createStatus(StatusType.ERROR, "field does not exist", null);
	private static final Status destroyedStatus =
		statusCreate.createStatus(StatusType.ERROR, "channel destroyed", null);
    private static final Status illegalRequestStatus =
    	statusCreate.createStatus(StatusType.ERROR, "illegal pvRequest", null);
    private static final Status capacityImmutableStatus =
    	statusCreate.createStatus(StatusType.ERROR, "capacity is immutable", null);
    private static final Status subFieldDoesNotExistStatus =
    	statusCreate.createStatus(StatusType.ERROR, "subField does not exist", null);
    private static final Status subFieldNotDefinedStatus =
    	statusCreate.createStatus(StatusType.ERROR, "subField not defined", null);
    private static final Status subFieldNotArrayStatus =
    	statusCreate.createStatus(StatusType.ERROR, "subField is not an array", null);

    class TestChannelImpl implements Channel
	{
		
		class TestBasicChannelRequest implements ChannelRequest
		{
			protected final PVTopStructure pvTopStructure;
			protected final AtomicBoolean destroyed = new AtomicBoolean();
			protected final Mapper mapper;
			protected final ReentrantLock lock = new ReentrantLock();
			
			public TestBasicChannelRequest(PVTopStructure pvTopStructure, PVStructure pvRequest) {
				this.pvTopStructure = pvTopStructure;
				
				if (pvRequest != null)
					mapper = new Mapper(pvTopStructure.getPVStructure(), pvRequest);
				else
					mapper = null;
				
				registerRequest(this);
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
			public final void destroy() {
				if (destroyed.getAndSet(true))
					return;
				unregisterRequest(this);
				internalDestroy();
			}
			
			protected void internalDestroy()
			{
				// noop
			}

		}

		class TestChannelGetImpl extends TestBasicChannelRequest implements ChannelGet, PVTopStructureListener
		{
			private final ChannelGetRequester channelGetRequester;
			private final PVStructure pvGetStructure;
			private final BitSet bitSet;		// for user
			private final BitSet activeBitSet;		// changed monitoring
			private final boolean process;
			private final AtomicBoolean firstGet = new AtomicBoolean(true);
			
			public TestChannelGetImpl(PVTopStructure pvTopStructure, ChannelGetRequester channelGetRequester, PVStructure pvRequest)
			{
				super(pvTopStructure, pvRequest);
				
				this.channelGetRequester = channelGetRequester;
			
				process = PVRequestUtils.getProcess(pvRequest);
				
				pvGetStructure = mapper.getCopyStructure();
				activeBitSet = new BitSet(pvGetStructure.getNumberFields());
	            activeBitSet.set(0);	// initial get gets all

				bitSet = new BitSet(pvGetStructure.getNumberFields());
				
				channelGetRequester.channelGetConnect(okStatus, this, pvGetStructure, bitSet);
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
				
					mapper.updateCopyStructureOriginBitSet(activeBitSet, bitSet);
					activeBitSet.clear();
					if (firstGet.getAndSet(false))
						pvTopStructure.registerListener(this);
				}
				finally {
					pvTopStructure.unlock();
					unlock();
				}

				channelGetRequester.getDone(okStatus);
				
				if (lastRequest)
					destroy();
			}

			@Override
			public void internalDestroy() {
				pvTopStructure.unregisterListener(this);
			}

			@Override
			public void topStructureChanged(BitSet changedBitSet) {
				lock();
				activeBitSet.or(changedBitSet);
				unlock();
			}

		}
		
		// TODO only queueSize==1 impl.
		class TestChannelMonitorImpl extends TestBasicChannelRequest implements Monitor, PVTopStructureListener, MonitorElement
		{
			private final MonitorRequester monitorRequester;
			private final PVStructure pvGetStructure;
			private final BitSet bitSet;		// for user
			private final BitSet activeBitSet;		// changed monitoring
			private final AtomicBoolean started = new AtomicBoolean(false);
			
			
			// TODO tmp
			private final BitSet allChanged;
			private final BitSet noOverrun;

			
			public TestChannelMonitorImpl(PVTopStructure pvTopStructure, MonitorRequester monitorRequester, PVStructure pvRequest)
			{
				super(pvTopStructure, pvRequest);
				
				this.monitorRequester = monitorRequester;
			
				pvGetStructure = mapper.getCopyStructure();
				activeBitSet = new BitSet(pvGetStructure.getNumberFields());
	            activeBitSet.set(0);	// initial get gets all

				bitSet = new BitSet(pvGetStructure.getNumberFields());
				
				
				allChanged = new BitSet(pvGetStructure.getNumberFields());
				allChanged.set(0);
				noOverrun = new BitSet(pvGetStructure.getNumberFields());
				
				monitorRequester.monitorConnect(okStatus, this, pvGetStructure.getStructure());
			}

			@Override
			public void internalDestroy() {
				pvTopStructure.unregisterListener(this);
			}

			@Override
			public void topStructureChanged(BitSet changedBitSet) {
				lock();
				activeBitSet.or(changedBitSet);

				// add to queue, trigger
				lock();
				pvTopStructure.lock();
				try
				{
					mapper.updateCopyStructureOriginBitSet(activeBitSet, bitSet);
					activeBitSet.clear();
				}
				finally {
					pvTopStructure.unlock();
					unlock();
				}
				unlock();
				// TODO not a safe copy...
				monitorRequester.monitorEvent(this);
			}

			@Override
			public Status start() {
				if (started.getAndSet(true))
					return okStatus;

				// force monitor immediately
				topStructureChanged(allChanged);
				
				pvTopStructure.registerListener(this);
				
				return okStatus;
			}

			@Override
			public Status stop() {
				if (!started.getAndSet(false))
					return okStatus;

				// TODO clear queue
				
				pvTopStructure.unregisterListener(this);

				return okStatus;
			}
			
			
			private final AtomicBoolean pooled = new AtomicBoolean(false);
			@Override
			public MonitorElement poll() {
				if (pooled.getAndSet(true))
					return null;
				
				return this;
			}

			@Override
			public void release(MonitorElement monitorElement) {
				pooled.set(false);
			}
			/* (non-Javadoc)
			 * @see org.epics.pvdata.monitor.MonitorElement#getPVStructure()
			 */
			@Override
			public PVStructure getPVStructure() {
				return pvGetStructure;
			}
			/* (non-Javadoc)
			 * @see org.epics.pvdata.monitor.MonitorElement#getChangedBitSet()
			 */
			@Override
			public BitSet getChangedBitSet() {
				return allChanged;
			}
			/* (non-Javadoc)
			 * @see org.epics.pvdata.monitor.MonitorElement#getOverrunBitSet()
			 */
			@Override
			public BitSet getOverrunBitSet() {
				return noOverrun;
			}

			
			
			
		}

		
		class TestChannelProcessImpl extends TestBasicChannelRequest implements ChannelProcess
		{
			private final ChannelProcessRequester channelProcessRequester;
			
			public TestChannelProcessImpl(PVTopStructure pvTopStructure, ChannelProcessRequester channelProcessRequester, PVStructure pvRequest)
			{
				super(pvTopStructure, pvRequest);
				
				this.channelProcessRequester = channelProcessRequester;
			
				channelProcessRequester.channelProcessConnect(okStatus, this);
			}

			/* (non-Javadoc)
			 * @see org.epics.pvaccess.client.ChannelProcess#process(boolean)
			 */
			@Override
			public void process(boolean lastRequest) {
				if (destroyed.get())
				{
					channelProcessRequester.processDone(destroyedStatus);
					return;
				}

				pvTopStructure.lock();
				try
				{
					pvTopStructure.process();
				}
				finally {
					pvTopStructure.unlock();
				}

				channelProcessRequester.processDone(okStatus);
				
				if (lastRequest)
					destroy();
			}
		}

		
		class TestChannelRPCImpl extends TestBasicChannelRequest implements ChannelRPC
		{
			private final ChannelRPCRequester channelRPCRequester;
			
			public TestChannelRPCImpl(PVTopStructure pvTopStructure, ChannelRPCRequester channelRPCRequester, PVStructure pvRequest)
			{
				super(pvTopStructure, pvRequest);
				
				this.channelRPCRequester = channelRPCRequester;
			
				channelRPCRequester.channelRPCConnect(okStatus, this);
			}

			/* (non-Javadoc)
			 * @see org.epics.pvaccess.client.ChannelRPC#request(org.epics.pvdata.pv.PVStructure, boolean)
			 */
			@Override
			public void request(PVStructure pvArgument, boolean lastRequest) {
				if (destroyed.get())
				{
					channelRPCRequester.requestDone(destroyedStatus, null);
					return;
				}

				// TODO async support
				PVStructure result = null;
				Status status = okStatus;
				pvTopStructure.lock();
				try
				{
					result = pvTopStructure.request(pvArgument);
				}
				catch (Throwable th)
				{
					status = statusCreate.createStatus(StatusType.ERROR, "exceptuon caught: " + th.getMessage(), th);
				}
				finally {
					pvTopStructure.unlock();
				}

				channelRPCRequester.requestDone(status, result);
				
				if (lastRequest)
					destroy();
			}
		}

		
		class TestChannelPutImpl extends TestBasicChannelRequest implements ChannelPut
		{
			private final ChannelPutRequester channelPutRequester;
			private final PVStructure pvPutStructure;
			private final BitSet bitSet;		// for user
			private final boolean process;
			
			public TestChannelPutImpl(PVTopStructure pvTopStructure, ChannelPutRequester channelPutRequester, PVStructure pvRequest)
			{
				super(pvTopStructure, pvRequest);
				
				this.channelPutRequester = channelPutRequester;
			
				process = PVRequestUtils.getProcess(pvRequest);
				
				pvPutStructure = mapper.getCopyStructure();
				bitSet = new BitSet(pvPutStructure.getNumberFields());
				
				channelPutRequester.channelPutConnect(okStatus, this, pvPutStructure, bitSet);
			}

			@Override
			public void put(boolean lastRequest) {
				if (destroyed.get())
				{
					channelPutRequester.putDone(destroyedStatus);
					return;
				}

				lock();
				pvTopStructure.lock();
				try
				{
					mapper.updateOriginStructure(bitSet);

					if (process)
						pvTopStructure.process();

				}
				finally {
					pvTopStructure.unlock();
					unlock();
				}
				
				channelPutRequester.putDone(okStatus);

				if (lastRequest)
					destroy();
			}

			@Override
			public void get() {
				if (destroyed.get())
				{
					channelPutRequester.putDone(destroyedStatus);
					return;
				}

				lock();
				pvTopStructure.lock();
				try
				{
					mapper.updateCopyStructure(null);
				}
				finally {
					pvTopStructure.unlock();
					unlock();
				}
				
				channelPutRequester.getDone(okStatus);
			}

		}

		
		class TestChannelScalarArrayImpl extends TestBasicChannelRequest implements ChannelArray
		{
			private final ChannelArrayRequester channelArrayRequester;
			private final PVScalarArray pvArray;
			private final PVScalarArray pvCopy;
			private final boolean process;
			
			public TestChannelScalarArrayImpl(PVTopStructure pvTopStructure, ChannelArrayRequester channelArrayRequester, PVScalarArray array, PVStructure pvRequest)
			{
				super(pvTopStructure, null);
				
				this.channelArrayRequester = channelArrayRequester;
				this.pvArray = array;
				this.pvCopy = pvDataCreate.createPVScalarArray(pvArray.getScalarArray().getElementType());
			
				process = false; // TODO PVRequestUtils.getProcess(pvRequest);
				
				channelArrayRequester.channelArrayConnect(okStatus, this, pvCopy);
			}

			/* (non-Javadoc)
			 * @see org.epics.pvaccess.client.ChannelArray#putArray(boolean, int, int)
			 */
			@Override
			public void putArray(boolean lastRequest, int offset, int count) {
				if (destroyed.get())
				{
					channelArrayRequester.putArrayDone(destroyedStatus);
					return;
				}

                lock();
				pvTopStructure.lock();
				try
				{
	                if(count<=0) count = pvCopy.getLength();
                    convert.copyScalarArray(pvCopy, 0, pvArray, offset, count);

					if (process)
						pvTopStructure.process();

				}
				finally {
					pvTopStructure.unlock();
					unlock();
				}
				
				channelArrayRequester.putArrayDone(okStatus);

				if (lastRequest)
					destroy();
			}

			/* (non-Javadoc)
			 * @see org.epics.pvaccess.client.ChannelArray#getArray(boolean, int, int)
			 */
			@Override
			public void getArray(boolean lastRequest, int offset, int count) {
				if (destroyed.get())
				{
					channelArrayRequester.getArrayDone(destroyedStatus);
					return;
				}
				
                lock();
				pvTopStructure.lock();
				try
				{
					//if (process)
					//	pvTopStructure.process();

	                if(count<=0) count = pvArray.getLength() - offset;
                    int len = convert.copyScalarArray(pvArray, offset, pvCopy, 0, count);
                    if(!pvCopy.isImmutable()) pvCopy.setLength(len);
				}
				finally {
					pvTopStructure.unlock();
					unlock();
				}
				
				channelArrayRequester.getArrayDone(okStatus);

				if (lastRequest)
					destroy();
			}

			/* (non-Javadoc)
			 * @see org.epics.pvaccess.client.ChannelArray#setLength(boolean, int, int)
			 */
			@Override
			public void setLength(boolean lastRequest, int length, int capacity) {
				if (destroyed.get())
				{
					channelArrayRequester.putArrayDone(destroyedStatus);
					return;
				}

				// TODO process???
                
                lock();
				pvTopStructure.lock();
				try
				{
					if(capacity>=0 && !pvArray.isCapacityMutable()) {
						channelArrayRequester.setLengthDone(capacityImmutableStatus);
						return;
					}

					if(length>=0) {
                    	if(pvArray.getLength()!=length) pvArray.setLength(length);
                    }
                    if(capacity>=0) {
                    	if(pvArray.getCapacity()!=capacity) pvArray.setCapacity(capacity);
                    }
				}
				finally {
					pvTopStructure.unlock();
					unlock();
				}
				
				channelArrayRequester.setLengthDone(okStatus);

				if (lastRequest)
					destroy();
			}

		}

		class TestChannelPutGetImpl extends TestBasicChannelRequest implements ChannelPutGet
		{
			private final ChannelPutGetRequester channelPutGetRequester;
			private PVStructure pvGetStructure;
			private PVStructure pvPutStructure;
			private Mapper putMapper;
			private Mapper getMapper;
			private boolean process;
			
			public TestChannelPutGetImpl(PVTopStructure pvTopStructure, ChannelPutGetRequester channelPutGetRequester, PVStructure pvRequest)
			{
				super(pvTopStructure, null);
				
				this.channelPutGetRequester = channelPutGetRequester;
			
	            PVField pvField = pvRequest.getSubField("putField");
	            if(pvField==null || pvField.getField().getType()!=Type.structure) {
	            	channelPutGetRequester.message("pvRequest does not have a putField request structure", MessageType.error);
	            	channelPutGetRequester.message(pvRequest.toString(),MessageType.warning);
	            	channelPutGetRequester.channelPutGetConnect(illegalRequestStatus, null, null, null);
	            	return;
	            }
				putMapper = new Mapper(pvTopStructure.getPVStructure(), pvRequest, "putField");
				
	        	
	        	pvField = pvRequest.getSubField("getField");
	            if(pvField==null || pvField.getField().getType()!=Type.structure) {
	            	channelPutGetRequester.message("pvRequest does not have a getField request structure", MessageType.error);
	            	channelPutGetRequester.message(pvRequest.toString(),MessageType.warning);
	            	channelPutGetRequester.channelPutGetConnect(illegalRequestStatus, null, null, null);
	            	return;
	            }
				getMapper = new Mapper(pvTopStructure.getPVStructure(), pvRequest, "getField");
	            
	            process = PVRequestUtils.getProcess(pvRequest);
				
				pvPutStructure = putMapper.getCopyStructure();
				pvGetStructure = getMapper.getCopyStructure();
				
				channelPutGetRequester.channelPutGetConnect(okStatus, this, pvPutStructure, pvGetStructure);
			}

			/* (non-Javadoc)
			 * @see org.epics.pvaccess.client.ChannelPutGet#putGet(boolean)
			 */
			@Override
			public void putGet(boolean lastRequest) {
				if (destroyed.get())
				{
					channelPutGetRequester.getPutDone(destroyedStatus);
					return;
				}

				lock();
				pvTopStructure.lock();
				try
				{
					putMapper.updateOriginStructure(null);
					if (process)
						pvTopStructure.process();
					getMapper.updateCopyStructure(null);
				}
				finally {
					pvTopStructure.unlock();
					unlock();
				}
				
				channelPutGetRequester.getPutDone(okStatus);
				
				if (lastRequest)
					destroy();
			}

			/* (non-Javadoc)
			 * @see org.epics.pvaccess.client.ChannelPutGet#getPut()
			 */
			@Override
			public void getPut() {
				if (destroyed.get())
				{
					channelPutGetRequester.getPutDone(destroyedStatus);
					return;
				}

				lock();
				pvTopStructure.lock();
				try
				{
					putMapper.updateCopyStructure(null);
				}
				finally {
					pvTopStructure.unlock();
					unlock();
				}
				
				channelPutGetRequester.getPutDone(okStatus);
			}

			/* (non-Javadoc)
			 * @see org.epics.pvaccess.client.ChannelPutGet#getGet()
			 */
			@Override
			public void getGet() {
				if (destroyed.get())
				{
					channelPutGetRequester.getGetDone(destroyedStatus);
					return;
				}

				lock();
				pvTopStructure.lock();
				try
				{
					getMapper.updateCopyStructure(null);
				}
				finally {
					pvTopStructure.unlock();
					unlock();
				}
				
				channelPutGetRequester.getGetDone(okStatus);
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
			// TODO implement
			return AccessRights.readWrite;
		}

		@Override
		public ChannelProcess createChannelProcess(
				ChannelProcessRequester channelProcessRequester,
				PVStructure pvRequest) {
			
			if (channelProcessRequester == null)
				throw new IllegalArgumentException("channelProcessRequester");
			
			if (destroyed.get())
			{
				channelProcessRequester.channelProcessConnect(destroyedStatus, null);
				return null;
			}

			return new TestChannelProcessImpl(pvTopStructure, channelProcessRequester, pvRequest); 
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
			
			if (channelPutRequester == null)
				throw new IllegalArgumentException("channelPutRequester");
			
			if (pvRequest == null)
				throw new IllegalArgumentException("pvRequest");
			
			if (destroyed.get())
			{
				channelPutRequester.channelPutConnect(destroyedStatus, null, null, null);
				return null;
			}

			return new TestChannelPutImpl(pvTopStructure, channelPutRequester, pvRequest); 
		}

		@Override
		public ChannelPutGet createChannelPutGet(
				ChannelPutGetRequester channelPutGetRequester,
				PVStructure pvRequest) {
			
			if (channelPutGetRequester == null)
				throw new IllegalArgumentException("channelPutGetRequester");
			
			if (pvRequest == null)
				throw new IllegalArgumentException("pvRequest");
			
			if (destroyed.get())
			{
				channelPutGetRequester.channelPutGetConnect(destroyedStatus, null, null, null);
				return null;
			}

			return new TestChannelPutGetImpl(pvTopStructure, channelPutGetRequester, pvRequest); 
		}

		@Override
		public ChannelRPC createChannelRPC(
				ChannelRPCRequester channelRPCRequester, PVStructure pvRequest) {
			
			if (channelRPCRequester == null)
				throw new IllegalArgumentException("channelRPCRequester");
			
			/*
			if (pvRequest == null)
				throw new IllegalArgumentException("pvRequest");
			*/
			
			if (destroyed.get())
			{
				channelRPCRequester.channelRPCConnect(destroyedStatus, null);
				return null;
			}

			return new TestChannelRPCImpl(pvTopStructure, channelRPCRequester, pvRequest); 
		}

		@Override
		public Monitor createMonitor(MonitorRequester monitorRequester,
				PVStructure pvRequest) {
			
			if (monitorRequester == null)
				throw new IllegalArgumentException("monitorRequester");
			
			if (pvRequest == null)
				throw new IllegalArgumentException("pvRequest");
			
			if (destroyed.get())
			{
				monitorRequester.monitorConnect(destroyedStatus, null, null);
				return null;
			}

			return new TestChannelMonitorImpl(pvTopStructure, monitorRequester, pvRequest); 
		}

	    @Override
		public ChannelArray createChannelArray(
				ChannelArrayRequester channelArrayRequester,
				PVStructure pvRequest) {

			if (channelArrayRequester == null)
				throw new IllegalArgumentException("channelArrayRequester");
			
			if (pvRequest == null)
				throw new IllegalArgumentException("pvRequest");
			
			if (destroyed.get())
			{
				channelArrayRequester.channelArrayConnect(destroyedStatus, null, null);
				return null;
			}

            PVField pvField = pvRequest.getSubField("field");
            if(pvField==null || pvField.getField().getType()!=Type.scalar) {
            	channelArrayRequester.channelArrayConnect(subFieldNotDefinedStatus, null, null);
                return null;
            }
            Scalar scalar = (Scalar)pvField.getField();
            if(scalar.getScalarType()!=ScalarType.pvString) {
            	channelArrayRequester.channelArrayConnect(subFieldNotDefinedStatus, null, null);
                return null;
            }
            PVString pvString = (PVString)pvField;
    		pvField = pvTopStructure.getPVStructure().getSubField(pvString.get());
            if(pvField==null) {
            	channelArrayRequester.channelArrayConnect(subFieldDoesNotExistStatus, null, null);
                return null;
            }
            if(pvField.getField().getType()==Type.structureArray) {
            	//PVStructureArray pvArray = (PVStructureArray)pvField;
            	throw new RuntimeException("todo todo");
            	//return new TestChannelStructureArrayImpl(pvTopStructure,channelArrayRequester,pvArray,pvRequest);
            }
            if(pvField.getField().getType()!=Type.scalarArray) {
                channelArrayRequester.channelArrayConnect(subFieldNotArrayStatus, null, null);
                return null;
            }
            PVScalarArray pvArray = (PVScalarArray)pvField;
            return new TestChannelScalarArrayImpl(pvTopStructure,channelArrayRequester,pvArray,pvRequest);
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
			return TestChannelProviderImpl.this;
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
			channelName.equals("valueOnly") ||
			channelName.equals("arrayDouble") ||
			channelName.equals("sum");
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
			retVal = new CounterTopStructure(0.0, timer);
		}
		else if (channelName.equals("valueOnly"))
		{
			retVal = new PVTopStructure(fieldCreate.createScalar(ScalarType.pvDouble));
		}
		else if (channelName.equals("sum"))
		{
			retVal = new RPCTopStructure();
		}
		else if (channelName.equals("arrayDouble"))
		{
			retVal = new PVTopStructure(fieldCreate.createScalarArray(ScalarType.pvDouble));
			PVDoubleArray pvArray = (PVDoubleArray)retVal.getPVStructure().getSubField("value");
		    final double[] ARRAY_VALUE = new double[] { 1.1, 2.2, 3.3, 4.4, 5.5 }; 
			pvArray.setCapacity(ARRAY_VALUE.length);
			pvArray.setLength(ARRAY_VALUE.length);
			pvArray.put(0, ARRAY_VALUE.length, ARRAY_VALUE, 0);
		}
		else
		{
			// default
			retVal =  new PVTopStructure(fieldCreate.createScalar(ScalarType.pvDouble));
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
		statusCreate.createStatus(StatusType.ERROR, "channel not found", null);

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
