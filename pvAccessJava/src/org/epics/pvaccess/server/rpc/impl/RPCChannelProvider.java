/*
 *
 */
package org.epics.pvaccess.server.rpc.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.ChannelFind;
import org.epics.pvaccess.client.ChannelFindRequester;
import org.epics.pvaccess.client.ChannelListRequester;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.server.rpc.Service;
import org.epics.pvaccess.util.WildcharMatcher;
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

	private final HashMap<String, Service> services = new HashMap<String, Service>();
	private final LinkedHashMap<String, Service> wildServices = new LinkedHashMap<String, Service>();
	private final ThreadPoolExecutor threadPool;

	public RPCChannelProvider(ThreadPoolExecutor threadPool) {
		this.threadPool = threadPool;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelProvider#getProviderName()
	 */
	public String getProviderName() {
		return PROVIDER_NAME;
	}

	private ChannelFind channelFind =
		new ChannelFind() {

			public ChannelProvider getChannelProvider() {
				return RPCChannelProvider.this;
			}

			public void cancel() {
				// noop
			}
		};

	// assumes synchronization on services
	private Service findWildService(String wildcard)
	{
		if (!wildServices.isEmpty())
			for (Map.Entry<String, Service> entry : wildServices.entrySet())
				if (WildcharMatcher.match(entry.getKey(), wildcard))
					return entry.getValue();

		return null;
	}

	// (too) simple check
	private boolean isWildcardPattern(String pattern)
	{
		return
		   (pattern.indexOf('*') != -1 ||
			pattern.indexOf('?') != -1 ||
			(pattern.indexOf('[') != -1 && pattern.indexOf(']') != -1));
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelProvider#channelFind(java.lang.String, org.epics.pvaccess.client.ChannelFindRequester)
	 */
	public ChannelFind channelFind(String channelName,
			ChannelFindRequester channelFindRequester) {
		boolean found;
		synchronized (services) {
			found = services.containsKey(channelName) ||
					(findWildService(channelName) != null);
		}
		channelFindRequester.channelFindResult(okStatus, channelFind, found);
		return channelFind;
	}

	public ChannelFind channelList(ChannelListRequester channelListRequester) {
		channelListRequester.channelListResult(okStatus, channelFind, services.keySet(), false);
		return channelFind;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelProvider#createChannel(java.lang.String, org.epics.pvaccess.client.ChannelRequester, short)
	 */
	public Channel createChannel(String channelName,
			ChannelRequester channelRequester, short priority)
	{

		Service service;
		synchronized (services) {
			service = services.get(channelName);
			if (service == null)
				service = findWildService(channelName);
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
	public Channel createChannel(String channelName,
			ChannelRequester channelRequester, short priority, String address) {
		// this will never get called by the pvAccess server
		throw new RuntimeException("not supported");
	}

	public void registerService(String serviceName, Service service)
	{
		synchronized (services) {
			services.put(serviceName, service);

			if (isWildcardPattern(serviceName))
				wildServices.put(serviceName, service);
		}

	}

	public void unregisterService(String serviceName)
	{
		synchronized (services) {
			services.remove(serviceName);
			wildServices.remove(serviceName);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelProvider#destroy()
	 */
	public void destroy() {
		// TODO destroy all channels

		synchronized (services) {
			services.clear();
			wildServices.clear();
		}
	}
}
