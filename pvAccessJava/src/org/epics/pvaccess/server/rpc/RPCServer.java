package org.epics.pvaccess.server.rpc;

import org.epics.pvaccess.CAException;
import org.epics.pvaccess.client.ChannelAccessFactory;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvaccess.server.impl.remote.plugins.DefaultBeaconServerDataProvider;
import org.epics.pvaccess.server.rpc.impl.RPCChannelProvider;

public class RPCServer {

	private final ServerContextImpl serverContext;
	private final RPCChannelProvider channelProviderImpl;
	
	public RPCServer()
	{
		channelProviderImpl = new RPCChannelProvider();
		ChannelAccessFactory.registerChannelProvider(channelProviderImpl);
		System.setProperty("EPICS4_CAS_PROVIDER_NAME", channelProviderImpl.getProviderName());

		serverContext = new ServerContextImpl();
		serverContext.setBeaconServerStatusProvider(new DefaultBeaconServerDataProvider(serverContext));

		try {
			serverContext.initialize(ChannelAccessFactory.getChannelAccess());
		} catch (Throwable th) {
			throw new RuntimeException("Failed to initialize pvAccess RPC server.", th);
		}
	}

	/**
	 * Display basic information about the context.
	 */
	public void printInfo()
	{
        System.out.println(serverContext.getVersion().getVersionString());
        serverContext.printInfo();
	}

	public void run(int seconds) throws CAException
	{
		serverContext.run(seconds);
	}
	
	public void destroy() throws CAException
	{
		serverContext.destroy();
	}
	
	public void registerService(String serviceName, RPCService service)
	{
		channelProviderImpl.registerService(serviceName, service);
	}
	
	public void unregisterService(String serviceName)
	{
		channelProviderImpl.unregisterService(serviceName);
	}
	
}
