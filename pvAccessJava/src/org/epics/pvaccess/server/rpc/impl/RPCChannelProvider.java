/**
 * 
 */
package org.epics.pvaccess.server.rpc.impl;

import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.ChannelFind;
import org.epics.pvaccess.client.ChannelFindRequester;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.server.rpc.RPCService;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.StatusCreate;

/**
 * @author msekoranja
 *
 */
public class RPCChannelProvider implements ChannelProvider {

	public static final String PROVIDER_NAME = "rpcService";

	private static final StatusCreate statusCreate = StatusFactory.getStatusCreate();
	private static final Status okStatus = statusCreate.getStatusOK();
	private static final Status noSuchChannelStatus =
		statusCreate.createStatus(StatusType.ERROR, "no such channel", null);
	
	private final HashMap<String, RPCService> services = new HashMap<String, RPCService>();
	private final ThreadPoolExecutor threadPool;
	
	public RPCChannelProvider(ThreadPoolExecutor threadPool) {
		this.threadPool = threadPool;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelProvider#getProviderName()
	 */
	@Override
	public String getProviderName() {
		return PROVIDER_NAME;
	}

	private ChannelFind channelFind =
		new ChannelFind() {
			
			@Override
			public ChannelProvider getChannelProvider() {
				return RPCChannelProvider.this;
			}
			
			@Override
			public void cancel() {
				// noop
			}
		};
	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelProvider#channelFind(java.lang.String, org.epics.pvaccess.client.ChannelFindRequester)
	 */
	@Override
	public ChannelFind channelFind(String channelName,
			ChannelFindRequester channelFindRequester) {
		boolean found;
		synchronized (services) {
			found = services.containsKey(channelName);
		}
		channelFindRequester.channelFindResult(okStatus, channelFind, found);
		return channelFind;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelProvider#createChannel(java.lang.String, org.epics.pvaccess.client.ChannelRequester, short)
	 */
	@Override
	public Channel createChannel(String channelName,
			ChannelRequester channelRequester, short priority)
	{
		
		RPCService service;
		synchronized (services) {
			service = services.get(channelName);
		}
		
		if (service == null)
		{
			channelRequester.channelCreated(noSuchChannelStatus, null);
			return null;
		}
			
		RPCChannel rpcChannel = new RPCChannel(
				this,
				channelName,
				channelRequester,
				service,
				threadPool);
		channelRequester.channelCreated(okStatus, rpcChannel);
		return rpcChannel;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelProvider#createChannel(java.lang.String, org.epics.pvaccess.client.ChannelRequester, short, java.lang.String)
	 */
	@Override
	public Channel createChannel(String channelName,
			ChannelRequester channelRequester, short priority, String address) {
		// this will never get called by the pvAccess server
		throw new RuntimeException("not supported");
	}

	public void registerService(String serviceName, RPCService service)
	{
		synchronized (services) {
			services.put(serviceName, service);
		}
	}
	
	public void unregisterService(String serviceName)
	{
		synchronized (services) {
			services.remove(serviceName);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelProvider#destroy()
	 */
	@Override
	public void destroy() {
		// TODO destroy all channels

		synchronized (services) {
			services.clear();
		}
	}
}
