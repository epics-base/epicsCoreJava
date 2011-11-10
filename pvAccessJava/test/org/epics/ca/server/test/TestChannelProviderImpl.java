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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
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
import org.epics.ca.client.ChannelRequester;
import org.epics.ca.client.GetFieldRequester;
import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.factory.StatusFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.monitor.Monitor;
import org.epics.pvData.monitor.MonitorRequester;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVDataCreate;
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
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

    private static final Status okStatus = StatusFactory.getStatusCreate().getStatusOK();
	private static final Status fieldDoesNotExistStatus =
		StatusFactory.getStatusCreate().createStatus(StatusType.ERROR, "field does not exist", null);
	private static final Status destroyedStatus =
		StatusFactory.getStatusCreate().createStatus(StatusType.ERROR, "channel destroyed", null);

	class TestChannelImpl implements Channel
	{
		
		class TestChannelGetImpl implements ChannelGet
		{
			private final ChannelGetRequester channelGetRequester;
			private final Lock mutex = new ReentrantLock();
			private final AtomicBoolean destroyed = new AtomicBoolean();
			private final PVStructure pvGetStructure;
			private final BitSet bitSet;
			
			public TestChannelGetImpl(ChannelGetRequester channelGetRequester, PVStructure pvRequest)
			{
				this.channelGetRequester = channelGetRequester;
				
				pvGetStructure = pvStructure;
				bitSet = new BitSet(pvGetStructure.getNumberFields());
					
				channelGetRequester.channelGetConnect(okStatus, this, pvGetStructure, bitSet);
			}

			@Override
			public void lock() {
				mutex.lock();
			}

			@Override
			public void unlock() {
				mutex.unlock();
			}

			@Override
			public void get(boolean lastRequest) {
				if (destroyed.get())
				{
					channelGetRequester.getDone(destroyedStatus);
					return;
				}
				
				channelGetRequester.getDone(okStatus);

				if (lastRequest)
					destroy();
			}

			@Override
			public void destroy() {
				if (destroyed.getAndSet(true))
					return;
			}

		}
		
		
		
		
		private final String channelName;
		private final ChannelRequester channelRequester;
		private final PVStructure pvStructure;
		
		TestChannelImpl(String channelName, ChannelRequester channelRequester, ScalarType type)
		{
			this.channelName = channelName;
			this.channelRequester = channelRequester;
			
			
			
			
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
	        fields[0] = FieldFactory.getFieldCreate().createScalar("value", type);
	        fields[1] = timeStampStructure.getField();
	        fields[2] = alarmStructure.getField();
	        
	        pvStructure = pvDataCreate.createPVStructure(null, channelName, fields);
			
			
			setConnectionState(ConnectionState.CONNECTED);
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
				field = pvStructure.getStructure();
			else
				field = pvStructure.getStructure().getField(subField);
			
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

			return new TestChannelGetImpl(channelGetRequester, pvRequest);
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

	private ScalarType getType(String channelName)
	{
		if (channelName.equals("counter") ||
			channelName.equals("simpleCounter"))
			return ScalarType.pvInt;
		
		if (channelName.equals("valueOnly"))
				return ScalarType.pvDouble;
		
		return ScalarType.pvDouble;
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
				new TestChannelImpl(channelName, channelRequester, getType(channelName)) :
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