package org.epics.pvaccess.server.rpc;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.epics.pvaccess.CAException;
import org.epics.pvaccess.client.ChannelAccessFactory;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvaccess.server.impl.remote.plugins.DefaultBeaconServerDataProvider;
import org.epics.pvaccess.server.rpc.impl.RPCChannelProvider;

public class RPCServer {

	private final ServerContextImpl serverContext;
	private final RPCChannelProvider channelProviderImpl;
	
	private final ThreadPoolExecutor threadPoll;

	public RPCServer()
	{
		// sync processing of RPC requests
		this(0, 1);
	}
	
	public RPCServer(int threads, int queueSize)
	{
		if (threads < 0)
			throw new IllegalArgumentException("threads < 0");
		
		if (threads > 0 && queueSize < 1)
			throw new IllegalArgumentException("queueSize < 1");
		
		if (threads > 0)
		{
			threadPoll = new ThreadPoolExecutor(threads, threads,
												0, TimeUnit.SECONDS,
												new ArrayBlockingQueue<Runnable>(queueSize));
			threadPoll.prestartAllCoreThreads();
		}
		else
			threadPoll = null;		// sync processing

		channelProviderImpl = new RPCChannelProvider(threadPoll);
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
		if (threadPoll == null)
			serverContext.destroy();
		else
		{
			// notify to shutdown and do not accept any new requests
			threadPoll.shutdown();
			serverContext.destroy();
			threadPoll.shutdownNow();
		}
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
