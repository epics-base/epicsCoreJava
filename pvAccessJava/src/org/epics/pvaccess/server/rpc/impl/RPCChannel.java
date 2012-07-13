/**
 * 
 */
package org.epics.pvaccess.server.rpc.impl;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.epics.pvaccess.client.AccessRights;
import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.ChannelArray;
import org.epics.pvaccess.client.ChannelArrayRequester;
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
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvaccess.server.rpc.RPCService;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.monitor.Monitor;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.StatusCreate;

/**
 * @author msekoranja
 *
 */
public class RPCChannel implements Channel {

	private static final StatusCreate statusCreate = StatusFactory.getStatusCreate();
	
	private static final Status notSupportedStatus = 
		statusCreate.createStatus(StatusType.ERROR, "only channelRPC requests are supported by this channel", null);
	private static final Status destroyedStatus = 
		statusCreate.createStatus(StatusType.ERROR, "channel destroyed", null);
	private static final Status okStatus = statusCreate.getStatusOK();
	
	private final AtomicBoolean destroyed = new AtomicBoolean(false);
	private final ArrayList<ChannelRPC> channelRPCRequests = new ArrayList<ChannelRPC>();
	
	private final ChannelProvider provider;
	private final String channelName;
	private final ChannelRequester channelRequester;
	
	private final RPCService rpcService;
	
	
	public RPCChannel(ChannelProvider provider, String channelName,
			ChannelRequester channelRequester, RPCService rpcService)
	{
		this.provider = provider;
		this.channelName = channelName;
		this.channelRequester = channelRequester;
		this.rpcService = rpcService;
	}

	@Override
	public ChannelProvider getProvider() {
		return provider;
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
	public String getRemoteAddress() {
		// local
		return getChannelName();
	}

	@Override
	public void destroy() {
		if (!destroyed.getAndSet(true))
		{
			// inverse order destruction
			synchronized (channelRPCRequests) {
				int size;
				while ((size = channelRPCRequests.size()) > 0)
					channelRPCRequests.get(size-1).destroy();
			}
		}
	}

	@Override
	public boolean isConnected() {
		// server-side implementation, always connected
		return !destroyed.get();
	}

	@Override
	public ConnectionState getConnectionState() {
		return isConnected() ?
				ConnectionState.CONNECTED :
				ConnectionState.DESTROYED;
	}

	
	private class ChannelRPCImpl implements ChannelRPC
	{
		private final ChannelRPCRequester channelRPCRequester;

		
		public ChannelRPCImpl(ChannelRPCRequester channelRPCRequester) {
			this.channelRPCRequester = channelRPCRequester;
			
			// add to the list, careful: "this" in the constructor
			synchronized (channelRPCRequests) {
				channelRPCRequests.add(this);
			}
		}

		@Override
		public void request(PVStructure pvArgument, boolean lastRequest) {
			// TODO now this is sync
			
			PVStructure result = null;
			Status status = okStatus;
			try
			{
				result = rpcService.request(pvArgument);
			}
			catch (RPCRequestException rre)
			{
				status =
					statusCreate.createStatus(
						rre.getStatus(),
						rre.getMessage(),
						rre);
			}
			catch (Throwable th)
			{
				// handle user unexpected errors
				status = 
					statusCreate.createStatus(StatusType.FATAL,
								"Unexpected exception caught while calling RPCService.request(PVStructure).",
								th);
			}
		
			// check null result
			if (result == null)
			{
				status =
					statusCreate.createStatus(
							StatusType.FATAL,
							"RPCService.request(PVStructure) returned null.",
							null);
			}
			
			channelRPCRequester.requestDone(status, result);
		}
		
		@Override
		public void destroy() {
			// remove from the list
			synchronized (channelRPCRequests) {
				channelRPCRequests.remove(this);
			}
		}

		@Override
		public void lock() {
			// noop
		}

		@Override
		public void unlock() {
			// noop
		}
	}
	
	@Override
	public ChannelRPC createChannelRPC(ChannelRPCRequester channelRPCRequester,
			PVStructure pvRequest) {
		
		// nothing expected to be in pvRequest
		
		if (channelRPCRequester == null)
			throw new IllegalArgumentException("channelRPCRequester == null");

		if (destroyed.get())
		{
			channelRPCRequester.channelRPCConnect(destroyedStatus, null);
			return null;
		}
		
		ChannelRPCImpl channelRPCImpl = new ChannelRPCImpl(channelRPCRequester);
		channelRPCRequester.channelRPCConnect(okStatus, channelRPCImpl);
		return channelRPCImpl;
	}
	
	
	
	@Override
	public AccessRights getAccessRights(PVField pvField) {
		return AccessRights.none;
	}

	@Override
	public void getField(GetFieldRequester requester, String subField) {
		requester.getDone(notSupportedStatus, null);
	}

	@Override
	public ChannelProcess createChannelProcess(
			ChannelProcessRequester channelProcessRequester,
			PVStructure pvRequest) {
		channelProcessRequester.channelProcessConnect(notSupportedStatus, null);
		return null;
	}

	@Override
	public ChannelGet createChannelGet(ChannelGetRequester channelGetRequester,
			PVStructure pvRequest) {
		channelGetRequester.channelGetConnect(notSupportedStatus, null, null, null);
		return null;
	}

	@Override
	public ChannelPut createChannelPut(ChannelPutRequester channelPutRequester,
			PVStructure pvRequest) {
		channelPutRequester.channelPutConnect(notSupportedStatus, null, null, null);
		return null;
	}

	@Override
	public ChannelPutGet createChannelPutGet(
			ChannelPutGetRequester channelPutGetRequester, PVStructure pvRequest) {
		channelPutGetRequester.channelPutGetConnect(notSupportedStatus, null, null, null);
		return null;
	}

	@Override
	public Monitor createMonitor(MonitorRequester monitorRequester,
			PVStructure pvRequest) {
		monitorRequester.monitorConnect(notSupportedStatus, null, null);
		return null;
	}

	@Override
	public ChannelArray createChannelArray(
			ChannelArrayRequester channelArrayRequester, PVStructure pvRequest) {
		channelArrayRequester.channelArrayConnect(notSupportedStatus, null, null);
		return null;
	}

	@Override
	public String getRequesterName() {
		return getChannelName();
	}

	@Override
	public void message(String message, MessageType messageType) {
		// just delegate
		channelRequester.message(message, messageType);
	}

	
}
