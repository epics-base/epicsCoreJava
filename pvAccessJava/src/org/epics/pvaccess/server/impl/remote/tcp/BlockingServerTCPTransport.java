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

package org.epics.pvaccess.server.impl.remote.tcp;

import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.impl.remote.Context;
import org.epics.pvaccess.impl.remote.TransportClient;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.TransportSender;
import org.epics.pvaccess.impl.remote.request.ResponseHandler;
import org.epics.pvaccess.impl.remote.server.ChannelHostingTransport;
import org.epics.pvaccess.impl.remote.server.ServerChannel;
import org.epics.pvaccess.impl.remote.tcp.BlockingTCPTransport;
import org.epics.pvaccess.impl.security.NoSecurityPlugin;
import org.epics.pvaccess.impl.security.SecurityPluginMessageTransportSender;
import org.epics.pvaccess.plugins.SecurityPlugin;
import org.epics.pvaccess.plugins.SecurityPlugin.SecurityPluginControl;
import org.epics.pvaccess.plugins.SecurityPlugin.SecuritySession;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Server TCP transport implementation.
 *
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class BlockingServerTCPTransport extends BlockingTCPTransport
        implements ChannelHostingTransport, TransportSender, SecurityPluginControl {

    /**
     * Last SID cache.
     */
    private final AtomicInteger lastChannelSID = new AtomicInteger(0);

    /**
     * Channel table (SID -> channel mapping).
     */
    private final Map<Integer, ServerChannel> channels;

    /**
     * Server TCP transport constructor.
     *
     * @param context           context where transport lives in.
     * @param channel           used socket channel.
     * @param responseHandler   response handler used to process PVA headers.
     * @param receiveBufferSize receive buffer size.
     * @throws SocketException thrown on any socket exception.
     */
    public BlockingServerTCPTransport(Context context, SocketChannel channel, ResponseHandler responseHandler,
                                      int receiveBufferSize) throws SocketException {
        super(context, channel, responseHandler, receiveBufferSize, PVAConstants.PVA_DEFAULT_PRIORITY);
        // NOTE: priority not yet known, default priority is used to register/unregister
        // TODO implement priorities in Reactor... not that user will change it.. still
        // getPriority() must return "registered" priority!

        final int INITIAL_SIZE = 64;
        channels = Collections.synchronizedMap(new HashMap<Integer, ServerChannel>(INITIAL_SIZE));

        start();
    }

    /**
     * @see org.epics.pvaccess.impl.remote.tcp.BlockingTCPTransport#internalClose()
     */
    @Override
    protected void internalClose() {
        super.internalClose();
        destroyAllChannels();
    }

    /**
     * Destroy all channels.
     */
    private void destroyAllChannels() {

        context.getLogger().fine("Transport to " + socketAddress + " still has " + channels.size()
                + " channel(s) active and closing...");

        for (ServerChannel serverChannel : channels.values()) {
            serverChannel.destroy();
        }

        channels.clear();
    }

    /**
     * Preallocate new channel SID.
     *
     * @return new channel server id (SID).
     */
    public int preallocateChannelSID() {
        // search first free (theoretically possible loop of death)
        int sid = lastChannelSID.incrementAndGet();
        while (channels.containsKey(sid))
            sid = lastChannelSID.incrementAndGet();
        return sid;
    }

    /**
     * De-preallocate new channel SID.
     *
     * @param sid pre-allocated channel SID.
     */
    public void dePreAllocateChannelSID(int sid) {
        // noop
    }

    /**
     * Register a new channel.
     *
     * @param sid     pre-allocated channel SID.
     * @param channel channel to register.
     */
    public void registerChannel(int sid, ServerChannel channel) {
        channels.put(sid, channel);
    }

    /**
     * Unregister a new channel (and deallocates its handle).
     *
     * @param sid SID
     */
    public void unregisterChannel(int sid) {
        channels.remove(sid);
    }

    /**
     * Get channel by its SID.
     *
     * @param sid channel SID
     * @return channel with given SID, <code>null</code> otherwise
     */
    public ServerChannel getChannel(int sid) {

        return channels.get(sid);

    }

    public ServerChannel[] getChannels() {

        ServerChannel[] sca = new ServerChannel[channels.size()];
        channels.values().toArray(sca);
        return sca;

    }

    /**
     * Get channel count.
     *
     * @return channel count.
     */
    public int getChannelCount() {
        return channels.size();

    }

    /*
     * (non-Javadoc)
     *
     * @see org.epics.pvaccess.impl.remote.TransportSender#lock()
     */
    public void lock() {
        // noop
    }

    /*
     * (non-Javadoc)
     *
     * @see org.epics.pvaccess.impl.remote.TransportSender#unlock()
     */
    public void unlock() {
        // noop
    }

    // always called from the same thread, therefore no sync needed
    private boolean verifyOrVerified = false;

    /**
     * PVA connection validation request. A server sends a validate connection
     * message when it receives a new connection. The message indicates that the
     * server is ready to receive requests; the client must not send any messages on
     * the connection until it has received the validate connection message from the
     * server. No reply to the message is expected by the server. The purpose of the
     * validate connection message is two-fold: It informs the client of the
     * protocol version supported by the server. It prevents the client from writing
     * a request message to its local transport buffers until after the server has
     * acknowledged that it can actually process the request. This avoids a race
     * condition caused by the server's TCP/IP stack accepting connections in its
     * backlog while the server is in the process of shutting down: if the client
     * were to send a request in this situation, the request would be lost but the
     * client could not safely re-issue the request because that might violate
     * at-most-once semantics. The validate connection message guarantees that a
     * server is not in the middle of shutting down when the server's TCP/IP stack
     * accepts an incoming connection and so avoids the race condition.
     *
     * @see org.epics.pvaccess.impl.remote.TransportSender#send(java.nio.ByteBuffer,
     * org.epics.pvaccess.impl.remote.TransportSendControl)
     */
    public void send(ByteBuffer buffer, TransportSendControl control) {

        if (!verifyOrVerified) {
            verifyOrVerified = true;

            //
            // set byte order control message
            //

            ensureBuffer(PVAConstants.PVA_MESSAGE_HEADER_SIZE);
            sendBuffer.put(PVAConstants.PVA_MAGIC);
            sendBuffer.put(PVAConstants.PVA_VERSION);
            sendBuffer.put((byte) 0xc1); // control + server + big endian
            sendBuffer.put((byte) 2); // set byte order
            sendBuffer.putInt(0);

            //
            // send verification message
            //

            control.startMessage((byte) 1, 4 + 2);

            // receive buffer size
            buffer.putInt(getReceiveBufferSize());

            // server introspection registry max size
            // TODO
            buffer.putShort(Short.MAX_VALUE);

            // list of authNZ plugin names
            Map<String, SecurityPlugin> securityPlugins = context.getSecurityPlugins();
            List<String> validSPNames = new ArrayList<String>(securityPlugins.size());
            InetSocketAddress remoteAddress = (InetSocketAddress) channel.socket().getRemoteSocketAddress();
            for (SecurityPlugin securityPlugin : securityPlugins.values()) {
                try {
                    if (securityPlugin.isValidFor(remoteAddress))
                        validSPNames.add(securityPlugin.getId());
                } catch (Throwable th) {
                    context.getLogger().log(Level.SEVERE,
                            "Unexpected exception caught while calling SecurityPlugin.isValidFor(InetAddress)/getId() methods.",
                            th);
                }
            }

            int validSPCount = validSPNames.size();

            SerializeHelper.writeSize(validSPCount, buffer, this);
            for (String spName : validSPNames)
                SerializeHelper.serializeString(spName, buffer, this);

            securityRequired = (validSPCount > 0);
        } else {
            //
            // send verified message
            //

            control.startMessage((byte) 9, 0);

            verificationStatus.serialize(buffer, control);

        }
        // send immediately
        control.flush(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.epics.pvaccess.impl.remote.Transport#acquire(org.epics.pvaccess.impl.
     * remote.TransportClient)
     */
    public boolean acquire(TransportClient client) {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.epics.pvaccess.impl.remote.Transport#release(org.epics.pvaccess.impl.
     * remote.TransportClient)
     */
    public void release(TransportClient client) {
    }

    private static final Status validationTimeoutStatus = StatusFactory.getStatusCreate().createStatus(StatusType.ERROR,
            "server-side validation timeout", null);

    private volatile Status verificationStatus = validationTimeoutStatus;

    @Override
    public void verified(Status status) {
        verificationStatus = status;
        super.verified(status);
    }

    @Override
    public boolean verify(long timeoutMs) {
        enqueueSendRequest(this);

        boolean verified = super.verify(timeoutMs);

        enqueueSendRequest(this);

        return verified;
    }

    private static final Status invalidSecurityPluginNameStatus = StatusFactory.getStatusCreate()
            .createStatus(StatusType.ERROR, "invalid security plug-in name", null);

    public void authNZInitialize(Object data) {

        Object[] dataArray = (Object[]) data;
        String securityPluginName = (String) dataArray[0];
        PVField initializationData = (PVField) dataArray[1];

        InetSocketAddress remoteAddress = (InetSocketAddress) channel.socket().getRemoteSocketAddress();

        // check if plug-in name is valid
        SecurityPlugin securityPlugin = context.getSecurityPlugins().get(securityPluginName);
        if (securityPlugin == null) {
            if (securityRequired) {
                verified(invalidSecurityPluginNameStatus);
                return;
            } else {
                securityPlugin = NoSecurityPlugin.INSTANCE;
                context.getLogger().finer("No security plug-in installed, selecting default plug-in '"
                        + securityPlugin.getId() + "' for PVA client: " + remoteAddress);
            }
        }

        try {
            if (!securityPlugin.isValidFor(remoteAddress))
                verified(invalidSecurityPluginNameStatus);
        } catch (Throwable th) {
            context.getLogger().log(Level.SEVERE,
                    "Unexpected exception caught while calling SecurityPlugin.isValidFor(InetAddress) methods.", th);

            verified(StatusFactory.getStatusCreate().createStatus(StatusType.ERROR, "dysfunctional security plug-in",
                    th));
            return;
        }

        context.getLogger()
                .finer("Accepted security plug-in '" + securityPluginName + "' for PVA client: " + remoteAddress);

        // create session
        securitySession = securityPlugin.createSession(remoteAddress, this, initializationData);
    }

    private volatile SecuritySession securitySession = null;
    private volatile boolean securityRequired = true;

    public void authNZMessage(PVField data) {
        SecuritySession ss = securitySession;
        if (ss != null)
            ss.messageReceived(data);
        else
            context.getLogger().warning("authNZ message received but no security plug-in session active");
    }

    public void sendSecurityPluginMessage(PVField data) {
        // TODO not optimal since it allocates a new object every time
        enqueueSendRequest(new SecurityPluginMessageTransportSender(data));
    }

    public void authenticationCompleted(Status status) {

        context.getLogger().finer("Authentication completed with status '" + status.getType() + "' for PVA client: "
                + channel.socket().getRemoteSocketAddress());

        if (!verified)
            verified(status);
        else if (!status.isSuccess()) {
            String msg = "Re-authentication failed: " + status.getMessage();
            String stackDump = status.getStackDump();
            if (stackDump != null && stackDump.trim().length() != 0)
                msg += "\n" + stackDump;
            context.getLogger().info(msg);

            try {
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.epics.pvaccess.impl.remote.Transport#aliveNotification()
     */
    public void aliveNotification() {
        // noop on server-side
    }

    // TODO move to proper place
    @Override
    public void close() throws IOException {

        if (securitySession != null) {
            try {
                securitySession.close();
            } catch (Throwable th) {
                context.getLogger().log(Level.WARNING, "Unexpected exception caught while closing security session.",
                        th);
            }

            securitySession = null;
        }

        super.close();
    }

    public SecuritySession getSecuritySession() {
        return securitySession;
    }

}
