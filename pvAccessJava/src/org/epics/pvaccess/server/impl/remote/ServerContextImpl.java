/*
 * Copyright (c) 2006 by Cosylab
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

package org.epics.pvaccess.server.impl.remote;

import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.PVAVersion;
import org.epics.pvaccess.Version;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelProviderRegistry;
import org.epics.pvaccess.client.ChannelProviderRegistryFactory;
import org.epics.pvaccess.impl.remote.*;
import org.epics.pvaccess.impl.remote.request.ResponseHandler;
import org.epics.pvaccess.impl.remote.udp.BlockingUDPConnector;
import org.epics.pvaccess.impl.remote.udp.BlockingUDPTransport;
import org.epics.pvaccess.plugins.SecurityPlugin;
import org.epics.pvaccess.server.ServerContext;
import org.epics.pvaccess.server.impl.remote.tcp.BlockingTCPAcceptor;
import org.epics.pvaccess.server.plugins.BeaconServerStatusProvider;
import org.epics.pvaccess.util.InetAddressUtil;
import org.epics.pvaccess.util.configuration.Configuration;
import org.epics.pvaccess.util.configuration.ConfigurationProvider;
import org.epics.pvaccess.util.configuration.impl.ConfigurationFactory;
import org.epics.pvaccess.util.logging.ConsoleLogHandler;
import org.epics.pvdata.misc.ThreadPriority;
import org.epics.pvdata.misc.Timer;
import org.epics.pvdata.misc.TimerFactory;
import org.epics.util.compat.legacy.net.NetworkInterface;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of <code>ServerContext</code>.
 *
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ServerContextImpl implements ServerContext, Context {

    static {
        // force only IPv4 sockets, since EPICS does not work right with IPv6 sockets
        // see http://java.sun.com/j2se/1.5.0/docs/guide/net/properties.html
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    /**
     * Version.
     */
    public static final Version VERSION = new Version(
            "pvAccess Server", "Java",
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
         * State value of running context.
         */
        RUNNING,

        /**
         * State value of shutdown (once running) context.
         */
        SHUTDOWN,

        /**
         * State value of destroyed context.
         */
        DESTROYED
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
     * A space-separated list of broadcast address which to send beacons.
     * Each address must be of the form: ip.number:port or host.name:port
     */
    protected String beaconAddressList = "";

    /**
     * A space-separated list of address from which to ignore name resolution requests.
     * Each address must be of the form: ip.number:port or host.name:port
     */
    protected String ignoreAddressList = "";

    /**
     * Define whether or not the network interfaces should be discovered at runtime.
     */
    protected boolean autoBeaconAddressList = true;

    /**
     * Period in second between two beacon signals.
     */
    protected float beaconPeriod = 15.0f;

    /**
     * Broadcast port number to listen to.
     */
    protected int broadcastPort = PVAConstants.PVA_BROADCAST_PORT;

    /**
     * Port number for the server to listen to.
     */
    protected int serverPort = PVAConstants.PVA_SERVER_PORT;

    /**
     * Length in bytes of the maximum buffer (payload) size that may pass through PVA.
     */
    protected int receiveBufferSize = PVAConstants.MAX_TCP_RECV;

    /**
     * Timer.
     */
    protected Timer timer = null;

    /*
     * Reactor.
     */
    //protected Reactor reactor = null;

    /*
     * Leader/followers thread pool.
     */
    //protected LeaderFollowersThreadPool leaderFollowersThreadPool = null;

    /**
     * Broadcast transport needed for channel searches.
     */
//	protected UDPTransport broadcastTransport = null;
    protected BlockingUDPTransport broadcastTransport = null;

    /**
     * Local multicast transport needed for unicast
     * channel searches to be multicasted locally.
     */
//	protected UDPTransport localMulticastTransport = null;
    protected BlockingUDPTransport localMulticastTransport = null;

    /**
     * Beacon emitter.
     */
    protected BeaconEmitter beaconEmitter = null;

    /**
     * PVAS acceptor (accepts PVA virtual circuit).
     */
//	protected TCPAcceptor acceptor = null;
    protected BlockingTCPAcceptor acceptor = null;

    /**
     * PVA transport (virtual circuit) registry.
     * This registry contains all active transports - connections to PVA servers.
     */
    protected TransportRegistry transportRegistry = null;

    /**
     * Channel provider name.
     */
    protected String channelProviderNames = PVAConstants.PVA_DEFAULT_PROVIDER;

    /**
     * Channel provider.
     */
    protected final ArrayList<ChannelProvider> channelProviders = new ArrayList<ChannelProvider>();

    /**
     * Channel (name) to provider mapping.
     * Used when there are more that one provider used.
     */
    protected final Map<String, ChannelProvider> channelNameToProvider = new HashMap<String, ChannelProvider>();

    /**
     * Response handler.
     */
    private final ResponseHandler serverResponseHandler;

    /**
     * Run lock.
     */
    protected final Object runLock = new Object();

    /**
     * GUID.
     */
    private final byte[] guid = new byte[12];

    /**
     * Constructor.
     */
    public ServerContextImpl() {
        generateGUID();
        loadConfiguration();
        initializeLogger();
        initializeSecurityPlugins();

        this.serverResponseHandler = new ServerResponseHandler(this);
    }

    /**
     * Generate GUID.
     */
    private void generateGUID() {
        // put first 12-bytes of UUID into the byte-array
        UUID uuid4 = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(guid);
        bb.putLong(uuid4.getMostSignificantBits());
        bb.putInt((int) (uuid4.getLeastSignificantBits() >>> 32));
    }

    /**
     * Returns GUID (12-byte array)
     *
     * @return GUID.
     */
    public byte[] getGUID() {
        return guid;
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.server.ServerContext#getVersion()
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
                if (logger != null) {
                    logger.addHandler(new ConsoleLogHandler());
                }
        }
    }

    /**
     * Get configuration instance.
     *
     * @return the configuration.
     */
    public Configuration getConfiguration() {
        final ConfigurationProvider configurationProvider = ConfigurationFactory.getProvider();
        Configuration config = configurationProvider.getConfiguration("pvAccess-server");
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

        beaconAddressList = config.getPropertyAsString("EPICS_PVA_ADDR_LIST", beaconAddressList);
        beaconAddressList = config.getPropertyAsString("EPICS_PVAS_BEACON_ADDR_LIST", beaconAddressList);

        autoBeaconAddressList = config.getPropertyAsBoolean("EPICS_PVA_AUTO_ADDR_LIST", autoBeaconAddressList);
        autoBeaconAddressList = config.getPropertyAsBoolean("EPICS_PVAS_AUTO_BEACON_ADDR_LIST", autoBeaconAddressList);

        beaconPeriod = config.getPropertyAsFloat("EPICS_PVA_BEACON_PERIOD", beaconPeriod);
        beaconPeriod = config.getPropertyAsFloat("EPICS_PVAS_BEACON_PERIOD", beaconPeriod);

        serverPort = config.getPropertyAsInteger("EPICS_PVA_SERVER_PORT", serverPort);
        serverPort = config.getPropertyAsInteger("EPICS_PVAS_SERVER_PORT", serverPort);

        broadcastPort = config.getPropertyAsInteger("EPICS_PVA_BROADCAST_PORT", broadcastPort);
        broadcastPort = config.getPropertyAsInteger("EPICS_PVAS_BROADCAST_PORT", broadcastPort);

        receiveBufferSize = config.getPropertyAsInteger("EPICS_PVA_MAX_ARRAY_BYTES", receiveBufferSize);
        receiveBufferSize = config.getPropertyAsInteger("EPICS_PVAS_MAX_ARRAY_BYTES", receiveBufferSize);

        channelProviderNames = config.getPropertyAsString("EPICS_PVA_PROVIDER_NAMES", channelProviderNames);
        channelProviderNames = config.getPropertyAsString("EPICS_PVAS_PROVIDER_NAMES", channelProviderNames);

    }

    public void setChannelProviderNames(String providerNames) {
        channelProviderNames = providerNames;
    }

    /**
     * Check context state and tries to establish necessary state.
     *
     * @throws PVAException          any PVA exception.
     * @throws IllegalStateException thrown if context was already destroyed.
     */
    protected final void checkState() throws PVAException, IllegalStateException {
        if (state == State.DESTROYED)
            throw new IllegalStateException("Context destroyed.");
    }

    public synchronized void initialize(ChannelProviderRegistry providerRegistry) throws PVAException, IllegalStateException {
        if (providerRegistry == null)
            throw new IllegalArgumentException("non-null providerRegistry expected");

        if (state == State.DESTROYED)
            throw new IllegalStateException("Context destroyed.");
        else if (state != State.NOT_INITIALIZED)
            throw new IllegalStateException("Context already initialized.");

        if (channelProviderNames.equals(PVAConstants.PVA_ALL_PROVIDERS)) {
            StringBuffer names = new StringBuffer(64);
            for (String name : providerRegistry.getProviderNames()) {
                channelProviders.add(providerRegistry.getProvider(name));
                if (names.length() > 0) names.append(' ');
                names.append(name);
            }
            channelProviderNames = names.toString();
        } else {
            String[] names = channelProviderNames.split("\\s+");
            for (String name : names) {
                ChannelProvider channelProvider = providerRegistry.getProvider(name);
                if (channelProvider == null)
                    logger.warning("Channel provider with name '" + name + "' not available.");
                else
                    channelProviders.add(channelProvider);
            }
        }

        if (channelProviders.isEmpty())
            throw new RuntimeException("None of the specified channel providers are available: " + channelProviderNames + ".");

        internalInitialize();

        state = State.INITIALIZED;
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.server.ServerContext#initialize(org.epics.pvaccess.client.ChannelProvider)
     */
    public synchronized void initialize(ChannelProvider channelProvider) throws PVAException, IllegalStateException {
        if (channelProvider == null)
            throw new IllegalArgumentException("non-null channelProvider expected");

        if (state == State.DESTROYED)
            throw new IllegalStateException("Context destroyed.");
        else if (state != State.NOT_INITIALIZED)
            throw new IllegalStateException("Context already initialized.");

        channelProviders.add(channelProvider);
        channelProviderNames = channelProvider.getProviderName();

        internalInitialize();

        state = State.INITIALIZED;
    }

    private void internalInitialize() throws PVAException {

        timer = TimerFactory.create("pvAccess-server timer", ThreadPriority.lower);
        transportRegistry = new TransportRegistry();
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

				        }, "CAS reactor").start();
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

//		acceptor = new TCPAcceptor(this, serverPort, receiveBufferSize);
        acceptor = new BlockingTCPAcceptor(this, serverPort, receiveBufferSize);
        serverPort = acceptor.getBindAddress().getPort();

        // setup broadcast UDP transport
        initializeUDPTransport();

        beaconEmitter = new BeaconEmitter(ProtocolType.tcp.name(), broadcastTransport, this);
    }

    /**
     * Initialize UDP transport.
     */
    private void initializeUDPTransport() throws PVAException {

        // setup UDP transport
        try {
            // where to bind (listen) address
            InetSocketAddress listenLocalAddress = new InetSocketAddress(broadcastPort);

            // where to send address
            InetSocketAddress[] broadcastAddresses = InetAddressUtil.getBroadcastAddresses(broadcastPort);

//			UDPConnector broadcastConnector = new UDPConnector(this, true, broadcastAddresses, true);
            BlockingUDPConnector broadcastConnector = new BlockingUDPConnector(this, true, broadcastAddresses, true);

            broadcastTransport = (BlockingUDPTransport) broadcastConnector.connect(
//			broadcastTransport = (UDPTransport)broadcastConnector.connect(
                    null, serverResponseHandler,
                    listenLocalAddress, PVAConstants.PVA_PROTOCOL_REVISION,
                    PVAConstants.PVA_DEFAULT_PRIORITY);

            // set ignore address list
            if (ignoreAddressList != null && ignoreAddressList.length() > 0) {
                // we do not care about the port
                InetSocketAddress[] list = InetAddressUtil.getSocketAddressList(ignoreAddressList, 0);
                if (list != null && list.length > 0)
                    broadcastTransport.setIgnoredAddresses(list);
            }
            // set broadcast address list
            if (beaconAddressList != null && beaconAddressList.length() > 0) {
                // if auto is true, add it to specified list
                InetSocketAddress[] appendList = null;
                if (autoBeaconAddressList == true)
                    appendList = broadcastTransport.getSendAddresses();

                InetSocketAddress[] list = InetAddressUtil.getSocketAddressList(beaconAddressList, broadcastPort, appendList);
                if (list != null && list.length > 0)
                    broadcastTransport.setSendAddresses(list);
            }

            // TODO configurable local NIF, address
            // setup local broadcasting
            NetworkInterface localNIF = InetAddressUtil.getLoopbackNIF();
            if (localNIF != null) {
                try {
                    InetAddress group = InetAddress.getByName("224.0.0.128");
                    /*MembershipKey key =*/
                    broadcastTransport.join(group, localNIF);

                    InetSocketAddress anyAddress = new InetSocketAddress(0);
                    // NOTE: localMulticastTransport is not started (no read is called on a socket)
                    localMulticastTransport = (BlockingUDPTransport) broadcastConnector.connect(
//					localMulticastTransport = (UDPTransport)broadcastConnector.connect(
                            null, serverResponseHandler,
                            anyAddress, PVAConstants.PVA_PROTOCOL_REVISION,
                            PVAConstants.PVA_DEFAULT_PRIORITY);
                    localMulticastTransport.setMulticastNIF(localNIF, true);
                    localMulticastTransport.setSendAddresses(new InetSocketAddress[]{
                            new InetSocketAddress(group, broadcastPort)
                    });

                    logger.config("Local multicast enabled on " + group + ":" + broadcastPort + " using " + localNIF.getDisplayName() + ".");
                } catch (Throwable th) {
                    if (localMulticastTransport != null) {
                        try {
                            localMulticastTransport.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        localMulticastTransport = null;
                    }
                    logger.log(Level.CONFIG, "Failed to initialize local multicast, functionality disabled.", th);
                }
            } else {
                logger.config("Failed to detect a loopback network interface, local multicast disabled.");
            }

            broadcastTransport.start();
        } catch (ConnectionException ce) {
            throw new PVAException("Failed to initialize broadcast UDP transport", ce);
        }

    }

    private boolean runTerminated;

    /**
     * Run server (process events).
     *
     * @param seconds time in seconds the server will process events (method will block), if <code>0</code>
     *                the method would block until <code>destroy()</code> is called.
     * @throws IllegalStateException if server is already destroyed.
     * @throws PVAException          exception.
     */
    public void run(int seconds) throws PVAException, IllegalStateException {
        if (seconds < 0)
            throw new IllegalArgumentException("seconds cannot be negative.");

        if (state == State.NOT_INITIALIZED)
            throw new IllegalStateException("Context not initialized.");
        else if (state == State.DESTROYED)
            throw new IllegalStateException("Context destroyed.");
        else if (state == State.RUNNING)
            throw new IllegalStateException("Context is already running.");
        else if (state == State.SHUTDOWN)
            throw new IllegalStateException("Context was shutdown.");

        synchronized (this) {
            if (state == State.SHUTDOWN)
                throw new IllegalStateException("Context was shutdown.");

            state = State.RUNNING;
        }

        // run...
        beaconEmitter.start();
        synchronized (runLock) {
            runTerminated = false;
            try {
                final long timeToWait = seconds * 1000L;
                final long start = System.currentTimeMillis();
                while (!runTerminated && (timeToWait == 0 || ((System.currentTimeMillis() - start) < timeToWait)))
                    runLock.wait(timeToWait);
            } catch (InterruptedException e) { /* noop */ }
        }

        synchronized (this) {
            state = State.SHUTDOWN;
        }

    }


    /* (non-Javadoc)
     * @see org.epics.pvaccess.server.ServerContext#shutdown()
     */
    public synchronized void shutdown() throws PVAException, IllegalStateException {

        if (state == State.DESTROYED)
            throw new IllegalStateException("Context already destroyed.");

        // notify to stop running...
        synchronized (runLock) {
            runTerminated = true;
            runLock.notifyAll();
        }
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.server.ServerContext#destroy()
     */
    public synchronized void destroy() throws PVAException, IllegalStateException {

        if (state == State.DESTROYED)
            throw new IllegalStateException("Context already destroyed.");

        // shutdown if not already
        shutdown();

        // go into destroyed state ASAP
        state = State.DESTROYED;

        internalDestroy();

    }

    private void internalDestroy() throws PVAException {
        // stop responding to search requests
        if (broadcastTransport != null) {
            try {
                broadcastTransport.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // and close local multicast transport
        if (localMulticastTransport != null) {
            try {
                localMulticastTransport.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // stop accepting connections
        if (acceptor != null)
            acceptor.destroy();

        // stop emitting beacons
        if (beaconEmitter != null)
            beaconEmitter.destroy();

        // stop timer
        if (timer != null)
            timer.stop();

        //
        // cleanup
        //

        // this will also destroy all channels
        destroyAllTransports();
		/*
		// shutdown reactor
		if (reactor != null)
			reactor.shutdown();

		// shutdown LF thread pool
		if (leaderFollowersThreadPool != null)
		    leaderFollowersThreadPool.shutdown();
		*/
    }

    /**
     * Destroy all transports.
     */
    private void destroyAllTransports() {

        // not initialized yet
        if (transportRegistry == null)
            return;

        Transport[] transports = transportRegistry.toArray();

        if (transports.length == 0)
            return;

        logger.fine("Server context still has " + transports.length + " transport(s) active and closing...");

        for (Transport transport : transports) {
            try {
                transport.close();
            } catch (Throwable th) {
                // do all exception safe, print stack in case of an error
                th.printStackTrace();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.server.ServerContext#printInfo()
     */
    public void printInfo() {
        printInfo(System.out);
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.server.ServerContext#printInfo(java.io.PrintStream)
     */
    public void printInfo(PrintStream out) {
        out.println("CLASS   : " + getClass().getName());
        out.println("VERSION : " + getVersion());
        out.println("PROVIDER_NAMES : " + channelProviderNames);
        out.println("BEACON_ADDR_LIST : " + beaconAddressList);
        out.println("AUTO_BEACON_ADDR_LIST : " + autoBeaconAddressList);
        out.println("BEACON_PERIOD : " + beaconPeriod);
        out.println("BROADCAST_PORT : " + broadcastPort);
        out.println("SERVER_PORT : " + serverPort);
        out.println("RCV_BUFFER_SIZE : " + receiveBufferSize);
        out.println("IGNORE_ADDR_LIST: " + ignoreAddressList);
        out.println("STATE : " + state.name());
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.server.ServerContext#dispose()
     */
    public void dispose() {
        try {
            destroy();
        } catch (Throwable th) {
            // noop
        }
    }

    /**
     * Get initialization status.
     *
     * @return initialization status.
     */
    public boolean isInitialized() {
        return state == State.INITIALIZED || state == State.RUNNING || state == State.SHUTDOWN;
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
     * Get beacon address list.
     *
     * @return beacon address list.
     */
    public String getBeaconAddressList() {
        return beaconAddressList;
    }

    /**
     * Get beacon address list auto flag.
     *
     * @return beacon address list auto flag.
     */
    public boolean isAutoBeaconAddressList() {
        return autoBeaconAddressList;
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
     * Get receiver buffer (payload) size.
     *
     * @return max payload size.
     */
    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    /**
     * Get server port.
     *
     * @return server port.
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Set server port number.
     *
     * @param port new server port number.
     */
    public void setServerPort(int port) {
        serverPort = port;
    }

    /**
     * Get broadcast port.
     *
     * @return broadcast port.
     */
    public int getBroadcastPort() {
        return broadcastPort;
    }

    /**
     * Get ignore search address list.
     *
     * @return ignore search address list.
     */
    public String getIgnoreAddressList() {
        return ignoreAddressList;
    }

    // ************************************************************************** //

    /**
     * Beacon server status provider interface (optional).
     */
    private BeaconServerStatusProvider beaconServerStatusProvider = null;

    /**
     * Get registered beacon server status provider.
     *
     * @return registered beacon server status provider.
     */
    public BeaconServerStatusProvider getBeaconServerStatusProvider() {
        return beaconServerStatusProvider;
    }

    /**
     * Set beacon server status provider.
     *
     * @param beaconServerStatusProvider <code>BeaconServerStatusProvider</code> implementation to set
     */
    public void setBeaconServerStatusProvider(BeaconServerStatusProvider beaconServerStatusProvider) {
        this.beaconServerStatusProvider = beaconServerStatusProvider;
    }

    // ************************************************************************** //

    /**
     * Get server network (IP) address.
     *
     * @return server network (IP) address, <code>null</code> if not bounded.
     */
    public InetAddress getServerInetAddress() {
        return (acceptor != null) ?
                acceptor.getBindAddress().getAddress() : null;
    }

    /**
     * Broadcast transport.
     *
     * @return broadcast transport.
     */
    public BlockingUDPTransport getBroadcastTransport() {
//	public UDPTransport getBroadcastTransport() {
        return broadcastTransport;
    }

    /**
     * Local multicast transport.
     *
     * @return multicast transport.
     */
    public BlockingUDPTransport getLocalMulticastTransport() {
//	public UDPTransport getLocalMulticastTransport() {
        return localMulticastTransport;
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

    // NOTE order must be preserved
    private final Map<String, SecurityPlugin> securityPlugins = new LinkedHashMap<String, SecurityPlugin>();

    public Map<String, SecurityPlugin> getSecurityPlugins() {
        return securityPlugins;
    }

    private void initializeSecurityPlugins() {
        String classes = System.getProperty(SecurityPlugin.SECURITY_PLUGINS_SERVER_KEY);
        if (classes != null) {
            StringTokenizer tokens = new StringTokenizer(classes, ",");

            while (tokens.hasMoreElements()) {
                String className = tokens.nextToken().trim();
                logger.log(Level.FINER, "Loading security plug-in '" + className + "'...");

                try {
                    final Class<?> c = Class.forName(className);
                    SecurityPlugin p = (SecurityPlugin) c.newInstance();        // TODO in the future any specific method can be used
                    securityPlugins.put(p.getId(), p);
                    logger.log(Level.FINER, "Security plug-in '" + className + "' [" + p.getId() + "] loaded.");
                } catch (Throwable th) {
                    logger.log(Level.WARNING, "Failed to load security plug-in '" + className + "'.", th);
                }
            }
        }

        logger.log(Level.FINE, "Installed security plug-ins: " + securityPlugins.keySet() + ".");
    }


    /*
     * Get LF thread pool.
     * @return LF thread pool, can be <code>null</code> if disabled.
     *
    public LeaderFollowersThreadPool getLeaderFollowersThreadPool() {
    return leaderFollowersThreadPool;
    }*/

    /**
     * Get channel provider name.
     *
     * @return channel provider name.
     */
    public String getChannelProviderNames() {
        return channelProviderNames;
    }

    /**
     * Get channel providers.
     *
     * @return channel provider.
     */
    public List<ChannelProvider> getChannelProviders() {
        return channelProviders;
    }

    /**
     * Return channel (name) to provider mapping.
     *
     * @return the map.
     */
    public Map<String, ChannelProvider> getChannelNameToProviderMap() {
        return channelNameToProvider;
    }

    /**
     * Get server response handler.
     *
     * @return server response handler.
     */
    public ResponseHandler getServerResponseHandler() {
        return serverResponseHandler;
    }

    /**
     * Create <code>ServerContextImpl</code> instance and start server.
     *
     * @param providerNames       providers to use, <code>null</code> to use defaults or
     *                            <code>PVAConstants.PVA_ALL_PROVIDERS</code> to use all providers.
     * @param timeToRun           time (in seconds) to run, <code>0</code> until {@link #destroy()} is called.
     * @param runInSeparateThread run in separate thread flag.
     * @param printInfoStream     stream instance where to print context info, can be <code>null</code>
     * @return the server context instance.
     * @throws PVAException thrown on exception.
     */
    public static ServerContextImpl startPVAServer(String providerNames, final int timeToRun,
                                                   boolean runInSeparateThread, PrintStream printInfoStream)
            throws PVAException {
        final ServerContextImpl context = new ServerContextImpl();
        if (providerNames != null)
            context.setChannelProviderNames(providerNames);

        context.initialize(ChannelProviderRegistryFactory.getChannelProviderRegistry());

        if (printInfoStream != null) {
            printInfoStream.println(context.getVersion().getVersionString());
            context.printInfo(printInfoStream);
        }

        if (runInSeparateThread) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        context.run(timeToRun);
                    } catch (Throwable th) {
                        Logger logger = Logger.getLogger(this.getClass().getName());
                        logger.log(Level.SEVERE, "Unhandled exception caught.", th);
                    }
                }
            }, "startPVAServer").start();
        } else
            context.run(timeToRun);

        return context;
    }

}
