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

package org.epics.pvaccess.impl.remote.udp;

import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.impl.remote.*;
import org.epics.pvaccess.impl.remote.request.ResponseHandler;
import org.epics.pvaccess.plugins.SecurityPlugin.SecuritySession;
import org.epics.pvaccess.server.ServerContext;
import org.epics.pvaccess.util.InetAddressUtil;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.Status;
import org.epics.util.compat.legacy.net.NetworkInterface;

import java.io.IOException;
import java.net.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.UnresolvedAddressException;
import java.util.Set;
import java.util.logging.Level;


/**
 * PVA UDP transport implementation.
 *
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class BlockingUDPTransport implements Transport, TransportSendControl {

    /**
     * Context instance.
     */
    private final Context context;

    /**
     * Corresponding channel.
     */
    private final MulticastSocket channel;

    /**
     * Cached socket address.
     */
    private InetSocketAddress socketAddress;

    /**
     * Bind address.
     */
    private final InetSocketAddress bindAddress;

    /**
     * Send addresses.
     */
    private InetSocketAddress[] sendAddresses;

    /**
     * Send addresses.
     */
    private boolean[] isSendAddressUnicast;

    /**
     * Ignore addresses.
     */
    private InetSocketAddress[] ignoredAddresses = null;

    /**
     * Receive buffer.
     */
    private final ByteBuffer receiveBuffer;
    private final DatagramPacket legacyReceiveBuffer;
    private final byte[] legacyReceiveByteArray = new byte[PVAConstants.MAX_UDP_PACKET];

    /**
     * Send buffer.
     */
    private final ByteBuffer sendBuffer;
    private final DatagramPacket legacySendBuffer;
    private final byte[] legacySendByteArray = new byte[PVAConstants.MAX_UDP_UNFRAGMENTED_SEND];

    /**
     * Response handler.
     */
    protected final ResponseHandler responseHandler;

    /**
     * Closed status.
     */
    protected volatile boolean closed = false;

    /**
     * Last message start position.
     */
    private int lastMessageStartPosition = 0;

    /**
     * Client/server flag (including big endian flag).
     */
    private final int clientServerWithBigEndianFlag;

    public BlockingUDPTransport(Context context, ResponseHandler responseHandler, MulticastSocket channel,
                                InetSocketAddress bindAddress, InetSocketAddress[] sendAddresses,
                                short remoteTransportRevision) {
        this.context = context;
        this.clientServerWithBigEndianFlag = (context instanceof ServerContext) ? 0xC0 : 0x80;
        this.responseHandler = responseHandler;
        this.channel = channel;
        this.bindAddress = bindAddress;
        setSendAddresses(sendAddresses);

        try {
            this.socketAddress = (InetSocketAddress) channel.getLocalSocketAddress();
        } catch (Throwable th) {
            context.getLogger().log(Level.FINER, "Failed to obtain local socket address.", th);
        }

        // allocate receive buffer
        this.receiveBuffer = ByteBuffer.allocate(PVAConstants.MAX_UDP_PACKET);
        this.legacyReceiveBuffer = new DatagramPacket(this.legacyReceiveByteArray, PVAConstants.MAX_UDP_PACKET);

        // allocate send buffer and non-reentrant lock
        this.sendBuffer = ByteBuffer.allocate(PVAConstants.MAX_UDP_UNFRAGMENTED_SEND);
        this.legacySendBuffer = new DatagramPacket(this.legacySendByteArray, PVAConstants.MAX_UDP_UNFRAGMENTED_SEND);
    }

    /**
     * Start processing requests.
     */
    public void start() {
        new Thread(new Runnable() {

            public void run() {
                while (!closed) {
                    try {
                        processRead();
                    } catch (Throwable th) {
                        context.getLogger().log(Level.FINE, "Uncaught exception caught.", th);
                    }
                }
            }
        }, "UDP-receive " + this.socketAddress).start();
    }

    /**
     * Close transport.
     */
    public void close() throws IOException {
        if (this.closed)
            return;
        this.closed = true;

        if (this.bindAddress != null)
            context.getLogger().finer("UDP connection to " + this.bindAddress + " closed.");
        //context.getReactor().unregisterAndClose(channel);
        // TODO this just does not exit socket.receive()!!!!
        // maybe try with setSoTimeout(int timeout)
        this.channel.close();
    }

    /* (non-Javadoc)
     * @see java.nio.channels.Channel#isOpen()
     */
    public boolean isOpen() {
        return !this.closed;
    }


    /* (non-Javadoc)
     * @see org.epics.pvaccess.impl.remote.Transport#acquire(org.epics.pvaccess.impl.remote.TransportClient)
     */
    public boolean acquire(TransportClient client) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.impl.remote.Transport#release(org.epics.pvaccess.impl.remote.TransportClient)
     */
    public void release(TransportClient client) {
    }

    /**
     * Process input (read) IO event.
     */
    protected void processRead() {
        try {
            while (!this.closed) {

                // reset header buffer
                this.receiveBuffer.clear();

                // read to buffer
                // NOTE: If there are fewer bytes remaining in the buffer
                // than are required to hold the datagram then the remainder
                // of the datagram is silently discarded.
                this.legacyReceiveBuffer.setData(this.legacyReceiveByteArray);
                this.channel.receive(this.legacyReceiveBuffer);
                this.receiveBuffer.put(this.legacyReceiveBuffer.getData());

                final InetAddress fromAddressOnly = this.legacyReceiveBuffer.getAddress();
                final InetSocketAddress fromAddress = new InetSocketAddress(fromAddressOnly, this.legacyReceiveBuffer.getPort());

                // check if datagram not available
                // NOTE: If this channel is in non-blocking mode and a datagram is not
                // immediately available then this method immediately returns <tt>null</tt>.
//                if (fromAddress == null)
//                    break;

                // check if received from ignore address list
                if (this.ignoredAddresses != null) {
                    boolean ignore = false;

                    for (InetSocketAddress ignoredAddress : this.ignoredAddresses)
                        if (ignoredAddress.getAddress().equals(fromAddressOnly)) {
                            ignore = true;
                            break;
                        }

                    if (ignore)
                        continue;
                }

                //context.getLogger().finest("Received " + receiveBuffer.position() + " bytes from " + fromAddress + ".");

                // prepare buffer for reading
                this.receiveBuffer.flip();

                // process
                processBuffer(fromAddress, this.receiveBuffer);
            }

        } catch (AsynchronousCloseException ace) {
            try {
                close();
            } catch (IOException e) {
                this.context.getLogger().log(Level.WARNING, "IO Exception: " + e.getMessage());
            }
        } catch (ClosedChannelException cce) {
            try {
                close();
            } catch (IOException e) {
                this.context.getLogger().log(Level.WARNING, "IO Exception: " + e.getMessage());
            }
        } catch (IOException ioex) {
            this.context.getLogger().log(Level.WARNING, "IO Exception: " + ioex.getMessage());
        }
    }


    /**
     * Process buffer.
     * Buffer can contain several messages. Last message must be completed (not partial).
     *
     * @return success flag.
     */
    private boolean processBuffer(final InetSocketAddress fromAddress, final ByteBuffer receiveBuffer) {

        // handle response(s)
        while (receiveBuffer.remaining() >= PVAConstants.PVA_MESSAGE_HEADER_SIZE) {
            //
            // read header
            //

            // first byte is PVA_MAGIC
            final byte magic = receiveBuffer.get();
            if (magic != PVAConstants.PVA_MAGIC)
                return false;

            // second byte version - major/minor nibble
            // check only major version for compatibility
            final byte version = receiveBuffer.get();

            final byte flags = receiveBuffer.get();
            if ((flags & 0x80) != 0) {
                // 7th bit is set
                receiveBuffer.order(ByteOrder.BIG_ENDIAN);
            } else {
                receiveBuffer.order(ByteOrder.LITTLE_ENDIAN);
            }

            // command ID and payload
            final byte command = receiveBuffer.get();
            final int payloadSize = receiveBuffer.getInt();

            // control message check (skip message)
            if ((flags & 0x01) != 0)
                continue;

            final int nextRequestPosition = receiveBuffer.position() + payloadSize;

            // payload size check
            if (nextRequestPosition > receiveBuffer.limit())
                return false;

            // handle
            this.responseHandler.handleResponse(fromAddress, this, version, command, payloadSize, receiveBuffer);

            // set position (e.g. in case handler did not read all)
            receiveBuffer.position(nextRequestPosition);
        }

        // all OK
        return true;
    }

    /**
     * Process output (write) IO event.
     */
    protected void processWrite() {
        // noop (not used for datagrams)
    }

    /**
     * InetAddress type.
     */
    public enum InetAddressType {ALL, UNICAST, BROADCAST_MULTICAST}

    /**
     * Send a buffer through the transport.
     *
     * @param byteBuffer buffer to send.
     * @param target     filter (selector) of what addresses to use when sending.
     * @return success status.
     */
    public boolean send(ByteBuffer byteBuffer, InetAddressType target) {
        // noop check
        if (this.sendAddresses == null)
            return false;

        for (int i = 0; i < this.sendAddresses.length; i++) {
            // filter
            if (target != InetAddressType.ALL)
                if ((target == InetAddressType.UNICAST && !this.isSendAddressUnicast[i]) ||
                        (target == InetAddressType.BROADCAST_MULTICAST && this.isSendAddressUnicast[i]))
                    continue;

            try {
                // prepare buffer
                byteBuffer.flip();
                this.legacySendBuffer.setData(byteBuffer.array());
                this.legacySendBuffer.setAddress(this.sendAddresses[i].getAddress());
                this.legacySendBuffer.setPort(this.sendAddresses[i].getPort());

                //context.getLogger().finest("Sending " + buffer.limit() + " bytes to " + sendAddresses[i] + ".");
                this.channel.send(this.legacySendBuffer);
            } catch (NoRouteToHostException noRouteToHostException) {
                this.context.getLogger().log(Level.FINER, "No route to host exception caught when sending to: " + this.sendAddresses[i] + ".", noRouteToHostException);
            } catch (UnresolvedAddressException uae) {
                this.context.getLogger().log(Level.FINER, "Unresolved address exception caught when sending to: " + this.sendAddresses[i] + ".", uae);
            } catch (Throwable th) {
                this.context.getLogger().log(Level.FINER, "Exception caught when sending to: " + this.sendAddresses[i] + ".", th);
            }
        }

        return true;
    }

    /**
     * Send a buffer through the transport.
     *
     * @param buffer buffer to send.
     * @return success status.
     */
    public boolean send(ByteBuffer buffer) {
        return send(buffer, InetAddressType.ALL);
    }

    /**
     * Send a buffer through the transport immediately.
     *
     * @param byteBuffer buffer to send.
     * @param address    send address.
     */
    public void send(ByteBuffer byteBuffer, InetSocketAddress address) {
        try {
            // context.getLogger().finest("Sending " + buffer.limit() + " bytes to " + address + ".");
            byteBuffer.flip();
            this.legacySendBuffer.setData(byteBuffer.array());
            this.legacySendBuffer.setAddress(address.getAddress());
            this.legacySendBuffer.setPort(address.getPort());
            this.channel.send(this.legacySendBuffer);
        } catch (NoRouteToHostException noRouteToHostException) {
            this.context.getLogger().log(Level.FINER, "No route to host exception caught when sending to: " + address + ".", noRouteToHostException);
        } catch (UnresolvedAddressException uae) {
            this.context.getLogger().log(Level.FINER, "Unresolved address exception caught when sending to: " + address + ".", uae);
        } catch (Throwable th) {
            this.context.getLogger().log(Level.FINER, "Exception caught when sending to: " + address + ".", th);
        }
    }

    public void join(InetAddress group, NetworkInterface nif) throws IOException {
        this.channel.joinGroup(group);
    }

    // set NIF used to send packets
    public void setMulticastNIF(NetworkInterface nif, boolean loopback) throws IOException {
        this.channel.setLoopbackMode(true);
        this.channel.setNetworkInterface(nif.getNetworkInterface());
    }

    /**
     * @see org.epics.pvaccess.impl.remote.Transport#getRevision()
     */
    public byte getRevision() {
        return PVAConstants.PVA_PROTOCOL_REVISION;
    }

    /**
     * Get protocol type (e.g. tpc, udp, ssl, etc.).
     *
     * @see org.epics.pvaccess.impl.remote.Transport#getType()
     */
    public String getType() {
        return ProtocolType.udp.name();
    }

    /**
     * @see org.epics.pvaccess.impl.remote.Transport#changedTransport()
     */
    public void changedTransport() {
        // noop
    }

    /**
     * @see org.epics.pvaccess.impl.remote.Transport#getContext()
     */
    public Context getContext() {
        return this.context;
    }

    /**
     * @see org.epics.pvaccess.impl.remote.Transport#getPriority()
     */
    public short getPriority() {
        return PVAConstants.PVA_DEFAULT_PRIORITY;
    }

    /**
     * Flush...
     *
     * @return success status.
     */
    public boolean flush() {
        // noop since all UDP requests are sent immediately
        return true;
    }

    /**
     * @see org.epics.pvaccess.impl.remote.Transport#getRemoteAddress()
     */
    public InetSocketAddress getRemoteAddress() {
        return this.socketAddress;
    }

    /**
     * Get list of send addresses.
     *
     * @return send addresses.
     */
    public InetSocketAddress[] getSendAddresses() {
        return this.sendAddresses;
    }

    /**
     * Get list of ignored addresses.
     *
     * @return ignored addresses.
     */
    public InetSocketAddress[] getIgnoredAddresses() {
        return this.ignoredAddresses;
    }

    /**
     * Get bind address.
     *
     * @return bind address.
     */
    public InetSocketAddress getBindAddress() {
        return this.bindAddress;
    }

    /**
     * Set list of send addresses.
     *
     * @param addresses list of send addresses, non-<code>null</code>.
     */
    public void setSendAddresses(InetSocketAddress[] addresses) {
        this.sendAddresses = addresses;

        this.isSendAddressUnicast = new boolean[this.sendAddresses.length];
        Set<InetAddress> broadcastAddresses = InetAddressUtil.getBroadcastAddresses();
        for (int i = 0; i < this.sendAddresses.length; i++) {
            InetAddress address = this.sendAddresses[i].getAddress();
            // address == null if unresolved
            // unicast = not broadcast and not multicast
            this.isSendAddressUnicast[i] = (address == null) ||
                    (!broadcastAddresses.contains(address) &&
                            !address.isMulticastAddress());
        }
    }

    /**
     * Set ignore list.
     *
     * @param addresses list of ignored addresses.
     */
    public void setIgnoredAddresses(InetSocketAddress[] addresses) {
        this.ignoredAddresses = addresses;
    }

    /**
     * @see org.epics.pvaccess.impl.remote.Transport#getReceiveBufferSize()
     */
    public int getReceiveBufferSize() {
        return this.receiveBuffer.capacity();
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.impl.remote.Transport#setRemoteMinorRevision(byte)
     */
    public void setRemoteRevision(byte minor) {
        // noop
    }

    /**
     * @see org.epics.pvaccess.impl.remote.Transport#setRemoteTransportReceiveBufferSize(int)
     */
    public void setRemoteTransportReceiveBufferSize(int receiveBufferSize) {
        // noop for UDP (limited by 64k; PVAConstants.MAX_UDP_SEND for PVA)
    }

    /**
     * @see org.epics.pvaccess.impl.remote.Transport#setRemoteTransportSocketReceiveBufferSize(int)
     */
    public void setRemoteTransportSocketReceiveBufferSize(int socketReceiveBufferSize) {
        // noop for UDP (limited by 64k; PVAConstants.MAX_UDP_SEND for PVA)
    }

    /**
     * @see org.epics.pvaccess.impl.remote.Transport#getSocketReceiveBufferSize()
     */
    public int getSocketReceiveBufferSize() {
        try {
            return channel.getReceiveBufferSize();
        } catch (SocketException e) {
            // error
            return -1;
        }
    }

    private InetSocketAddress sendTo = null;

    /* (non-Javadoc)
     * @see org.epics.pvaccess.impl.remote.Transport#enqueueSendRequest(org.epics.pvaccess.impl.remote.TransportSender)
     */
    public final void enqueueSendRequest(TransportSender sender) {
        synchronized (this) {
            this.sendTo = null;
            this.sendBuffer.clear();
            sender.lock();
            try {
                sender.send(this.sendBuffer, this);
                sender.unlock();
                endMessage();
                if (this.sendTo != null)
                    send(this.sendBuffer, this.sendTo);
                else
                    send(this.sendBuffer);
            } catch (Throwable th) {
                sender.unlock();
                // TODO ?
                th.printStackTrace();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.SerializableControl#ensureBuffer(int)
     */
    public void ensureBuffer(int size) {
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.SerializableControl#alignBuffer(int)
     */
    public void alignBuffer(int alignment) {
        final int k = (alignment - 1);
        final int pos = this.sendBuffer.position();
        int newpos = (pos + k) & (~k);
        this.sendBuffer.position(newpos);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.SerializableControl#flushSerializeBuffer()
     */
    public void flushSerializeBuffer() {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.impl.remote.TransportSendControl#flush(boolean)
     */
    public void flush(boolean lastMessageCompleted) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.impl.remote.TransportSendControl#start(byte, int)
     */
    public final void startMessage(byte command, int ensureCapacity) {
        //ensureBuffer(PVAConstants.PVA_MESSAGE_HEADER_SIZE + ensureCapacity);
        this.lastMessageStartPosition = this.sendBuffer.position();
        this.sendBuffer.put(PVAConstants.PVA_MAGIC);
        this.sendBuffer.put(PVAConstants.PVA_VERSION);
        this.sendBuffer.put((byte) this.clientServerWithBigEndianFlag);    // data, big endian, client/server
        this.sendBuffer.put(command);    // command
        this.sendBuffer.putInt(0);        // temporary zero payload
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.impl.remote.TransportSendControl#endMessage()
     */
    public final void endMessage() {
        //we always (for now) send by packet, so no need for this here...
        //alignBuffer(PVAConstants.PVA_ALIGNMENT);
        this.sendBuffer.putInt(this.lastMessageStartPosition + (Short.SIZE / Byte.SIZE + 2), this.sendBuffer.position() - this.lastMessageStartPosition - PVAConstants.PVA_MESSAGE_HEADER_SIZE);
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.impl.remote.TransportSendControl#setRecipient(java.net.InetSocketAddress)
     */
    public final void setRecipient(InetSocketAddress sendTo) {
        this.sendTo = sendTo;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.DeserializableControl#ensureData(int)
     */
    public void ensureData(int size) {
        if (receiveBuffer.remaining() < size)
            throw new BufferUnderflowException();
    }


    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.SerializableControl#cachedSerialize(org.epics.pvdata.pv.Field, java.nio.ByteBuffer)
     */
    public void cachedSerialize(Field field, ByteBuffer buffer) {
        // no cache
        field.serialize(buffer, this);
    }

    private final static FieldCreate fieldCreate = PVFactory.getFieldCreate();

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.DeserializableControl#cachedDeserialize(java.nio.ByteBuffer)
     */
    public Field cachedDeserialize(ByteBuffer buffer) {
        // no cache
        return fieldCreate.deserialize(buffer, this);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.DeserializableControl#alignData(int)
     */
    public void alignData(int alignment) {
        final int k = (alignment - 1);
        final int pos = this.receiveBuffer.position();
        int newpos = (pos + k) & (~k);
        this.receiveBuffer.position(newpos);
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.impl.remote.Transport#setByteOrder(java.nio.ByteOrder)
     */
    public void setByteOrder(ByteOrder byteOrder) {
        // called from receive thread... or before processing
        this.receiveBuffer.order(byteOrder);

        synchronized (this) {
            this.sendBuffer.order(byteOrder);
        }
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.impl.remote.Transport#verify(long)
     */
    public boolean verify(long timeoutMs) {
        // noop
        return true;
    }

    public void verified(Status status) {
        // noop
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.impl.remote.Transport#aliveNotification()
     */
    public void aliveNotification() {
        // noop
    }

    public void authNZMessage(PVField data) {
        // noop
    }

    public void authNZInitialize(Object data) {
        // noop
    }

    public SecuritySession getSecuritySession() {
        // noop
        return null;
    }

}
