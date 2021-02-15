/*
 *
 */
package org.epics.pvaccess.server.rpc.impl;

import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;
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
import org.epics.pvaccess.server.rpc.RPCResponseCallback;
import org.epics.pvaccess.server.rpc.RPCService;
import org.epics.pvaccess.server.rpc.RPCServiceAsync;
import org.epics.pvaccess.server.rpc.Service;
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

	private final Service service;
	private final ThreadPoolExecutor threadPool;


	public RPCChannel(ChannelProvider provider, String channelName,
			ChannelRequester channelRequester, Service service,
			ThreadPoolExecutor threadPool)
	{
		this.provider = provider;
		this.channelName = channelName;
		this.channelRequester = channelRequester;
		this.service = service;
		this.threadPool = threadPool;
	}

	public ChannelProvider getProvider() {
		return provider;
	}

	public String getChannelName() {
		return channelName;
	}

	public ChannelRequester getChannelRequester() {
		return channelRequester;
	}

	public String getRemoteAddress() {
		// local
		return getChannelName();
	}

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

	public boolean isConnected() {
		// server-side implementation, always connected
		return !destroyed.get();
	}

	public ConnectionState getConnectionState() {
		return isConnected() ?
				ConnectionState.CONNECTED :
				ConnectionState.DESTROYED;
	}


	private class ChannelRPCImpl implements ChannelRPC, RPCResponseCallback
	{
		private final ChannelRPCRequester channelRPCRequester;
		private final Channel channel;
		private volatile boolean lastRequest = false;


		public ChannelRPCImpl(Channel channel, ChannelRPCRequester channelRPCRequester) {
			this.channel = channel;
			this.channelRPCRequester = channelRPCRequester;

			// add to the list, careful: "this" in the constructor
			synchronized (channelRPCRequests) {
				channelRPCRequests.add(this);
			}
		}

		public void lastRequest()
		{
			lastRequest = true;
		}

		public Channel getChannel()
		{
			return channel;
		}

		private void processRequest(RPCService rpcService, PVStructure pvArgument)
		{
			PVStructure result = null;
			Status status = okStatus;
			boolean ok = true;
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
				ok = false;
			}
			catch (Throwable th)
			{
				// handle user unexpected errors
				status =
					statusCreate.createStatus(StatusType.FATAL,
								"Unexpected exception caught while calling RPCService.request(PVStructure).",
								th);
				ok = false;
			}

			// check null result
			if (ok && result == null)
			{
				status =
					statusCreate.createStatus(
							StatusType.FATAL,
							"RPCService.request(PVStructure) returned null.",
							null);
			}

			channelRPCRequester.requestDone(status, this, result);

			if (lastRequest)
				destroy();
		}

		public void requestDone(Status status, PVStructure result) {
			channelRPCRequester.requestDone(status, this, result);

			if (lastRequest)
				destroy();
		}

		private void processRequest(RPCServiceAsync rpcServiceAsync, PVStructure pvArgument)
		{
			try
			{
				rpcServiceAsync.request(pvArgument, this);
			}
			catch (Throwable th)
			{
				// handle user unexpected errors
				Status status =
					statusCreate.createStatus(StatusType.FATAL,
								"Unexpected exception caught while calling RPCService.request(PVStructure).",
								th);

				channelRPCRequester.requestDone(status, this, null);

				if (lastRequest)
					destroy();
			}

			// we wait for callback to be called
		}

		public void request(final PVStructure pvArgument) {

			if (service instanceof RPCService)
			{
				final RPCService rpcService = (RPCService)service;

				if (threadPool == null)
					processRequest(rpcService, pvArgument);
				else
				{
					threadPool.execute(new Runnable() {
						public void run() {
							processRequest(rpcService, pvArgument);
						}
					});
				}
			}
			else if (service instanceof RPCServiceAsync)
			{
				final RPCServiceAsync rpcServiceAsync = (RPCServiceAsync)service;
				processRequest(rpcServiceAsync, pvArgument);
			}
			else
				throw new RuntimeException("unsupported Service type");
		}

		public void destroy() {
			// remove from the list
			synchronized (channelRPCRequests) {
				channelRPCRequests.remove(this);
			}
		}

		public void lock() {
			// noop
		}

		public void unlock() {
			// noop
		}

		public void cancel() {
			// TODO do we need to extend API?
		}
	}

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

		ChannelRPCImpl channelRPCImpl = new ChannelRPCImpl(this, channelRPCRequester);
		channelRPCRequester.channelRPCConnect(okStatus, channelRPCImpl);
		return channelRPCImpl;
	}



	public AccessRights getAccessRights(PVField pvField) {
		return AccessRights.none;
	}

	public void getField(GetFieldRequester requester, String subField) {
		requester.getDone(notSupportedStatus, null);
	}

	public ChannelProcess createChannelProcess(
			ChannelProcessRequester channelProcessRequester,
			PVStructure pvRequest) {
		channelProcessRequester.channelProcessConnect(notSupportedStatus, null);
		return null;
	}

	public ChannelGet createChannelGet(ChannelGetRequester channelGetRequester,
			PVStructure pvRequest) {
		channelGetRequester.channelGetConnect(notSupportedStatus, null, null);
		return null;
	}

	public ChannelPut createChannelPut(ChannelPutRequester channelPutRequester,
			PVStructure pvRequest) {
		channelPutRequester.channelPutConnect(notSupportedStatus, null, null);
		return null;
	}

	public ChannelPutGet createChannelPutGet(
			ChannelPutGetRequester channelPutGetRequester, PVStructure pvRequest) {
		channelPutGetRequester.channelPutGetConnect(notSupportedStatus, null, null, null);
		return null;
	}

	public Monitor createMonitor(MonitorRequester monitorRequester,
			PVStructure pvRequest) {
		monitorRequester.monitorConnect(notSupportedStatus, null, null);
		return null;
	}

	public ChannelArray createChannelArray(
			ChannelArrayRequester channelArrayRequester, PVStructure pvRequest) {
		channelArrayRequester.channelArrayConnect(notSupportedStatus, null, null);
		return null;
	}

	public String getRequesterName() {
		return getChannelName();
	}

	public void message(String message, MessageType messageType) {
		// just delegate
		channelRequester.message(message, messageType);
	}

}
