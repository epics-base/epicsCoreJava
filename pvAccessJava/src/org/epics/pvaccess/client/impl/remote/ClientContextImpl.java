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

package org.epics.pvaccess.client.impl.remote;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.PVAVersion;
import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.Version;
import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.ChannelFind;
import org.epics.pvaccess.client.ChannelFindRequester;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.client.impl.remote.search.ChannelSearchManager;
import org.epics.pvaccess.client.impl.remote.search.SearchInstance;
import org.epics.pvaccess.client.impl.remote.search.SimpleChannelSearchManagerImpl;
import org.epics.pvaccess.client.impl.remote.tcp.BlockingClientTCPTransport;
import org.epics.pvaccess.client.impl.remote.tcp.BlockingTCPConnector;
import org.epics.pvaccess.client.impl.remote.tcp.BlockingTCPConnector.TransportFactory;
import org.epics.pvaccess.client.impl.remote.tcp.NonBlockingClientTCPTransport;
import org.epics.pvaccess.impl.remote.ConnectionException;
import org.epics.pvaccess.impl.remote.Context;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportClient;
import org.epics.pvaccess.impl.remote.TransportRegistry;
import org.epics.pvaccess.impl.remote.io.impl.PollerImpl;
import org.epics.pvaccess.impl.remote.request.ResponseHandler;
import org.epics.pvaccess.impl.remote.request.ResponseRequest;
import org.epics.pvaccess.impl.remote.udp.BlockingUDPConnector;
import org.epics.pvaccess.impl.remote.udp.BlockingUDPTransport;
import org.epics.pvaccess.util.InetAddressUtil;
import org.epics.pvaccess.util.IntHashMap;
import org.epics.pvaccess.util.configuration.Configuration;
import org.epics.pvaccess.util.configuration.ConfigurationProvider;
import org.epics.pvaccess.util.configuration.impl.ConfigurationFactory;
import org.epics.pvaccess.util.logging.ConsoleLogHandler;
import org.epics.pvaccess.util.sync.NamedLockPattern;
import org.epics.pvdata.misc.ThreadPriority;
import org.epics.pvdata.misc.Timer;
import org.epics.pvdata.misc.TimerFactory;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.StatusCreate;

/**
 * Implementation of PVAJ JCA <code>Context</code>. 
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ClientContextImpl implements Context/*, Configurable*/ {

	/**
	 * Name if the provider this context provides.
	 */
	public static final String PROVIDER_NAME = "pva";
	
    /**
     * Version.
     */
    public static final Version VERSION = new Version(
            "pvAccess Client", "Java",
            PVAVersion.VERSION_MAJOR, PVAVersion.VERSION_MINOR,
            PVAVersion.VERSION_MAINTENANCE, PVAVersion.VERSION_DEVELOPMENT);
	  
    /**
     * Server state enum.
     */
    enum State {
		/**
		 * State value of non-initialized context.
		 */
		NOT_INITIALIZED,
	
		/**
		 * State value of initialized context.
		 */
		INITIALIZED,
	
		/**
		 * State value of destroyed context.
		 */
		DESTROYED;
    }
    
	/**
	 * Initialization status.
	 */
	private volatile State state = State.NOT_INITIALIZED;
	
	/**
	 * Context logger.
	 */
	protected Logger logger;

	/**
	 * A space-separated list of broadcast address for process variable name resolution.
	 * Each address must be of the form: ip.number:port or host.name:port
	 */
	protected String addressList = "";
	
	/**
	 * Define whether or not the network interfaces should be discovered at runtime. 
	 */
	protected boolean autoAddressList = true;
	
	/**
	 * If the context doesn't see a beacon from a server that it is connected to for
	 * connectionTimeout seconds then a state-of-health message is sent to the server over TCP/IP.
	 * If this state-of-health message isn't promptly replied to then the context will assume that
	 * the server is no longer present on the network and disconnect.
	 */
	protected float connectionTimeout = 30.0f;
	
	/**
	 * Period in second between two beacon signals.
	 */
	protected float beaconPeriod = 15.0f;
	
	/**
	 * Broadcast (beacon, search) port number to listen to.
	 */
	protected int broadcastPort = PVAConstants.PVA_BROADCAST_PORT;
	
	/**
	 * Receive buffer size (max size of payload).
	 */
	protected int receiveBufferSize = PVAConstants.MAX_TCP_RECV;
	
	/**
	 * Timer.
	 */
	protected Timer timer = null;

	/**
	 * Reactor.
	 */
	//protected Reactor reactor = null;

	/**
	 * Leader/followers thread pool.
	 */
	//protected LeaderFollowersThreadPool leaderFollowersThreadPool = null;

	/**
	 * Broadcast transport needed to listen for broadcasts.
	 */
//	protected UDPTransport broadcastTransport = null;
	protected BlockingUDPTransport broadcastTransport = null;
	
	/**
	 * UDP transport needed for channel searches.
	 */
//	protected UDPTransport searchTransport = null;
	protected BlockingUDPTransport searchTransport = null;

	/**
	 * PVA connector (creates PVA virtual circuit).
	 */
	protected BlockingTCPConnector connector = null;
//	protected TCPConnector connector = null;

	/**
	 * PVA transport (virtual circuit) registry.
	 * This registry contains all active transports - connections to PVA servers. 
	 */
	protected TransportRegistry transportRegistry = null;

	/**
	 * Context instance.
	 */
	private NamedLockPattern namedLocker;

	/**
	 * Context instance.
	 */
	private static final int LOCK_TIMEOUT = 20 * 1000;	// 20s

	/**
	 * Map of channels (keys are CIDs).
	 */
	// TODO consider using WeakHashMap (and call Channel.destroy() in finalize() method).
	protected final IntHashMap channelsByCID = new IntHashMap();

	/**
	 * Map of channels (keys are names).
	 */
	// TODO consider using WeakHashMap (and call Channel.destroy() in finalize() method).
	//protected final Map<String, ChannelImpl> channelsByName = new HashMap<String, ChannelImpl>();

	/**
	 * Last CID cache. 
	 */
	private int lastCID = 0;

	/**
	 * Map of pending response requests (keys are IOID).
	 */
	// TODO consider using WeakHashMap (and call ResponseRequest.destroy() in finalize() method).
	protected final IntHashMap pendingResponseRequests = new IntHashMap();

	/**
	 * Last IOID cache. 
	 */
	private int lastIOID = 0;

	/**
	 * Channel search manager.
	 * Manages UDP search requests.
	 */
	private ChannelSearchManager channelSearchManager;

	/**
	 * Beacon handler map.
	 */
	protected final Map<InetSocketAddress, BeaconHandlerImpl> beaconHandlers = new HashMap<InetSocketAddress, BeaconHandlerImpl>();

	/**
	 * Response handler.
	 */
	private final ResponseHandler clientResponseHandler;
	
	/**
	 * Provider implementation.
	 */
	protected ChannelProvider channelProvider = new ChannelProviderImpl();
	
	/**
	 * Constructor.
	 */
	public ClientContextImpl()
	{
		initializeLogger();
		loadConfiguration();
		
		clientResponseHandler = new ClientResponseHandler(this);
	}
	
    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.ClientContext#getVersion()
     */
    public Version getVersion()
    {
        return VERSION;
    }
    
	/**
	 * Initialize context logger.
	 */
	protected void initializeLogger()
	{
		String thisClassName = this.getClass().getName();
		String loggerName = thisClassName;
		logger = Logger.getLogger(loggerName);
		
		// TODO use config
		if (Integer.getInteger(PVAConstants.PVACCESS_DEBUG) > 0)
		{
			logger.setLevel(Level.ALL);
			boolean found = false;
			for (Handler handler : logger.getHandlers())
				if (handler instanceof ConsoleLogHandler)
				{
					found = true;
					break;
				}
			if (!found)
				logger.addHandler(new ConsoleLogHandler());
		}
	}
	
	/**
	 * Get configuration instance.
	 */
	public Configuration getConfiguration()
	{
		final ConfigurationProvider configurationProvider = ConfigurationFactory.getProvider();
		Configuration config = configurationProvider.getConfiguration("pvAccess-client");
		if (config == null)
			config = configurationProvider.getConfiguration("system");
		return config;
	}

	/**
	 * Load configuration.
	 */
	protected void loadConfiguration()
	{
		final Configuration config = getConfiguration();
		
		addressList = config.getPropertyAsString("EPICS_PVA_ADDR_LIST", addressList);
		autoAddressList = config.getPropertyAsBoolean("EPICS_PVA_AUTO_ADDR_LIST", autoAddressList);
		connectionTimeout = config.getPropertyAsFloat("EPICS_PVA_CONN_TMO", connectionTimeout);
		beaconPeriod = config.getPropertyAsFloat("EPICS_PVA_BEACON_PERIOD", beaconPeriod);
		broadcastPort = config.getPropertyAsInteger("EPICS_PVA_BROADCAST_PORT", broadcastPort);
		receiveBufferSize = config.getPropertyAsInteger("EPICS_PVA_MAX_ARRAY_BYTES", receiveBufferSize);
	}

	/**
	 * Check context state and tries to establish necessary state.
	 * @throws PVAException
	 * @throws IllegalStateException
	 */
	protected void checkState() throws PVAException, IllegalStateException {
		if (state == State.DESTROYED)
			throw new IllegalStateException("Context destroyed.");
		else if (state == State.NOT_INITIALIZED)
		{
			// double-locking pattern used to prevent unnecessary initialization calls 
			synchronized (this)
			{
				if (state == State.NOT_INITIALIZED)
					initialize();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ClientContext#initialize()
	 */
	public synchronized void initialize() throws PVAException {
		
		if (state == State.DESTROYED)
			throw new IllegalStateException("Context destroyed.");
		else if (state == State.INITIALIZED)
			throw new IllegalStateException("Context already initialized.");
		
		//super.initialize();
		
		internalInitialize();
		
		state = State.INITIALIZED;
		
	}

	// TODO remove
	final AtomicBoolean pollerInitialized = new AtomicBoolean();
	PollerImpl poller;
	
	/**
	 * @throws PVAException
	 */
	private void internalInitialize() throws PVAException {
		
		timer = TimerFactory.create("pvAccess-client timer", ThreadPriority.lower);
//		connector = new TCPConnector(this, receiveBufferSize, beaconPeriod);
		
		TransportFactory transportFactory = new TransportFactory() {
			
			@Override
			public Transport create(Context context, SocketChannel channel,
					ResponseHandler responseHandler, int receiveBufferSize,
					TransportClient client, short transportRevision,
					float heartbeatInterval, short priority) {
				try {
					return new BlockingClientTCPTransport(context, channel, responseHandler, receiveBufferSize, client, transportRevision, heartbeatInterval, priority);
				} catch (SocketException e) {
					throw new RuntimeException("Failed to create transport.");
				}
			}
		};
		
		// TODO not used yet
		@SuppressWarnings("unused")
		TransportFactory nonBlockingTransportFactory = new TransportFactory() {
			
			@Override
			public Transport create(Context context, SocketChannel channel,
					ResponseHandler responseHandler, int receiveBufferSize,
					TransportClient client, short transportRevision,
					float heartbeatInterval, short priority) {
				try {
					// TODO !!!
					if (!pollerInitialized.getAndSet(true))
					{
						poller = new PollerImpl();
						poller.start();
					}
					return new NonBlockingClientTCPTransport(context, poller, channel, responseHandler, receiveBufferSize, client, transportRevision, heartbeatInterval, priority);
				} catch (IOException e) {
					throw new RuntimeException("Failed to create transport.");
				}
			}
		};

		connector = new BlockingTCPConnector(this, transportFactory, receiveBufferSize, beaconPeriod);
		//connector = new BlockingTCPConnector(this, nonBlockingTransportFactory, receiveBufferSize, beaconPeriod);
		transportRegistry = new TransportRegistry();
		namedLocker = new NamedLockPattern();
/*
		try
		{
			reactor = new Reactor();
			
			if (System.getProperties().containsKey(CAJ_SINGLE_THREADED_MODEL))
			{
			    logger.config("Using single threaded model.");
			    
				// single thread processing
				new Thread(
				        new Runnable() {
				            /**
				        	 * @see java.lang.Runnable#run()
				        	 *
				        	public void run() {
				        		// do the work
				        		while (reactor.process());
				        	}
				        	
				        }, "CA reactor").start();
			}
			else
			{
			    // leader/followers processing
			    leaderFollowersThreadPool = new LeaderFollowersThreadPool();
				// spawn initial leader
				leaderFollowersThreadPool.promoteLeader(
				        new Runnable() {
				            /**
				        	 * @see java.lang.Runnable#run()
				        	 *
				        	public void run() {
				        		reactor.process();
				        	}
						}
				);
			}
			
		}
		catch (IOException ioex)
		{
			throw new PVAException("Failed to initialize reactor.", ioex); 
		}
		*/
		
		// setup UDP transport
		initializeUDPTransport();

		// setup search manager
		// TODO
//		channelSearchManager = new ChannelSearchManagerImpl(this);
		channelSearchManager = new SimpleChannelSearchManagerImpl(this);
	}

	/**
	 * Initialized UDP transport (broadcast socket and repeater connection).
	 */
	private void initializeUDPTransport() {
		// setup UDP transport
		try
		{
			// where to bind (listen) address
			InetSocketAddress listenLocalAddress = new InetSocketAddress(broadcastPort);
		
			// where to send address
			InetSocketAddress[] broadcastAddresses = InetAddressUtil.getBroadcastAddresses(broadcastPort);

//			UDPConnector broadcastConnector = new UDPConnector(this, true, broadcastAddresses, true);
			BlockingUDPConnector broadcastConnector = new BlockingUDPConnector(this, true, broadcastAddresses, true);
			
			broadcastTransport = (BlockingUDPTransport)broadcastConnector.connect(
//			broadcastTransport = (UDPTransport)broadcastConnector.connect(
						null, new ClientResponseHandler(this),
						listenLocalAddress, PVAConstants.PVA_PROTOCOL_REVISION,
						PVAConstants.PVA_DEFAULT_PRIORITY);

//			UDPConnector searchConnector = new UDPConnector(this, false, broadcastAddresses, true);
			BlockingUDPConnector searchConnector = new BlockingUDPConnector(this, false, broadcastAddresses, true);
			
			searchTransport = (BlockingUDPTransport)searchConnector.connect(
//			searchTransport = (UDPTransport)searchConnector.connect(
										null, new ClientResponseHandler(this),
										new InetSocketAddress(0), PVAConstants.PVA_PROTOCOL_REVISION,
										PVAConstants.PVA_DEFAULT_PRIORITY);

			// set broadcast address list
			if (addressList != null && addressList.length() > 0)
			{
				// if auto is true, add it to specified list
				InetSocketAddress[] appendList = null;
				if (autoAddressList == true)
					appendList = broadcastTransport.getSendAddresses();
				
				InetSocketAddress[] list = InetAddressUtil.getSocketAddressList(addressList, broadcastPort, appendList);
				if (list != null && list.length > 0) {
					broadcastTransport.setBroadcastAddresses(list);
					searchTransport.setBroadcastAddresses(list);
				}
			}
			
			broadcastTransport.start();
			searchTransport.start();

		}
		catch (ConnectionException ce)
		{
			logger.log(Level.SEVERE, "Failed to initialize UDP transport.", ce);
		}
	}
	 
	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ClientContext#destroy()
	 */
	public synchronized void destroy() {

		if (state == State.DESTROYED)
			throw new IllegalStateException("Context already destroyed.");

		// go into destroyed state ASAP			
		state =  State.DESTROYED;
		
		internalDestroy();
				
	}

	/**
	 * @throws PVAException
	 */
	private void internalDestroy() {

		// stop searching
		if (channelSearchManager != null)
			channelSearchManager.cancel();

		// stop timer
		if (timer != null) 
			timer.stop();
		 
		//
		// cleanup
		//
		
		// this will also close all PVA transports
		destroyAllChannels();
		
		// close broadcast transport
		if (broadcastTransport != null)
		{
			try {
				broadcastTransport.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (searchTransport != null)
		{
			try {
				searchTransport.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	/*	
		// shutdown reactor
		if (reactor != null)
			reactor.shutdown();
		
		// shutdown LF thread pool
		if (leaderFollowersThreadPool != null)
		    leaderFollowersThreadPool.shutdown();
		*/
		// TODO still some events can be in queue (e.g. channel destroyed)
		// reposibility of the event dispatcher?
		// shutdown dispatcher
		//if (eventDispatcher != null)
		//	eventDispatcher.dispose();
		
	}

	/**
	 * Destroy all channels.
	 */
	private void destroyAllChannels() {
		
		Channel[] channelsArray;
		synchronized (channelsByCID)
		{
			// not some elements might be null
			Channel[] ch = new Channel[channelsByCID.size()];
			channelsArray = (Channel[])channelsByCID.toArray(ch);
			channelsByCID.clear();
			//channelsByName.clear();
		}
		
		for (int i = 0; i < channelsArray.length; i++)
		{
			try
			{
				// force destruction regardless of reference count
				final ChannelImpl channel = (ChannelImpl)channelsArray[i];
				if (channel != null)
					channel.destroy(true);
			}
			catch (Throwable th)
			{
				th.printStackTrace();
			}
		}
	}

	/**
	 * Check channel name.
	 * @param name name to check.
	 * @throws IllegalArgumentException
	 */
	private final void checkChannelName(String name) throws IllegalArgumentException {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("null or empty channel name");
		else if (name.length() > PVAConstants.MAX_CHANNEL_NAME_LENGTH)
			throw new IllegalArgumentException("name too long");
	}
	/**
	 * Internal create channel.
	 */
	// TODO no minor version with the addresses
	// TODO what if there is an channel with the same name, but on different host!
	public Channel createChannelInternal(String name, ChannelRequester requester, short priority,
			InetSocketAddress[] addresses) throws PVAException {
		checkState();
		checkChannelName(name);

		if (requester == null)
			throw new IllegalArgumentException("null requester");
		
		if (priority < ChannelProvider.PRIORITY_MIN || priority > ChannelProvider.PRIORITY_MAX)
			throw new IllegalArgumentException("priority out of bounds");

		/*
		// lookup for channel w/o named lock
		ChannelImpl channel = getChannel(name, priority, true);
		if (channel != null)
		{
			if (l != null)
				channel.addConnectionListenerAndFireIfConnected(l);
			return channel;
		}
		*/
			
		boolean lockAcquired = namedLocker.acquireSynchronizationObject(name, LOCK_TIMEOUT);
		if (lockAcquired)
		{ 
			try
			{
				/*
				// ... lookup for channel, if created while acquiring lock
				channel = getChannel(name, priority, true);
				if (channel != null)
				{
					if (l != null)
						channel.addConnectionListenerAndFireIfConnected(l);
					return channel;
				}
				*/					
				int cid = generateCID();
				return new ChannelImpl(this, cid, name, requester, priority, addresses);
			}
			finally
			{
				namedLocker.releaseSynchronizationObject(name);	
			}
		}
		else
		{     
			throw new PVAException("Failed to obtain synchronization lock for '" + name + "', possible deadlock.", null);
		}
	}
	
	/**
	 * Destroy channel.
	 * @param channel
	 * @param force
	 * @throws PVAException
	 * @throws IllegalStateException
	 */
	public void destroyChannel(ChannelImpl channel, boolean force)
		throws PVAException, IllegalStateException {
			
		boolean lockAcquired = namedLocker.acquireSynchronizationObject(channel.getChannelName(), LOCK_TIMEOUT);
		if (lockAcquired)
		{ 
			try
			{   
				channel.destroyChannel(force);
			}
			catch (IOException ioex)
			{
				logger.log(Level.SEVERE, "Failed to cleanly destroy channel.", ioex);
				throw new PVAException("Failed to cleanly destroy channel.", ioex);
			}
			finally
			{
				namedLocker.releaseSynchronizationObject(channel.getChannelName());	
			}
		}
		else
		{     
			throw new PVAException("Failed to obtain synchronization lock for '" + channel.getChannelName() + "', possible deadlock.", null);
		}
	}

	/**
	 * Register channel.
	 * @param channel
	 */
	void registerChannel(ChannelImpl channel)
	{
		synchronized (channelsByCID)
		{
			channelsByCID.put(channel.getChannelID(), channel);
			//channelsByName.put(getUniqueChannelName(channel.getChannelName(), channel.getPriority()), channel);
		}
	}

	/**
	 * Unregister channel.
	 * @param channel
	 */
	void unregisterChannel(ChannelImpl channel)
	{
		synchronized (channelsByCID)
		{
			channelsByCID.remove(channel.getChannelID());
			//channelsByName.remove(getUniqueChannelName(channel.getChannelName(), channel.getPriority()));
		}
	}

	/**
	 * Searches for a channel with given channel ID.
	 * @param channelID CID.
	 * @return channel with given CID, <code>null</code> if non-existent.
	 */
	public ChannelImpl getChannel(int channelID)
	{
		synchronized (channelsByCID)
		{
			return (ChannelImpl)channelsByCID.get(channelID);
		}
	}
	
	/*
	 * Generate unique channel string from channel name and priority.
	 * @param name channel name.
	 * @param priority channel priority.
	 * @return unique channel string.
	 *
	private final String getUniqueChannelName(String name, short priority)
	{
		// this name is illegal for PVA, so this function is unique
		return name + '\0' + priority;
	}*/
	
	/*
	 * Searches for a channel with given channel name.
	 * @param name channel name.
	 * @param priority channel priority.
	 * @param acquire whether to acquire ownership (increment ref. counting)
	 * @return channel with given name, <code>null</code> if non-existant.
	 *
	public ChannelImpl getChannel(String name, short priority, boolean acquire)
	{
		synchronized (channelsByName)
		{
			ChannelImpl channel = (ChannelImpl)channelsByName.get(getUniqueChannelName(name, priority));
			if (channel != null && acquire)
				channel.acquire();
			return channel;
		}
	}*/

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ClientContext#printInfo()
	 */
	public void printInfo() {
		printInfo(System.out);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ClientContext#printInfo(java.io.PrintStream)
	 */
	public void printInfo(PrintStream out)  {
		out.println("CLASS   : " + getClass().getName());
		out.println("VERSION : " + getVersion());
		out.println("ADDR_LIST : " + addressList);
		out.println("AUTO_ADDR_LIST : " + autoAddressList);
		out.println("CONNECTION_TIMEOUT : " + connectionTimeout);
		out.println("BEACON_PERIOD : " + beaconPeriod);
		out.println("BROADCAST_PORT : " + broadcastPort);
		out.println("RCV_BUFFER_SIZE : " + receiveBufferSize);
		//out.println("EVENT_DISPATCHER: " + eventDispatcher);
		out.print("STATE : ");
		switch (state)
		{
			case NOT_INITIALIZED:
				out.println("NOT_INITIALIZED");
				break;
			case INITIALIZED:
				out.println("INITIALIZED");
				break;
			case DESTROYED:
				out.println("DESTROYED");
				break;
			default:
				out.println("UNKNOWN");
		}
	}

	/**
	 * Get initialization status.
	 * @return initialization status.
	 */
	public boolean isInitialized() {
		return state == State.INITIALIZED;
	}

	/**
	 * Get destruction status.
	 * @return destruction status.
	 */
	public boolean isDestroyed() {
		return state == State.DESTROYED;
	}
	
	/**
	 * Get search address list.
	 * @return get search address list.
	 */
	public String getAddressList() {
		return addressList;
	}

	/**
	 * Get auto search-list flag.
	 * @return auto search-list flag.
	 */
	public boolean isAutoAddressList() {
		return autoAddressList;
	}

	/**
	 * Get beacon period (in seconds).
	 * @return beacon period (in seconds).
	 */
	public float getBeaconPeriod() {
		return beaconPeriod;
	}

	/**
	 * Get connection timeout (in seconds).
	 * @return connection timeout (in seconds). 
	 */
	public float getConnectionTimeout() {
		return connectionTimeout;
	}

	/**
	 * Get logger.
	 * @return logger.
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * Get receive buffer size (max size of payload).
	 * @return receive buffer size (max size of payload).
	 */
	public int getReceiveBufferSize() {
		return receiveBufferSize;
	}

	/**
	 * Get broadcast port.
	 * @return broadcast port.
	 */
	public int getBroadcastPort() {
		return broadcastPort;
	}

	/*
	 * Get event dispatcher.
	 * @return event dispatcher.
	 *
	public final EventDispatcher getEventDispatcher() {
		return eventDispatcher;
	}*/

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ClientContext#dispose()
	 */
	public void dispose() {
		try {
			destroy();
		} catch (Throwable th) {
			// noop
		}
	}

	// ************************************************************************** //

	/**
	 * Broadcast transport.
	 * @return broadcast transport.
	 */
	//public UDPTransport getBroadcastTransport() {
	public BlockingUDPTransport getBroadcastTransport() {
		return broadcastTransport;
	}

	/**
	 * Broadcast transport.
	 * @return broadcast transport.
	 */
//	public UDPTransport getSearchTransport() {
	public BlockingUDPTransport getSearchTransport() {
		return searchTransport;
	}

	/**
	 * Get PVA transport (virtual circuit) registry.
	 * @return PVA transport (virtual circuit) registry.
	 */
	public TransportRegistry getTransportRegistry() {
		return transportRegistry;
	}

	/**
	 * Get timer.
	 * @return timer.
	 */
	public Timer getTimer() {
		return timer;
	}

	/**
	 * Get channel search manager.
	 * @return channel search manager.
	 */
	public ChannelSearchManager getChannelSearchManager() {
		return channelSearchManager;
	}

    /**
     * Get LF thread pool.
     * @return LF thread pool, can be <code>null</code> if disabled.
     *
    public LeaderFollowersThreadPool getLeaderFollowersThreadPool() {
        return leaderFollowersThreadPool;
    }*/
	
	/**
	 * Called each time new server is detected. 
	 */
	public void newServerDetected()
	{
		if (channelSearchManager != null)
			channelSearchManager.newServerDetected();
	}
	
	/**
	 * Get, or create if necessary, transport of given server address.
	 * Note that this method might block (creating TCP connection, verifying it).
	 * @param serverAddress	required transport address
	 * @param priority process priority.
	 * @return transport for given address
	 */
	Transport getTransport(TransportClient client, InetSocketAddress serverAddress, byte minorRevision, short priority)
	{
		try
		{
			return connector.connect(client, clientResponseHandler, serverAddress, minorRevision, priority);
		}
		catch (ConnectionException cex)
		{
			logger.log(Level.SEVERE, "Failed to create transport for: " + serverAddress, cex);
		}
			
		return null;
	}
   
	/**
	 * Generate Client channel ID (CID).
	 * @return Client channel ID (CID). 
	 */
	private int generateCID()
	{
		synchronized (channelsByCID)
		{
			// search first free (theoretically possible loop of death)
			while (getChannel(++lastCID) != null);
			// reserve CID
			channelsByCID.put(lastCID, null);
			return lastCID;
		}
	}

	/**
	 * Free generated channel ID (CID).
	 */
	private void freeCID(int cid)
	{
		synchronized (channelsByCID)
		{
			channelsByCID.remove(cid);
		}
	}

	/**
	 * Searches for a response request with given channel IOID.
	 * @param ioid	I/O ID.
	 * @return request response with given I/O ID.
	 */
	public ResponseRequest getResponseRequest(int ioid)
	{
		synchronized (pendingResponseRequests)
		{
			return (ResponseRequest)pendingResponseRequests.get(ioid);
		}
	}

	/**
	 * Register response request.
	 * @param request request to register.
	 * @return request ID (IOID).
	 */
	public int registerResponseRequest(ResponseRequest request)
	{
		synchronized (pendingResponseRequests)
		{
			int ioid = generateIOID();
			pendingResponseRequests.put(ioid, request);
			return ioid;
		}
	}

	/**
	 * Unregister response request.
	 * @param request
	 * @return removed object, can be <code>null</code>
	 */
	public ResponseRequest unregisterResponseRequest(ResponseRequest request)
	{
		synchronized (pendingResponseRequests)
		{
			return (ResponseRequest)pendingResponseRequests.remove(request.getIOID());
		}
	}

	/**
	 * Generate IOID.
	 * @return IOID. 
	 */
	private int generateIOID()
	{
		synchronized (pendingResponseRequests)
		{
			// search first free (theoretically possible loop of death)
			while (pendingResponseRequests.get(++lastIOID) != null || lastIOID == PVAConstants.PVA_INVALID_IOID);
			// reserve IOID
			pendingResponseRequests.put(lastIOID, null);
			return lastIOID;
		}
	}

	/**
	 * Get (and if necessary create) beacon handler.
	 * @param responseFrom remote source address of received beacon.	
	 * @return beacon handler for particular server.
	 */
	public BeaconHandler getBeaconHandler(InetSocketAddress responseFrom)
	{
		synchronized (beaconHandlers) {
			BeaconHandlerImpl handler = beaconHandlers.get(responseFrom);
			if (handler == null)
			{
				handler = new BeaconHandlerImpl(this, responseFrom);
				beaconHandlers.put(responseFrom, handler);
			}
			return handler;
		}
	}

	public ChannelProvider getProvider() {
		return channelProvider;
	}

	private static final StatusCreate statusCreate = PVFactory.getStatusCreate();
    private static final Status okStatus = statusCreate.getStatusOK();

    private class ChannelProviderImpl implements ChannelProvider
	{
    	private class ChannelFindImpl implements ChannelFind, SearchInstance
    	{
    		final String channelName;
    		final ChannelFindRequester requester;
    		final int channelID;
    		final AtomicInteger userValue = new AtomicInteger();

    		public ChannelFindImpl(String channelName, ChannelFindRequester requester) {
				this.channelName = channelName;
				this.requester = requester;
				
				channelID = generateCID();
				
				getChannelSearchManager().register(this);
			}

			/* (non-Javadoc)
			 * @see org.epics.pvaccess.client.impl.remote.ChannelSearchManager.SearchInstance#getChannelID()
			 */
			@Override
			public int getChannelID() {
				return channelID;
			}

			/* (non-Javadoc)
			 * @see org.epics.pvaccess.client.impl.remote.ChannelSearchManager.SearchInstance#getChannelName()
			 */
			@Override
			public String getChannelName() {
				return channelName;
			}

			/* (non-Javadoc)
			 * @see org.epics.pvaccess.client.impl.remote.ChannelSearchManager.SearchInstance#searchResponse(byte, java.net.InetSocketAddress)
			 */
			@Override
			public void searchResponse(byte minorRevision, InetSocketAddress serverAddress) {
				freeCID(channelID);
				requester.channelFindResult(okStatus, this, true);
			}

			/* (non-Javadoc)
			 * @see org.epics.pvaccess.client.impl.remote.search.SearchInstance#getUserValue()
			 */
			@Override
			public AtomicInteger getUserValue() {
				return userValue;
			}

			/* (non-Javadoc)
			 * @see org.epics.pvaccess.client.ChannelFind#cancel()
			 */
			@Override
			public void cancel() {
				freeCID(channelID);
				getChannelSearchManager().unregister(this);
			}

			/* (non-Javadoc)
			 * @see org.epics.pvaccess.client.ChannelFind#getChannelProvider()
			 */
			@Override
			public ChannelProvider getChannelProvider() {
				return getProvider();
			}
    		
    	}
    	
	    /* (non-Javadoc)
		 * @see org.epics.pvaccess.client.ChannelProvider#channelFind(java.lang.String, org.epics.pvaccess.client.ChannelFindRequester)
		 */
		@Override
		public ChannelFind channelFind(String channelName,
				ChannelFindRequester channelFindRequester) {

			checkChannelName(channelName);

			if (channelFindRequester == null)
				throw new IllegalArgumentException("null requester");
			
			synchronized (this) {
				try {
					checkState();
					return new ChannelFindImpl(channelName, channelFindRequester);
				} catch (Throwable th) {
					channelFindRequester.channelFindResult(statusCreate.createStatus(StatusType.ERROR, "failed to find channel", th), null, false);
					return null;
				}
			}
		}
	
		/* (non-Javadoc)
		 * @see org.epics.pvaccess.client.ChannelProvider#createChannel(java.lang.String, org.epics.pvaccess.client.ChannelRequester, short)
		 */
		@Override
		public Channel createChannel(String channelName,
				ChannelRequester channelRequester, short priority) {
        	return createChannel(channelName, channelRequester, priority, null);
		}
	
        /* (non-Javadoc)
         * @see org.epics.pvaccess.client.ChannelProvider#createChannel(java.lang.String, org.epics.pvaccess.client.ChannelRequester, short, java.lang.String[])
         */
        @Override
		public Channel createChannel(String channelName,
				ChannelRequester channelRequester, short priority,
				String address) {
		    Channel channel;
			try {
				// TODO configurable
				short defaultPort = PVAConstants.PVA_SERVER_PORT;
				InetSocketAddress[] addressList = (address == null) ? null : InetAddressUtil.getSocketAddressList(address, defaultPort);
				channel = createChannelInternal(channelName, channelRequester, priority, addressList);
			} catch (IllegalArgumentException iae) {
				throw iae;
			} catch (Throwable th) {
				channelRequester.channelCreated(statusCreate.createStatus(StatusType.ERROR, "failed to create channel", th), null);
				return null;
			}
		    channelRequester.channelCreated(okStatus, channel);
		    return channel;
		}

        /* (non-Javadoc)
		 * @see org.epics.pvaccess.client.ChannelProvider#getProviderName()
		 */
		@Override
		public String getProviderName() {
			return PROVIDER_NAME;
		}
	
		/* (non-Javadoc)
		 * @see org.epics.pvaccess.client.ChannelProvider#destroy()
		 */
		@Override
		public void destroy() {
			dispose();
		}
	
	}
	
	

}
