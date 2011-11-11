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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

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
import org.epics.ca.server.test.helpers.CounterTopStructure;
import org.epics.ca.server.test.helpers.Mapper;
import org.epics.ca.server.test.helpers.PVRequestUtils;
import org.epics.ca.server.test.helpers.PVTopStructure;
import org.epics.ca.server.test.helpers.PVTopStructure.PVTopStructureListener;
import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.StatusFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.misc.ThreadPriority;
import org.epics.pvData.misc.Timer;
import org.epics.pvData.misc.TimerFactory;
import org.epics.pvData.monitor.Monitor;
import org.epics.pvData.monitor.MonitorRequester;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.Status.StatusType;

/**
 * Implementation of a channel provider for tests.
 * @author msekoranja
 */
public class TestChannelProviderImpl implements ChannelProvider
{
    private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();

    private static final Status okStatus = StatusFactory.getStatusCreate().getStatusOK();
	private static final Status fieldDoesNotExistStatus =
		StatusFactory.getStatusCreate().createStatus(StatusType.ERROR, "field does not exist", null);
	private static final Status destroyedStatus =
		StatusFactory.getStatusCreate().createStatus(StatusType.ERROR, "channel destroyed", null);


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
				mapper = new Mapper(pvTopStructure.getPVStructure(), pvRequest);
				
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
			
			if (channelPutRequester == null)
				throw new IllegalArgumentException("channelGetRequester");
			
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