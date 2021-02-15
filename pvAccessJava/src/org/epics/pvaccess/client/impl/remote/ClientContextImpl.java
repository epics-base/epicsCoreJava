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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
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
import org.epics.pvaccess.client.ChannelListRequester;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.client.impl.remote.search.ChannelSearchManager;
import org.epics.pvaccess.client.impl.remote.search.SearchInstance;
import org.epics.pvaccess.client.impl.remote.search.SimpleChannelSearchManagerImpl;
import org.epics.pvaccess.client.impl.remote.tcp.BlockingClientTCPTransport;
import org.epics.pvaccess.client.impl.remote.tcp.BlockingTCPConnector;
import org.epics.pvaccess.client.impl.remote.tcp.BlockingTCPConnector.TransportFactory;
import org.epics.pvaccess.impl.remote.ConnectionException;
import org.epics.pvaccess.impl.remote.Context;
import org.epics.pvaccess.impl.remote.ProtocolType;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportClient;
import org.epics.pvaccess.impl.remote.TransportRegistry;
import org.epics.pvaccess.impl.remote.io.impl.PollerImpl;
import org.epics.pvaccess.impl.remote.request.ResponseHandler;
import org.epics.pvaccess.impl.remote.request.ResponseRequest;
import org.epics.pvaccess.impl.remote.udp.BlockingUDPConnector;
import org.epics.pvaccess.impl.remote.udp.BlockingUDPTransport;
import org.epics.pvaccess.impl.remote.utils.GUID;
import org.epics.pvaccess.plugins.SecurityPlugin;
import org.epics.pvaccess.plugins.impl.client.CAClientSecurityPlugin;
import org.epics.pvaccess.util.InetAddressUtil;
import org.epics.pvaccess.util.configuration.Configuration;
import org.epics.pvaccess.util.configuration.ConfigurationProvider;
import org.epics.pvaccess.util.configuration.impl.ConfigurationFactory;
import org.epics.pvaccess.util.logging.ConsoleLogHandler;
import org.epics.pvaccess.util.sync.NamedLockPattern;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.misc.ThreadPriority;
import org.epics.pvdata.misc.Timer;
import org.epics.pvdata.misc.TimerFactory;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.StatusCreate;
import org.epics.util.compat.legacy.net.NetworkInterface;

/**
 * Implementation of PVAJ JCA <code>Context</code>.
 *
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ClientContextImpl implements Context {

	static {
		// force only IPv4 sockets, since EPICS does not work right with IPv6 sockets
		// see http://java.sun.com/j2se/1.5.0/docs/guide/net/properties.html
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	/**
	 * Name if the provider this context provides.
	 */
	public static final String PROVIDER_NAME = "pva";

	/**
	 * Version.
	 */
	public static final Version VERSION = new Version("pvAccess Client", "Java", PVAVersion.VERSION_MAJOR,
			PVAVersion.VERSION_MINOR, PVAVersion.VERSION_MAINTENANCE, PVAVersion.VERSION_DEVELOPMENT);

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
	 * Debug level.
	 */
	protected int debugLevel;

	/**
	 * A space-separated list of broadcast address for process variable name
	 * resolution. Each address must be of the form: ip.number:port or
	 * host.name:port
	 */
	protected String addressList = "";

	/**
	 * Define whether or not the network interfaces should be discovered at runtime.
	 */
	protected boolean autoAddressList = true;

	/**
	 * If the context doesn't see a beacon from a server that it is connected to for
	 * connectionTimeout seconds then a state-of-health message is sent to the
	 * server over TCP/IP. If this state-of-health message isn't promptly replied to
	 * then the context will assume that the server is no longer present on the
	 * network and disconnect.
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
	 * Broadcast transport needed to listen for broadcasts.
	 */

	protected BlockingUDPTransport broadcastTransport = null;

	/**
	 * UDP transport needed for channel searches.
	 */

	protected BlockingUDPTransport searchTransport = null;

	/**
	 * Local multicast address.
	 */
	protected InetSocketAddress localBroadcastAddress = null;

	/**
	 * PVA connector (creates PVA virtual circuit).
	 */
	protected BlockingTCPConnector connector = null;

	/**
	 * PVA transport (virtual circuit) registry. This registry contains all active
	 * transports - connections to PVA servers.
	 */
	protected TransportRegistry transportRegistry = null;

	/**
	 * Context instance.
	 */
	private NamedLockPattern namedLocker;

	/**
	 * Context instance.
	 */
	private static final int LOCK_TIMEOUT = 20 * 1000; // 20s

	/**
	 * Map of channels (keys are CIDs).
	 */
	// TODO consider using WeakHashMap (and call Channel.destroy() in finalize()
	// method).
	protected final Map<Integer, Channel> channelsByCID = Collections.synchronizedMap(new HashMap<Integer, Channel>());

	/**
	 * Last CID cache.
	 */
	private int lastCID = 0;

	/**
	 * Map of pending response requests (keys are IOID).
	 */
	// TODO consider using WeakHashMap (and call ResponseRequest.destroy() in
	// finalize() method).
	protected final Map<Integer, ResponseRequest> pendingResponseRequests = Collections
			.synchronizedMap(new HashMap<Integer, ResponseRequest>());

	/**
	 * Last IOID cache.
	 */
	private int lastIOID = 0;

	/**
	 * Channel search manager. Manages UDP search requests.
	 */
	private ChannelSearchManager channelSearchManager;

	/**
	 * Beacon handler map.
	 */
	protected final Map<String, Map<InetSocketAddress, BeaconHandlerImpl>> beaconHandlers = new HashMap<String, Map<InetSocketAddress, BeaconHandlerImpl>>();

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
	public ClientContextImpl() {
		loadConfiguration();
		initializeLogger();
		initializeSecutiryPlugins();

		clientResponseHandler = new ClientResponseHandler(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.epics.pvaccess.client.ClientContext#getVersion()
	 */
	public Version getVersion() {
		return VERSION;
	}

	/**
	 * Initialize context logger.
	 */
	protected void initializeLogger() {
		logger = Logger.getLogger(this.getClass().getName());

		if (debugLevel > 0) {
			logger.setLevel(Level.ALL);

			// install console logger only if there is no already installed
			Logger inspectedLogger = logger;
			boolean found = false;
			while (inspectedLogger != null) {
				for (Handler handler : inspectedLogger.getHandlers())
					if (handler instanceof ConsoleLogHandler) {
						found = true;
						break;
					}
				inspectedLogger = inspectedLogger.getParent();
			}

			if (!found)
				logger.addHandler(new ConsoleLogHandler());
		}
	}

	/**
	 * Get configuration instance.
	 *
	 * @return the configuration.
	 */
	public Configuration getConfiguration() {
		final ConfigurationProvider configurationProvider = ConfigurationFactory.getProvider();
		Configuration config = configurationProvider.getConfiguration("pvAccess-client");
		if (config == null)
			config = configurationProvider.getConfiguration("system");
		return config;
	}

	/**
	 * Load configuration.
	 */
	protected void loadConfiguration() {
		final Configuration config = getConfiguration();

		debugLevel = config.getPropertyAsInteger(PVAConstants.PVACCESS_DEBUG, 0);

		addressList = config.getPropertyAsString("EPICS_PVA_ADDR_LIST", addressList);
		autoAddressList = config.getPropertyAsBoolean("EPICS_PVA_AUTO_ADDR_LIST", autoAddressList);
		connectionTimeout = config.getPropertyAsFloat("EPICS_PVA_CONN_TMO", connectionTimeout);
		beaconPeriod = config.getPropertyAsFloat("EPICS_PVA_BEACON_PERIOD", beaconPeriod);
		broadcastPort = config.getPropertyAsInteger("EPICS_PVA_BROADCAST_PORT", broadcastPort);
		receiveBufferSize = config.getPropertyAsInteger("EPICS_PVA_MAX_ARRAY_BYTES", receiveBufferSize);
	}

	/**
	 * Check context state and tries to establish necessary state.
	 *
	 * @throws PVAException
	 *             any PVA exception.
	 * @throws IllegalStateException
	 *             thrown if context is already destroyed.
	 */
	protected void checkState() throws PVAException, IllegalStateException {
		if (state == State.DESTROYED)
			throw new IllegalStateException("Context destroyed.");
		else if (state == State.NOT_INITIALIZED) {
			// double-locking pattern used to prevent unnecessary initialization calls
			synchronized (this) {
				if (state == State.NOT_INITIALIZED)
					initialize();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.epics.pvaccess.client.ClientContext#initialize()
	 */
	public synchronized void initialize() throws PVAException {

		if (state == State.DESTROYED)
			throw new IllegalStateException("Context destroyed.");
		else if (state == State.INITIALIZED)
			throw new IllegalStateException("Context already initialized.");

		internalInitialize();

		state = State.INITIALIZED;

	}

	// TODO remove
	final AtomicBoolean pollerInitialized = new AtomicBoolean();
	PollerImpl poller;

	/**
	 * @throws PVAException for PV Access Exceptions
	 */
	private void internalInitialize() throws PVAException {

		timer = TimerFactory.create("pvAccess-client timer", ThreadPriority.lower);
		TransportFactory transportFactory = new TransportFactory() {

			public Transport create(Context context, SocketChannel channel, ResponseHandler responseHandler,
					int receiveBufferSize, TransportClient client, short transportRevision, float heartbeatInterval,
					short priority) {
				try {
					return new BlockingClientTCPTransport(context, channel, responseHandler, receiveBufferSize, client,
							transportRevision, heartbeatInterval, priority);
				} catch (SocketException e) {
					throw new RuntimeException("Failed to create transport.");
				}
			}
		};

		connector = new BlockingTCPConnector(this, transportFactory, receiveBufferSize, connectionTimeout);
		transportRegistry = new TransportRegistry();
		namedLocker = new NamedLockPattern();

		// setup UDP transport
		initializeUDPTransport();

		// setup search manager
		channelSearchManager = new SimpleChannelSearchManagerImpl(this);
	}

	/**
	 * Initialized UDP transport (broadcast socket and repeater connection).
	 */
	private void initializeUDPTransport() {
		// setup UDP transport
		try {
			// where to bind (listen) address
			InetSocketAddress listenLocalAddress = new InetSocketAddress(broadcastPort);

			// where to send address
			InetSocketAddress[] broadcastAddresses = InetAddressUtil.getBroadcastAddresses(broadcastPort);

			BlockingUDPConnector broadcastConnector = new BlockingUDPConnector(this, true, broadcastAddresses, true);

			broadcastTransport = (BlockingUDPTransport) broadcastConnector.connect(
					null,
					new ClientResponseHandler(this),
					listenLocalAddress,
					PVAConstants.PVA_PROTOCOL_REVISION,
					PVAConstants.PVA_DEFAULT_PRIORITY
			);

			BlockingUDPConnector searchConnector = new BlockingUDPConnector(this, false, broadcastAddresses, true);

			searchTransport = (BlockingUDPTransport) searchConnector.connect(null, new ClientResponseHandler(this),
					new InetSocketAddress(0), PVAConstants.PVA_PROTOCOL_REVISION, PVAConstants.PVA_DEFAULT_PRIORITY);

			// set broadcast address list
			if (addressList != null && addressList.length() > 0) {
				// if auto is true, add it to specified list
				InetSocketAddress[] appendList = null;
				if (autoAddressList == true)
					appendList = broadcastTransport.getSendAddresses();

				InetSocketAddress[] list = InetAddressUtil.getSocketAddressList(addressList, broadcastPort, appendList);
				if (list != null && list.length > 0) {
					broadcastTransport.setSendAddresses(list);
					searchTransport.setSendAddresses(list);
				}
			}

			final InetSocketAddress[] broadcastAddressList = broadcastTransport.getSendAddresses();
			if (broadcastAddressList != null)
				for (int i = 0; i < broadcastAddressList.length; i++)
					logger.finer("Broadcast address #" + i + ": " + broadcastAddressList[i] + '.');

			// TODO do not use searchBroadcast in future
			// TODO configurable local NIF, address
			// setup local broadcasting
			NetworkInterface localNIF = InetAddressUtil.getLoopbackNIF();
			if (localNIF != null) {
				try {
					InetAddress group = InetAddress.getByName("224.0.0.128");
					localBroadcastAddress = new InetSocketAddress(group, broadcastPort);
					searchTransport.join(group, localNIF);

					// NOTE: this disables usage of multicast addresses in EPICS_PVA_ADDR_LIST
					searchTransport.setMulticastNIF(localNIF, true);

					logger.config("Local multicast enabled on " + localBroadcastAddress + ":" + broadcastPort
							+ " using " + localNIF.getDisplayName() + ".");
				} catch (Exception th) {
					logger.log(Level.CONFIG, "Failed to initialize local multicast, funcionality disabled.", th);
				}
			} else {
				logger.config("Failed to detect a loopback network interface, local multicast disabled.");
			}

			broadcastTransport.start();
			searchTransport.start();

		} catch (ConnectionException ce) {
			logger.log(Level.SEVERE, "Failed to initialize UDP transport.", ce);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.epics.pvaccess.client.ClientContext#destroy()
	 */
	public synchronized void destroy() {

		if (state == State.DESTROYED)
			throw new IllegalStateException("Context already destroyed.");

		// go into destroyed state ASAP
		state = State.DESTROYED;

		internalDestroy();

	}

	/**
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
		if (broadcastTransport != null) {
			try {
				broadcastTransport.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (searchTransport != null) {
			try {
				searchTransport.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

    /**
     * Destroy all channels.
     */
    private void destroyAllChannels() {
        synchronized (channelsByCID) {
            for (Channel channel : new ArrayList<Channel>(channelsByCID.values())) {
                try {
                    channel.destroy();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            channelsByCID.clear();
        }
    }

	/**
	 * Check channel name.
	 *
	 * @param name
	 *            name to check.
	 * @throws IllegalArgumentException for illegal argument exceptions
	 */
	private final void checkChannelName(String name) throws IllegalArgumentException {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("null or empty channel name");
		else if (name.length() > PVAConstants.MAX_CHANNEL_NAME_LENGTH)
			throw new IllegalArgumentException("name too long");
	}

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

		boolean lockAcquired = namedLocker.acquireSynchronizationObject(name, LOCK_TIMEOUT);
		if (lockAcquired) {
			try {
				int cid = generateCID();
				return new ChannelImpl(this, cid, name, requester, priority, addresses);
			} finally {
				namedLocker.releaseSynchronizationObject(name);
			}
		} else {
			throw new PVAException("Failed to obtain synchronization lock for '" + name + "', possible deadlock.",
					null);
		}
	}

	/**
	 * Destroy channel.
	 *
	 * @param channel
	 *            the channel to destroy.
	 * @param force
	 *            force-full (non-user) destruction.
	 * @throws PVAException
	 *             unexpected exception.
	 * @throws IllegalStateException
	 *             if channel is already destroyed.
	 */
	public void destroyChannel(ChannelImpl channel, boolean force) throws PVAException, IllegalStateException {

		boolean lockAcquired = namedLocker.acquireSynchronizationObject(channel.getChannelName(), LOCK_TIMEOUT);
		if (lockAcquired) {
			try {
				channel.destroyChannel(force);
			} catch (IOException ioex) {
				logger.log(Level.SEVERE, "Failed to cleanly destroy channel.", ioex);
				throw new PVAException("Failed to cleanly destroy channel.", ioex);
			} finally {
				namedLocker.releaseSynchronizationObject(channel.getChannelName());
			}
		} else {
			throw new PVAException(
					"Failed to obtain synchronization lock for '" + channel.getChannelName() + "', possible deadlock.",
					null);
		}
	}

	/**
	 * Register channel.
	 *
	 * @param channel channel
	 */
	void registerChannel(ChannelImpl channel) {
		channelsByCID.put(channel.getChannelID(), channel);
	}

	/**
	 * Unregister channel.
	 *
	 * @param channel channel
	 */
	void unregisterChannel(ChannelImpl channel) {
		channelsByCID.remove(channel.getChannelID());
	}

	/**
	 * Searches for a channel with given channel ID.
	 *
	 * @param channelID
	 *            CID.
	 * @return channel with given CID, <code>null</code> if non-existent.
	 */
	public ChannelImpl getChannel(int channelID) {

		return (ChannelImpl) channelsByCID.get(channelID);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.epics.pvaccess.client.ClientContext#printInfo()
	 */
	public void printInfo() {
		printInfo(System.out);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.epics.pvaccess.client.ClientContext#printInfo(java.io.PrintStream)
	 */
	public void printInfo(PrintStream out) {
		out.println("CLASS   : " + getClass().getName());
		out.println("VERSION : " + getVersion());
		out.println("ADDR_LIST : " + addressList);
		out.println("AUTO_ADDR_LIST : " + autoAddressList);
		out.println("CONNECTION_TIMEOUT : " + connectionTimeout);
		out.println("BEACON_PERIOD : " + beaconPeriod);
		out.println("BROADCAST_PORT : " + broadcastPort);
		out.println("RCV_BUFFER_SIZE : " + receiveBufferSize);
		// out.println("EVENT_DISPATCHER: " + eventDispatcher);
		out.print("STATE : ");
		switch (state) {
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
	 *
	 * @return initialization status.
	 */
	public boolean isInitialized() {
		return state == State.INITIALIZED;
	}

	/**
	 * Get destruction status.
	 *
	 * @return destruction status.
	 */
	public boolean isDestroyed() {
		return state == State.DESTROYED;
	}

	/**
	 * Get search address list.
	 *
	 * @return get search address list.
	 */
	public String getAddressList() {
		return addressList;
	}

	/**
	 * Get auto search-list flag.
	 *
	 * @return auto search-list flag.
	 */
	public boolean isAutoAddressList() {
		return autoAddressList;
	}

	/**
	 * Get beacon period (in seconds).
	 *
	 * @return beacon period (in seconds).
	 */
	public float getBeaconPeriod() {
		return beaconPeriod;
	}

	/**
	 * Get connection timeout (in seconds).
	 *
	 * @return connection timeout (in seconds).
	 */
	public float getConnectionTimeout() {
		return connectionTimeout;
	}

	/**
	 * Get logger.
	 *
	 * @return logger.
	 */
	public Logger getLogger() {
		return logger;
	}

	public int getDebugLevel() {
		return debugLevel;
	}

	/**
	 * Get receive buffer size (max size of payload).
	 *
	 * @return receive buffer size (max size of payload).
	 */
	public int getReceiveBufferSize() {
		return receiveBufferSize;
	}

	/**
	 * Get broadcast port.
	 *
	 * @return broadcast port.
	 */
	public int getBroadcastPort() {
		return broadcastPort;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.epics.pvaccess.client.ClientContext#dispose()
	 */
	public void dispose() {
		try {
			destroy();
		} catch (Throwable th) {
			// noop
		}
	}

	/**
	 * Broadcast transport.
	 *
	 * @return broadcast transport.
	 */
	public BlockingUDPTransport getBroadcastTransport() {
		return broadcastTransport;
	}

	/**
	 * Broadcast transport.
	 *
	 * @return broadcast transport.
	 */
	public BlockingUDPTransport getSearchTransport() {
		return searchTransport;
	}

	/**
	 * Get local multicast address (group).
	 *
	 * @return the address.
	 */
	public InetSocketAddress getLocalMulticastAddress() {
		return localBroadcastAddress;
	}

	/**
	 * Get PVA transport (virtual circuit) registry.
	 *
	 * @return PVA transport (virtual circuit) registry.
	 */
	public TransportRegistry getTransportRegistry() {
		return transportRegistry;
	}

	/**
	 * Get timer.
	 *
	 * @return timer.
	 */
	public Timer getTimer() {
		return timer;
	}

	private final Map<String, SecurityPlugin> securityPlugins = new LinkedHashMap<String, SecurityPlugin>();

	public Map<String, SecurityPlugin> getSecurityPlugins() {
		return securityPlugins;
	}

	private void initializeSecutiryPlugins() {
		String classes = System.getProperty(SecurityPlugin.SECURITY_PLUGINS_CLIENT_KEY);
		if (classes != null) {
			StringTokenizer tokens = new StringTokenizer(classes, ",");

			while (tokens.hasMoreElements()) {
				String className = tokens.nextToken().trim();
				logger.log(Level.FINER, "Loading security plug-in '" + className + "'...");

				try {
					final Class<?> c = Class.forName(className);
					SecurityPlugin p = (SecurityPlugin) c.newInstance(); // TODO in the future any specific method can
																			// be used
					securityPlugins.put(p.getId(), p);
					logger.log(Level.FINER, "Security plug-in '" + className + "' [" + p.getId() + "] loaded.");
				} catch (Throwable th) {
					logger.log(Level.WARNING, "Failed to load security plug-in '" + className + "'.", th);
				}
			}
		}

		// load by default
		if (!securityPlugins.containsKey("ca")) {
			SecurityPlugin p = new CAClientSecurityPlugin();
			securityPlugins.put(p.getId(), p);
		}

		logger.log(Level.FINE, "Installed security plug-ins: " + securityPlugins.keySet() + ".");
	}

	/**
	 * Get channel search manager.
	 *
	 * @return channel search manager.
	 */
	public ChannelSearchManager getChannelSearchManager() {
		return channelSearchManager;
	}

	/**
	 * Get LF thread pool.
	 *
	 * @return LF thread pool, can be <code>null</code> if disabled.
	 *
	 *         public LeaderFollowersThreadPool getLeaderFollowersThreadPool() {
	 *         return leaderFollowersThreadPool; }
	 */

	/**
	 * Called each time new server is detected.
	 */
	public void newServerDetected() {
		if (channelSearchManager != null)
			channelSearchManager.newServerDetected();
	}

	/**
	 * Get, or create if necessary, transport of given server address. Note that
	 * this method might block (creating TCP connection, verifying it).
	 *
	 * @param serverAddress
	 *            required transport address
	 * @param priority
	 *            process priority.
	 * @return transport for given address
	 */
	Transport getTransport(TransportClient client, InetSocketAddress serverAddress, byte minorRevision,
			short priority) {
		try {
			return connector.connect(client, clientResponseHandler, serverAddress, minorRevision, priority);
		} catch (ConnectionException cex) {
			logger.log(Level.SEVERE, "Failed to create transport for: " + serverAddress, cex);
		}

		return null;
	}

	/**
	 * Generate Client channel ID (CID).
	 *
	 * @return Client channel ID (CID).
	 */
    private int generateCID() {
        synchronized (channelsByCID) {
            // reserve CID
            // search first free (theoretically possible loop of death)
            while (getChannel(++lastCID) != null)
                ;
            // reserve CID
            channelsByCID.put(lastCID, null);
            return lastCID;
        }
    }

	/**
	 * Free generated channel ID (CID).
	 */
	private void freeCID(int cid) {
		channelsByCID.remove(cid);
	}

	/**
	 * Searches for a response request with given channel IOID.
	 *
	 * @param ioid
	 *            I/O ID.
	 * @return request response with given I/O ID.
	 */
    public ResponseRequest getResponseRequest(int ioid) {
            return (ResponseRequest) pendingResponseRequests.get(ioid);
    }

	/**
	 * Register response request.
	 *
	 * @param request
	 *            request to register.
	 * @return request ID (IOID).
	 */
	public int registerResponseRequest(ResponseRequest request) {
        synchronized (pendingResponseRequests) {
            int ioid = generateIOID();
            pendingResponseRequests.put(ioid, request);
            return ioid;
        }

	}

	/**
	 * Unregister response request.
	 *
	 * @param request
	 *            request to unregister.
	 * @return removed object, can be <code>null</code>
	 */
	public ResponseRequest unregisterResponseRequest(ResponseRequest request) {
		return (ResponseRequest) pendingResponseRequests.remove(request.getIOID());
	}

	/**
	 * Generate IOID.
	 *
	 * @return IOID.
	 */
    private int generateIOID() {
        synchronized (pendingResponseRequests) {
            // search first free (theoretically possible loop of death)
            while (pendingResponseRequests.get(++lastIOID) != null || lastIOID == PVAConstants.PVA_INVALID_IOID)
                ;
            // reserve IOID
            pendingResponseRequests.put(lastIOID, null);
            return lastIOID;
        }
    }

	/**
	 * Get (and if necessary create) beacon handler.
	 *
	 * @param protocol
	 *            protocol used.
	 * @param responseFrom
	 *            remote source address of received beacon.
	 * @return beacon handler for particular server.
	 */
	public BeaconHandler getBeaconHandler(String protocol, InetSocketAddress responseFrom) {
		// TODO for now we monitor only TCP responses
		if (!protocol.equals(ProtocolType.tcp.name()))
			return null;

		synchronized (beaconHandlers) {
			Map<InetSocketAddress, BeaconHandlerImpl> protocolBeaconHandlersMap = beaconHandlers.get(protocol);
			if (protocolBeaconHandlersMap == null) {
				protocolBeaconHandlersMap = new HashMap<InetSocketAddress, BeaconHandlerImpl>();
				beaconHandlers.get(protocolBeaconHandlersMap);
			}

			BeaconHandlerImpl handler = protocolBeaconHandlersMap.get(responseFrom);
			if (handler == null) {
				handler = new BeaconHandlerImpl(this, protocol, responseFrom);
				protocolBeaconHandlersMap.put(responseFrom, handler);
			}
			return handler;
		}
	}

	public ChannelProvider getProvider() {
		return channelProvider;
	}

	private static final StatusCreate statusCreate = PVFactory.getStatusCreate();
	private static final Status okStatus = statusCreate.getStatusOK();

	private static final Status listNotSupported = StatusFactory.getStatusCreate().createStatus(StatusType.ERROR,
			"channelList not supported", null);

	private static final Status findNotSupported = StatusFactory.getStatusCreate().createStatus(StatusType.ERROR,
			"channelFind not supported", null);

	private class ChannelProviderImpl implements ChannelProvider {
		@SuppressWarnings("unused")
		private class ChannelFindImpl implements ChannelFind, SearchInstance {
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

			/*
			 * (non-Javadoc)
			 *
			 * @see
			 * org.epics.pvaccess.client.impl.remote.ChannelSearchManager.SearchInstance#
			 * getChannelID()
			 */
			public int getChannelID() {
				return channelID;
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see
			 * org.epics.pvaccess.client.impl.remote.ChannelSearchManager.SearchInstance#
			 * getChannelName()
			 */
			public String getChannelName() {
				return channelName;
			}

			public void searchResponse(GUID guid, byte minorRevision, InetSocketAddress serverAddress) {
				freeCID(channelID);
				requester.channelFindResult(okStatus, this, true);
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see
			 * org.epics.pvaccess.client.impl.remote.search.SearchInstance#getUserValue()
			 */
			public AtomicInteger getUserValue() {
				return userValue;
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see org.epics.pvaccess.client.ChannelFind#cancel()
			 */
			public void cancel() {
				freeCID(channelID);
				getChannelSearchManager().unregister(this);
			}

			/*
			 * (non-Javadoc)
			 *
			 * @see org.epics.pvaccess.client.ChannelFind#getChannelProvider()
			 */
			public ChannelProvider getChannelProvider() {
				return getProvider();
			}

		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.epics.pvaccess.client.ChannelProvider#channelFind(java.lang.String,
		 * org.epics.pvaccess.client.ChannelFindRequester)
		 */
		public ChannelFind channelFind(String channelName, ChannelFindRequester channelFindRequester) {

			checkChannelName(channelName);

			if (channelFindRequester == null)
				throw new IllegalArgumentException("null requester");

			channelFindRequester.channelFindResult(findNotSupported, null, false);
			return null;

		}

		public ChannelFind channelList(ChannelListRequester channelListRequester) {

			if (channelListRequester == null)
				throw new IllegalArgumentException("null requester");

			channelListRequester.channelListResult(listNotSupported, null, null, false);
			return null;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.epics.pvaccess.client.ChannelProvider#createChannel(java.lang.String,
		 * org.epics.pvaccess.client.ChannelRequester, short)
		 */
		public Channel createChannel(String channelName, ChannelRequester channelRequester, short priority) {
			return createChannel(channelName, channelRequester, priority, null);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.epics.pvaccess.client.ChannelProvider#createChannel(java.lang.String,
		 * org.epics.pvaccess.client.ChannelRequester, short, java.lang.String[])
		 */
		public Channel createChannel(String channelName, ChannelRequester channelRequester, short priority,
				String address) {
			Channel channel;
			try {
				// TODO configurable
				short defaultPort = PVAConstants.PVA_SERVER_PORT;
				InetSocketAddress[] addressList = (address == null) ? null
						: InetAddressUtil.getSocketAddressList(address, defaultPort);
				channel = createChannelInternal(channelName, channelRequester, priority, addressList);
			} catch (IllegalArgumentException iae) {
				throw iae;
			} catch (Throwable th) {
				channelRequester.channelCreated(
						statusCreate.createStatus(StatusType.ERROR, "failed to create channel", th), null);
				return null;
			}
			channelRequester.channelCreated(okStatus, channel);
			return channel;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.epics.pvaccess.client.ChannelProvider#getProviderName()
		 */
		public String getProviderName() {
			return PROVIDER_NAME;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.epics.pvaccess.client.ChannelProvider#destroy()
		 */
		public void destroy() {
			dispose();
		}

	}

}
