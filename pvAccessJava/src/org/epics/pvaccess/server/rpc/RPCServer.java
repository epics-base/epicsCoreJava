package org.epics.pvaccess.server.rpc;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.epics.pvaccess.CAException;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvaccess.server.impl.remote.plugins.DefaultBeaconServerDataProvider;
import org.epics.pvaccess.server.rpc.impl.RPCChannelProvider;

/**
 * pvAccess RPC server implementation.
 * @author msekoranja
 */
public class RPCServer {

	private final ServerContextImpl serverContext;
	private final RPCChannelProvider channelProviderImpl;
	
	private final ThreadPoolExecutor threadPoll;

	/**
	 * Default constructor.
	 * Creates a simple RPC server that processes requests directly in pvAccess receive thread.
	 */
	public RPCServer()
	{
		// sync processing of RPC requests
		this(0, 1);
	}
	
	/**
	 * Creates a RPC server with a thread-pool used to process requests.
	 * @param threads number of threads in a thread-pool.
	 * @param queueSize thread-pool request queue size.
	 */
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

		serverContext = new ServerContextImpl();
		serverContext.setBeaconServerStatusProvider(new DefaultBeaconServerDataProvider(serverContext));

		try {
			serverContext.initialize(channelProviderImpl);
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

	/**
	 * Run the server for a given amount of time.
	 * @param seconds time (in seconds) to run the server, if <code>0</code> server is run until destroyed.
	 * @throws CAException exception thrown in case of an unexpected error.
	 */
	public void run(int seconds) throws CAException
	{
		serverContext.run(seconds);
	}
	
	/**
	 * Destroy (shutdown) the server.
	 * @throws CAException exception thrown in case of an unexpected error.
	 */
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
	
	/**
	 * Register RPC service.
	 * Multiple services (with different name) can be registered.
	 * In case of name duplicates, the last registered service (with the same name) is used.
	 * @param serviceName RPC service name. This name is used by client to discover/connect to the service.
	 * @param service service implementation.
	 */
	public void registerService(String serviceName, RPCService service)
	{
		channelProviderImpl.registerService(serviceName, service);
	}

	/**
	 * Unregister RPC service.
	 * @param serviceName name of the RPC service to be unregistered.
	 */
	public void unregisterService(String serviceName)
	{
		channelProviderImpl.unregisterService(serviceName);
	}
	
}
