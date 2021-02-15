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

import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.client.*;
import org.epics.pvaccess.client.impl.remote.search.SearchInstance;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportClient;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.TransportSender;
import org.epics.pvaccess.impl.remote.request.ResponseRequest;
import org.epics.pvaccess.impl.remote.request.SubscriptionRequest;
import org.epics.pvaccess.impl.remote.utils.GUID;
import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.misc.Timer.TimerCallback;
import org.epics.pvdata.misc.Timer.TimerNode;
import org.epics.pvdata.misc.TimerFactory;
import org.epics.pvdata.monitor.Monitor;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.*;
import org.epics.pvdata.pv.Status.StatusType;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of PVAJ JCA <code>Channel</code>.
 *
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelImpl implements Channel, SearchInstance, TransportClient, TransportSender, TimerCallback {

    /**
     * Client channel ID.
     */
    protected final int channelID;

    /**
     * Channel name.
     */
    protected final String name;

    /**
     * Context.
     */
    protected final ClientContextImpl context;

    /**
     * Process priority.
     */
    protected final short priority;

    /**
     * List of fixed addresses, if <code>null</code> name resolution will be used.
     */
    protected final InetSocketAddress[] addresses;

    /**
     * Last reported connection status.
     */
    //protected boolean lastReportedConnectionState = false;

    /**
     * Connection status.
     */
    protected ConnectionState connectionState = ConnectionState.NEVER_CONNECTED;

    /**
     * Channel requester.
     */
    protected final ChannelRequester requester;

    /**
     * List of all channel's pending requests (keys are subscription IDs).
     */
    protected final Map<Integer, ResponseRequest> responseRequests = new HashMap<Integer, ResponseRequest>();

    /**
     * Allow reconnection flag.
     */
    protected boolean allowCreation = true;

    /**
     * Reference counting.
     * NOTE: synced on <code>this</code>.
     */
    protected int references = 1;

    /* ****************** */
    /* PVA protocol fields */
    /* ****************** */

    /**
     * Server transport.
     */
    protected Transport transport = null;

    /**
     * Server channel ID.
     */
    protected int serverChannelID = 0xFFFFFFFF;

    /**
     * User value used by SearchInstance.
     */
    private final AtomicInteger userValue = new AtomicInteger();

    /**
     * GUID of the server hosting the channel.
     */
    private GUID serverGUID = null;

    /* ****************** */

    protected ChannelImpl(ClientContextImpl context, int channelID, String name,
                          ChannelRequester requester, short priority, InetSocketAddress[] addresses) throws PVAException {
        this.context = context;
        this.channelID = channelID;
        this.name = name;
        this.priority = priority;
        this.addresses = addresses;
        this.requester = requester;

        // register before issuing search request
        context.registerChannel(this);

        // connect
        connect();
    }

    /**
     * Create a channel, i.e. submit create channel request to the server.
     * This method is called after search is complete.
     *
     * @param transport on what transport to create channel.
     */
    public synchronized void createChannel(Transport transport) {

        // do not allow duplicate creation to the same transport
        if (!allowCreation)
            return;
        allowCreation = false;

        // check existing transport
        if (this.transport != null && this.transport != transport) {
            disconnectPendingIO(false);
            this.transport.release(this);
        } else if (this.transport == transport) {
            // request to sent create request to same transport, ignore
            // this happens when server is slower (processing search requests) than client generating it
            return;
        }

        this.transport = transport;
        this.transport.enqueueSendRequest(this);
    }

    /**
     * @see org.epics.pvaccess.impl.remote.request.ResponseRequest#cancel()
     */
    public void cancel() {
        // noop
    }

    /**
     * @see org.epics.pvaccess.impl.remote.request.ResponseRequest#timeout()
     */
    public void timeout() {
        createChannelFailed();
    }

    /**
     * Create channel failed.
     */
    public synchronized void createChannelFailed() {
        cancel();

        if (transport != null) {
            transport.release(this);
            transport = null;
        }

        // ... and search again, with penalty
        initiateSearch(true);
    }

    /**
     * Called when channel created succeeded on the server.
     * <code>sid</code> might not be valid, this depends on protocol revision.
     *
     * @param sid server-side channel ID.
     * @throws IllegalStateException when called in wrong state.
     */
    public synchronized void connectionCompleted(int sid/*,  rights*/)
            throws IllegalStateException {
        try {
            // do this silently
            if (connectionState == ConnectionState.DESTROYED)
                return;

            // store data
            this.serverChannelID = sid;
            //setAccessRights(rights);

            addressIndex = 0;    // reset

            // TODO think what to call first
            resubscribeSubscriptions();
            setConnectionState(ConnectionState.CONNECTED);
        } finally {
            // end connection request
            cancel();
        }
    }

    public void channelDestroyedOnServer() {
        disconnect(true, false);
    }

    /**
     * @param force force destruction regardless of reference count
     * @throws PVAException          thrown on unexpected exception.
     * @throws IllegalStateException thrown if channel is already destroyed.
     */
    public synchronized void destroy(boolean force) throws PVAException, IllegalStateException {

        if (connectionState == ConnectionState.DESTROYED)
            throw new IllegalStateException("Channel already destroyed.");

        // do destruction via context
        context.destroyChannel(this, force);

    }

    /**
     * Increment reference.
     */
    public synchronized void acquire() {
        references++;
    }

    /**
     * Actual destroy method, to be called <code>CAJContext</code>.
     *
     * @param force force destruction regardless of reference count
     * @throws PVAException          thrown on unexpected exception.
     * @throws IllegalStateException thrown when channel is destroyed.
     * @throws IOException           thrown if IO exception occurs.
     */
    public synchronized void destroyChannel(boolean force) throws PVAException, IllegalStateException, IOException {

        if (connectionState == ConnectionState.DESTROYED)
            throw new IllegalStateException("Channel already destroyed.");

        references--;
        if (references > 0 && !force)
            return;

        // stop searching...
        context.getChannelSearchManager().unregister(this);
        cancel();

        disconnectPendingIO(true);

        if (connectionState == ConnectionState.CONNECTED) {
            disconnect(false, true);
        } else if (transport != null) {
            // unresponsive state, do not forget to release transport
            transport.release(this);
            transport = null;
        }

        setConnectionState(ConnectionState.DESTROYED);

        // unregister
        context.unregisterChannel(this);

		/*
		synchronized (accessRightsListeners)
		{
			accessRightsListeners.clear();
		}
		*/

		/*
		// this makes problem to the queued dispatchers...
		synchronized (connectionListeners)
		{
			connectionListeners.clear();
		}
		*/
    }

    /**
     * Disconnected notification.
     *
     * @param initiateSearch flag to indicate if searching (connect) procedure should be initiated
     * @param remoteDestroy  issue channel destroy request.
     */
    public synchronized void disconnect(boolean initiateSearch, boolean remoteDestroy) {
//System.err.println("CHANNEL disconnect");

        if (connectionState != ConnectionState.CONNECTED)
            return;

        if (!initiateSearch) {
            // stop searching...
            context.getChannelSearchManager().unregister(this);
            cancel();
        }
        setConnectionState(ConnectionState.DISCONNECTED);

        disconnectPendingIO(false);

        // release transport
        if (transport != null) {
            if (remoteDestroy) {
                issueCreateMessage = false;
                transport.enqueueSendRequest(this);
            }

            transport.release(this);
            transport = null;
        }

        if (initiateSearch)
            initiateSearch(false);

    }

    /**
     * Initiate search (connect) procedure.
     *
     * @param penalize register with penalty.
     */
    public synchronized void initiateSearch(boolean penalize) {
        allowCreation = true;

        if (addresses == null)
            context.getChannelSearchManager().register(this, penalize);
        else {
            context.getTimer().scheduleAfterDelay(timerNode,
                    ((float) addressIndex / addresses.length) * STATIC_SEARCH_BASE_DELAY_SEC);
        }
    }

    private int addressIndex = 0;
    private final TimerNode timerNode = TimerFactory.createNode(this);
    private final static int STATIC_SEARCH_BASE_DELAY_SEC = 5;
    private final static int STATIC_SEARCH_MAX_MULTIPLIER = 10;

    private static final GUID dummyGUID = new GUID(new byte[12]);

    public void callback() {
        // TODO not in this timer thread !!!
        // TODO boost when a server (from address list) is started!!! IP vs address !!!
        int ix = addressIndex % addresses.length;
        addressIndex++;
        if (addressIndex >= (addresses.length * (STATIC_SEARCH_MAX_MULTIPLIER + 1)))
            addressIndex = addresses.length * STATIC_SEARCH_MAX_MULTIPLIER;

        // NOTE: calls channelConnectFailed() on failure
        searchResponse(dummyGUID, PVAConstants.PVA_PROTOCOL_REVISION, addresses[ix]);
    }

    public void timerStopped() {
        // noop
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.impl.remote.search.SearchInstance#getUserValue()
     */
    public AtomicInteger getUserValue() {
        return userValue;
    }

    public synchronized void searchResponse(GUID guid, byte minorRevision, InetSocketAddress serverAddress) {
        // channel is already automatically unregistered

        Transport transport = getTransport();
        if (transport != null) {
            // TODO use GUID to determine whether there are multiple servers with the same channel
            // multiple defined PV or reconnect request (same server address)
            if (!transport.getRemoteAddress().equals(serverAddress) &&
                    !guid.equals(serverGUID)) {
                requester.message("More than one channel with name '" + name +
                        "' detected, connected to: " + transport.getRemoteAddress() + ", ignored: " + serverAddress, MessageType.warning);
                return;
            }
        }

        transport = context.getTransport(this, serverAddress, minorRevision, priority);
        if (transport == null) {
            createChannelFailed();
            return;
        }

        // remember GUID
        serverGUID = guid;

        // create channel
        createChannel(transport);
    }

    /**
     * @see org.epics.pvaccess.impl.remote.TransportClient#transportClosed()
     */
    public void transportClosed() {
//System.err.println("CHANNEL transportClosed");
        disconnect(true, false);
    }

    /**
     * @see org.epics.pvaccess.impl.remote.TransportClient#transportChanged()
     */
    public /*synchronized*/ void transportChanged() {
//System.err.println("CHANNEL transportChanged");
// this will be called immediately after reconnect... bad...
		/*
		if (connectionState == ConnectionState.CONNECTED)
		{
			disconnect(true, false);
		}
		*/
    }

    /**
     * @see org.epics.pvaccess.impl.remote.TransportClient#transportResponsive(org.epics.pvaccess.impl.remote.Transport)
     */
    public synchronized void transportResponsive(Transport transport) {
//System.err.println("CHANNEL transportResponsive");
        if (connectionState == ConnectionState.DISCONNECTED) {
            updateSubscriptions();

            // reconnect using existing IDs, data
            connectionCompleted(serverChannelID/*, accessRights*/);
        }
    }

    /**
     * @see org.epics.pvaccess.impl.remote.TransportClient#transportUnresponsive()
     */
    public synchronized void transportUnresponsive() {
//		System.err.println("CHANNEL transportUnresponsive");
        //if (connectionState == ConnectionState.CONNECTED)
        {
            // TODO 2 types of disconnected state - distinguish them otherwise disconnect will handle connection loss right
            // setConnectionState(ConnectionState.DISCONNECTED);
            // should we notify client at all?
        }
    }

    /**
     * Set connection state and if changed, notifies listeners.
     *
     * @param connectionState state to set.
     */
    private synchronized void setConnectionState(ConnectionState connectionState) {
        if (this.connectionState != connectionState) {
            this.connectionState = connectionState;

            try {
                requester.channelStateChange(this, connectionState);
            } catch (Throwable th) {
                // guard PVA code from exceptions
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                th.printStackTrace(printWriter);
                requester.message("Unexpected exception caught: " + writer, MessageType.fatalError);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#getConnectionState()
     */
    public synchronized ConnectionState getConnectionState() {
        return connectionState;
    }

    /**
     * NOTE: synchronization guarantees that <code>transport</code> is non-<code>null</code> and <code>state == CONNECTED</code>.
     *
     * @see org.epics.pvaccess.client.Channel#getRemoteAddress()
     */
    public synchronized String getRemoteAddress() {
        if (connectionState != ConnectionState.CONNECTED)
            return null;
        else
            return transport.getRemoteAddress().toString();
    }

    /**
     * Get client channel ID.
     *
     * @return client channel ID.
     */
    public int getChannelID() {
        return channelID;
    }

    /**
     * Get context.
     *
     * @return context.
     */
    public ClientContextImpl getContext() {
        return context;
    }

    /**
     * Checks if channel is in connected state,
     * if not throws <code>IllegalStateException</code> if not.
     *
     private final void connectionRequiredCheck()
     {
     if (connectionState != ConnectionState.CONNECTED)
     throw new IllegalStateException("Channel not connected.");
     }*/

    /**
     * Checks if channel is in connected state and returns transport.
     *
     * @return used transport.
     * @throws IllegalStateException if not connected.
     */
    public synchronized final Transport checkAndGetTransport() {
        if (connectionState == ConnectionState.DESTROYED)
            throw new IllegalStateException("Channel destroyed.");
        else if (connectionState != ConnectionState.CONNECTED)
            throw new IllegalStateException("Channel not connected.");
        return transport;        // TODO transport can be null !!!!!!!!!!
    }

    /**
     * Checks if channel is destroyed and returns transport, <code>null</code> not connected.
     *
     * @return used transport.
     * @throws IllegalStateException if not connected
     */
    public synchronized final Transport checkDestroyedAndGetTransport() {
        if (connectionState == ConnectionState.DESTROYED)
            throw new IllegalStateException("Channel destroyed.");
        else if (connectionState == ConnectionState.CONNECTED)
            return transport;
        else
            return null;
    }

    /**
     * Checks if channel is in connected or disconnected state,
     * if not throws <code>IllegalStateException</code> if not.
     *
     private final void checkState()
     {
     // connectionState is always non-null
     if (connectionState != ConnectionState.CONNECTED && connectionState != ConnectionState.DISCONNECTED)
     throw new IllegalStateException("Channel not in connected or disconnected state, state = '" + connectionState.name() + "'.");
     }*/

    /**
     * Checks if channel is not it closed state.
     * if not throws <code>IllegalStateException</code> if not.
     *
     private final synchronized void checkNotDestroyed()
     {
     if (connectionState == ConnectionState.DESTROYED)
     throw new IllegalStateException("Channel destroyed.");
     }*/

    /**
     * Get transport used by this channel.
     *
     * @return transport used by this channel.
     */
    public synchronized Transport getTransport() {
        return transport;
    }

    /**
     * Get SID.
     *
     * @return SID.
     */
    public synchronized int getServerChannelID() {
        return serverChannelID;
    }

    /**
     * Register a response request.
     *
     * @param responseRequest response request to register.
     */
    public void registerResponseRequest(ResponseRequest responseRequest) {
        synchronized (responseRequests) {
            responseRequests.put(responseRequest.getIOID(), responseRequest);
        }
    }

    /*
     * Unregister a response request.
     * @param responseRequest response request to unregister.
     */
    public void unregisterResponseRequest(ResponseRequest responseRequest) {
        synchronized (responseRequests) {
            responseRequests.remove(responseRequest.getIOID());
        }
    }

    private boolean needSubscriptionUpdate = false;

    private static final StatusCreate statusCreate = PVFactory.getStatusCreate();
    public static final Status channelDestroyed = statusCreate.createStatus(StatusType.WARNING, "channel destroyed", null);
    public static final Status channelDisconnected = statusCreate.createStatus(StatusType.WARNING, "channel disconnected", null);

    /**
     * Disconnects (destroys) all channels pending IO.
     *
     * @param destroy <code>true</code> if channel is being destroyed.
     */
    private void disconnectPendingIO(boolean destroy) {
        // TODO destroy????!!
        Status status;
        if (destroy)
            status = channelDestroyed;
        else
            status = channelDisconnected;

        synchronized (responseRequests) {
            needSubscriptionUpdate = true;

            ResponseRequest[] rrs = new ResponseRequest[responseRequests.size()];
            responseRequests.values().toArray(rrs);
            for (int i = 0; i < rrs.length; i++) {
                try {
                    rrs[i].reportStatus(status);
                } catch (Throwable th) {
                    // TODO remove
                    th.printStackTrace();
                }
            }
        }
    }

    /**
     * Resubscribe subscriptions.
     */
    // TODO to be called from non-transport thread !!!!!!
    private void resubscribeSubscriptions() {
        synchronized (responseRequests) {
            // sync get
            Transport transport = getTransport();

            ResponseRequest[] rrs = new ResponseRequest[responseRequests.size()];
            responseRequests.values().toArray(rrs);
            for (int i = 0; i < rrs.length; i++) {
                try {
                    if (rrs[i] instanceof SubscriptionRequest)
                        ((SubscriptionRequest) rrs[i]).resubscribeSubscription(transport);
                } catch (Throwable th) {
                    // TODO remove
                    th.printStackTrace();
                }
            }
        }
    }

    /**
     * Update subscriptions.
     */
    // TODO to be called from non-transport thread !!!!!!
    private void updateSubscriptions() {
        synchronized (responseRequests) {
            if (needSubscriptionUpdate)
                needSubscriptionUpdate = false;
            else
                return;    // noop

            ResponseRequest[] rrs = new ResponseRequest[responseRequests.size()];
            responseRequests.values().toArray(rrs);
            for (int i = 0; i < rrs.length; i++) {
                try {
                    if (rrs[i] instanceof SubscriptionRequest)
                        ((SubscriptionRequest) rrs[i]).updateSubscription();
                } catch (Throwable th) {
                    // TODO remove
                    th.printStackTrace();
                }
            }
        }
    }

    /**
     * Get process priority.
     *
     * @return process priority.
     */
    public short getPriority() {
        return priority;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public synchronized String toString() {
        StringBuilder buffy = new StringBuilder();
        buffy.append("CHANNEL  : ").append(name).append('\n');
        buffy.append("STATE    : ").append(connectionState).append('\n');
        if (connectionState == ConnectionState.CONNECTED) {
            buffy.append("ADDRESS  : ").append(getRemoteAddress()).append('\n');
            //buffy.append("RIGHTS   : ").append(getAccessRights()).append('\n');
        }
        return buffy.toString();
    }


    protected synchronized final void connect() {
        // if not destroyed...
        if (connectionState == ConnectionState.DESTROYED)
            throw new IllegalArgumentException("Channel destroyed.");
        else if (connectionState != ConnectionState.CONNECTED)
            initiateSearch(false);
    }

    protected synchronized void disconnect() {
        // if not destroyed...
        if (connectionState == ConnectionState.DESTROYED)
            throw new IllegalArgumentException("Channel destroyed.");
        else if (connectionState == ConnectionState.CONNECTED)
            disconnect(false, true);
    }

    public ChannelGet createChannelGet(
            ChannelGetRequester channelGetRequester,
            PVStructure pvRequest) {
        return ChannelGetRequestImpl.create(this, channelGetRequester, pvRequest);
    }

    public Monitor createMonitor(
            MonitorRequester monitorRequester, PVStructure pvRequest) {
        return ChannelMonitorImpl.create(this, monitorRequester, pvRequest);
    }

    public ChannelProcess createChannelProcess(
            ChannelProcessRequester channelProcessRequester,
            PVStructure pvRequest) {
        return ChannelProcessRequestImpl.create(this, channelProcessRequester, pvRequest);
    }

    public ChannelPut createChannelPut(
            ChannelPutRequester channelPutRequester,
            PVStructure pvRequest) {
        return ChannelPutRequestImpl.create(this, channelPutRequester, pvRequest);
    }

    public ChannelPutGet createChannelPutGet(
            ChannelPutGetRequester channelPutGetRequester,
            PVStructure pvRequest) {
        return ChannelPutGetRequestImpl.create(this, channelPutGetRequester, pvRequest);
    }

    public ChannelRPC createChannelRPC(ChannelRPCRequester channelRPCRequester, PVStructure pvRequest) {
        return ChannelRPCRequestImpl.create(this, channelRPCRequester, pvRequest);
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#getAccessRights(org.epics.pvdata.pv.PVField)
     */
    public org.epics.pvaccess.client.AccessRights getAccessRights(PVField pvField) {
        // TODO not implemented
        return AccessRights.readWrite;
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#getChannelName()
     */
    public String getChannelName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#getChannelRequester()
     */
    public ChannelRequester getChannelRequester() {
        return requester;
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#getProvider()
     */
    public ChannelProvider getProvider() {
        return context.getProvider();
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#isConnected()
     */
    public synchronized boolean isConnected() {
        return connectionState == ConnectionState.CONNECTED;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Requester#getRequesterName()
     */
    public String getRequesterName() {
        return requester.getRequesterName();
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Requester#message(java.lang.String, org.epics.pvdata.pv.MessageType)
     */
    public void message(String message, MessageType messageType) {
        // TODO
        System.err.println("[" + messageType + "] " + message);
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#destroy()
     */
    public void destroy() {
        try {
            destroy(false);
        } catch (IllegalStateException ise) {
            // noop on multiple destroys
        } catch (Throwable th) {
            throw new RuntimeException("Failed to destroy channel.", th);
        }
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#createChannelArray(org.epics.pvaccess.client.ChannelArrayRequester, java.lang.String, org.epics.pvdata.pv.PVStructure)
     */
    public ChannelArray createChannelArray(
            ChannelArrayRequester channelArrayRequester,
            PVStructure pvRequest) {
        return ChannelArrayRequestImpl.create(this, channelArrayRequester, pvRequest);
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.client.Channel#getField(org.epics.pvaccess.client.GetFieldRequester, java.lang.String)
     */
    public void getField(GetFieldRequester requester, String subField) {
        ChannelGetFieldRequestImpl.create(this, requester, subField);
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.impl.remote.TransportSender#lock()
     */
    public void lock() {
        // noop
    }

    private volatile boolean issueCreateMessage = true;

    /* (non-Javadoc)
     * @see org.epics.pvaccess.impl.remote.TransportSender#send(java.nio.ByteBuffer, org.epics.pvaccess.impl.remote.TransportSendControl)
     */
    public void send(ByteBuffer buffer, TransportSendControl control) {
        if (issueCreateMessage) {
            control.startMessage((byte) 7, (Short.SIZE + Integer.SIZE) / Byte.SIZE);

            // count
            buffer.putShort((short) 1);
            // array of CIDs and names
            buffer.putInt(channelID);
            SerializeHelper.serializeString(name, buffer, control);
            // send immediately
            // TODO really?
            control.flush(true);
        } else {
            control.startMessage((byte) 8, 2 * Integer.SIZE / Byte.SIZE);
            // SID
            buffer.putInt(getServerChannelID());
            // CID
            buffer.putInt(channelID);
            // send immediately
            // TODO really?
            control.flush(true);
        }
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.impl.remote.TransportSender#unlock()
     */
    public void unlock() {
        // noop
    }


}
