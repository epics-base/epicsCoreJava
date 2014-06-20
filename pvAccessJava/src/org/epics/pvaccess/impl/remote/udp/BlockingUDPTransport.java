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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.util.Set;
import java.util.logging.Level;

import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.impl.remote.Context;
import org.epics.pvaccess.impl.remote.ProtocolType;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportClient;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.TransportSender;
import org.epics.pvaccess.impl.remote.request.ResponseHandler;
import org.epics.pvaccess.util.InetAddressUtil;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.Status;


/**
 * PVA UDP transport implementation.
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
	private final DatagramChannel channel;

	/**
	 * Cached socket address.
	 */
	private InetSocketAddress socketAddress;

	/**
	 * Bind address.
	 */
	private InetSocketAddress bindAddress;

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

	/**
	 * Send buffer.
	 */
	private final ByteBuffer sendBuffer;

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
	 * @param context
	 */
	public BlockingUDPTransport(Context context, ResponseHandler responseHandler, DatagramChannel channel,
							  InetSocketAddress bindAddress, InetSocketAddress[] sendAddresses, 
							  short remoteTransportRevision) {
		this.context = context;
		this.responseHandler = responseHandler;
		this.channel = channel;
		this.bindAddress = bindAddress;
		setSendAddresses(sendAddresses);

		try {
			this.socketAddress = (InetSocketAddress)channel.socket().getLocalSocketAddress();
		}
		catch (Throwable th) {
			context.getLogger().log(Level.FINER, "Failed to obtain local socket address.", th);
		}
		
		// allocate receive buffer
		receiveBuffer = ByteBuffer.allocate(PVAConstants.MAX_UDP_PACKET);
		
		// allocate send buffer and non-reentrant lock
		sendBuffer = ByteBuffer.allocate(PVAConstants.MAX_UDP_UNFRAGMENTED_SEND);
	}
	
	/**
	 * Start processing requests.
	 */
	public void start() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (!closed) {
					try {
						processRead();
					} catch (Throwable th) {
						context.getLogger().log(Level.FINE, "Uncaught exception caught.", th);
					}
				}
			}
		}, "UDP-receive " + socketAddress).start();
	}

	/**
	 * Close transport.
	 */
	@Override
	public void close() throws IOException
	{
		if (closed)
			return;
		closed = true;

		if (bindAddress != null)
			context.getLogger().finer("UDP connection to " + bindAddress + " closed.");
		//context.getReactor().unregisterAndClose(channel);
		try {
			// TODO this just does not exit socket.receive()!!!!
			// maybe try with setSoTimeout(int timeout)
			channel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.nio.channels.Channel#isOpen()
	 */
	public boolean isOpen() {
		return !closed;
	}

	
	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.Transport#acquire(org.epics.pvaccess.impl.remote.TransportClient)
	 */
	@Override
	public boolean acquire(TransportClient client) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.Transport#release(org.epics.pvaccess.impl.remote.TransportClient)
	 */
	@Override
	public void release(TransportClient client) {
	}

	/**
	 * Process input (read) IO event.
	 */
	protected void processRead() {

		try
		{
			while (!closed)
			{
				
				// reset header buffer
				receiveBuffer.clear();

				// read to buffer
				// NOTE: If there are fewer bytes remaining in the buffer
				// than are required to hold the datagram then the remainder
				// of the datagram is silently discarded.
				InetSocketAddress fromAddress = (InetSocketAddress)channel.receive(receiveBuffer);

				// check if datagram not available
				// NOTE: If this channel is in non-blocking mode and a datagram is not
				// immediately available then this method immediately returns <tt>null</tt>.
				if (fromAddress == null)
					break;

				// check if received from ignore address list
				if (ignoredAddresses != null)
				{
					boolean ignore = false;

					// we do not care about the port
					final InetAddress fromAddressOnly = fromAddress.getAddress();
					for (int i = 0; i < ignoredAddresses.length; i++)
						if (ignoredAddresses[i].getAddress().equals(fromAddressOnly))
						{
							ignore = true;
							break;
						}

					if (ignore)
						continue;
				}

				//context.getLogger().finest("Received " + receiveBuffer.position() + " bytes from " + fromAddress + ".");

				// prepare buffer for reading
				receiveBuffer.flip();

				// process
				processBuffer(fromAddress, receiveBuffer);
 			}
			
		} catch (AsynchronousCloseException ace) {
			try {
				close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ClosedChannelException cce) {
			try {
				close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException ioex) {
			// TODO what to do here
			ioex.printStackTrace();
		}
	}

	/**
	 * Process buffer.
	 * Buffer can contain several messages. Last message must be completed (not partital).
	 * @param buffer buffer to process.
	 * @return success flag.
	 */
	private final boolean processBuffer(final InetSocketAddress fromAddress, final ByteBuffer receiveBuffer) {
		
		// handle response(s)				
		while (receiveBuffer.remaining() >= PVAConstants.PVA_MESSAGE_HEADER_SIZE)
		{
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
			
			// only data for UDP
			final byte flags = receiveBuffer.get();
			if (flags < 0)
			{
				// 7-bit is set
				receiveBuffer.order(ByteOrder.BIG_ENDIAN);
			}
			else
			{
				receiveBuffer.order(ByteOrder.LITTLE_ENDIAN);
			}
			
			// command ID and paylaod
			final byte command = receiveBuffer.get();
			final int payloadSize = receiveBuffer.getInt();
			final int nextRequestPosition = receiveBuffer.position() + payloadSize;
			
			// payload size check
			if (nextRequestPosition > receiveBuffer.limit())
				return false;
			
			// handle
			responseHandler.handleResponse(fromAddress, this, version, command, payloadSize, receiveBuffer);

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
	public enum InetAddressType { ALL, UNICAST, BROADCAST_MULTICAST };
	
	/**
	 * Send a buffer through the transport.
	 * @param buffer	buffer to send. 
	 * @param target 	filter (selector) of what addresses to use when sending.
	 * @return success status.
	 */
	public boolean send(ByteBuffer buffer, InetAddressType target) 
	{
		// noop check
		if (sendAddresses == null)
			return false;
			
		for (int i = 0; i < sendAddresses.length; i++)
		{
			// filter
			if (target != InetAddressType.ALL)
				if ((target == InetAddressType.UNICAST && !isSendAddressUnicast[i]) ||
					(target == InetAddressType.BROADCAST_MULTICAST && isSendAddressUnicast[i]))
					continue;
				
			try
			{
				// prepare buffer
				buffer.flip();

				//context.getLogger().finest("Sending " + buffer.limit() + " bytes to " + sendAddresses[i] + ".");

				channel.send(buffer, sendAddresses[i]);
			}
			catch (NoRouteToHostException nrthe)
			{
				context.getLogger().log(Level.FINER, "No route to host exception caught when sending to: " + sendAddresses[i] + ".", nrthe);
				continue;
			}
			catch (Throwable ex) 
			{
				ex.printStackTrace(); // TODO !!!
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Send a buffer through the transport.
	 * @param buffer	buffer to send. 
	 * @return success status.
	 */
	public boolean send(ByteBuffer buffer) 
	{
		return send(buffer, InetAddressType.ALL);
	}

	/**
	 * Send a buffer through the transport immediately. 
	 * @param buffer	buffer to send. 
	 * @param address	send address. 
	 */
	public void send(ByteBuffer buffer, InetSocketAddress address)
	{
		try
		{
			//context.getLogger().finest("Sending " + buffer.limit() + " bytes to " + address + ".");
			buffer.flip();
			channel.send(buffer, address);
		}
		catch (NoRouteToHostException nrthe)
		{
			context.getLogger().log(Level.FINER, "No route to host exception caught when sending to: " + address + ".", nrthe);
		}
		catch (Throwable ex) 
		{
			// TODO what to do here
			ex.printStackTrace(); 
		}
	}

	public MembershipKey join(InetAddress group, NetworkInterface nif) throws IOException
	{
		return channel.join(group, nif);
	}
	
	// set NIF used to send packets
	public void setMutlicastNIF(NetworkInterface nif, boolean loopback) throws IOException
	{
		channel.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
		channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, nif);
	}
	
	/**
	 * @see org.epics.pvaccess.impl.remote.Transport#getRevision()
	 */
	public byte getRevision() {
		return PVAConstants.PVA_PROTOCOL_REVISION;
	}

	/**
	 * Get protocol type (e.g. tpc, udp, ssl, etc.).
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
		return context;
	}

	/**
	 * @see org.epics.pvaccess.impl.remote.Transport#getPriority()
	 */
	public short getPriority() {
		return PVAConstants.PVA_DEFAULT_PRIORITY;
	}

	/**
	 * Flush...
	 */
	public boolean flush() {
		// noop since all UDP reqeuests are sent immediately
		return true;
	}

	/**
	 * @see org.epics.pvaccess.impl.remote.Transport#getRemoteAddress()
	 */
	public InetSocketAddress getRemoteAddress() {
		return socketAddress;
	}

    /**
     * Get list of send addresses.
     * @return send addresses.
     */
    public InetSocketAddress[] getSendAddresses()
    {
        return sendAddresses;
    }

    /**
     * Get list of ignored addresses.
     * @return ignored addresses.
     */
    public InetSocketAddress[] getIgnoredAddresses()
    {
        return ignoredAddresses;
    }
    
    /**
     * Get bind address.
     * @return bind address.
     */
    public InetSocketAddress getBindAddress()
    {
        return bindAddress;
    }
    
	/**
	 * Set list of send addresses.
	 * @param addresses list of send addresses, non-<code>null</code>.
	 */
	public void setSendAddresses(InetSocketAddress[] addresses) {
		sendAddresses = addresses;
		
		isSendAddressUnicast = new boolean[sendAddresses.length];
		Set<InetAddress> broadcastAddresses = InetAddressUtil.getBroadcastAddresses();
		for (int i = 0; i < sendAddresses.length; i++)
		{
			InetAddress address = sendAddresses[i].getAddress();
			// unicast = not broadcast and not multicast
			isSendAddressUnicast[i] =
						!broadcastAddresses.contains(address) && 
						!address.isMulticastAddress();
		}
	}

	/**
	 * Set ignore list.
	 * @param addresses list of ignored addresses.
	 */
	public void setIgnoredAddresses(InetSocketAddress[] addresses) {
		ignoredAddresses = addresses;
	}
	
	/**
	 * @see org.epics.pvaccess.impl.remote.Transport#getReceiveBufferSize()
	 */
	public int getReceiveBufferSize() {
		return receiveBuffer.capacity();
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
			return channel.socket().getReceiveBufferSize();
		} catch (SocketException e) {
			// error
			return -1;
		}
	}

	private InetSocketAddress sendTo = null;
	
	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.Transport#enqueueSendRequest(org.epics.pvaccess.impl.remote.TransportSender)
	 */
	@Override
	public final void enqueueSendRequest(TransportSender sender) {
		synchronized (this) {
			sendTo = null;
	    	sendBuffer.clear();
			sender.lock();
			try
			{
				sender.send(sendBuffer, this);
				sender.unlock();
				endMessage();
				if (sendTo != null)
					send(sendBuffer, sendTo);
				else
					send(sendBuffer);
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
	@Override
	public void ensureBuffer(int size) {
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.SerializableControl#alignBuffer(int)
	 */
	@Override
	public void alignBuffer(int alignment) {
		final int k = (alignment - 1);
		final int pos = sendBuffer.position();
		int newpos = (pos + k) & (~k);
		sendBuffer.position(newpos);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.SerializableControl#flushSerializeBuffer()
	 */
	@Override
	public void flushSerializeBuffer() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.TransportSendControl#flush(boolean)
	 */
	@Override
	public void flush(boolean lastMessageCompleted) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.TransportSendControl#start(byte, int)
	 */
	@Override
	public final void startMessage(byte command, int ensureCapacity) {
		//ensureBuffer(PVAConstants.PVA_MESSAGE_HEADER_SIZE + ensureCapacity);
		lastMessageStartPosition = sendBuffer.position();
		sendBuffer.put(PVAConstants.PVA_MAGIC);
		sendBuffer.put(PVAConstants.PVA_VERSION);
		sendBuffer.put((byte)0x80);	// data + big endian
		sendBuffer.put(command);	// command
		sendBuffer.putInt(0);		// temporary zero payload
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.TransportSendControl#endMessage()
	 */
	@Override
	public final void endMessage() {
		//we always (for now) send by packet, so no need for this here...
		//alignBuffer(PVAConstants.PVA_ALIGNMENT);
		sendBuffer.putInt(lastMessageStartPosition + (Short.SIZE/Byte.SIZE + 2), sendBuffer.position() - lastMessageStartPosition - PVAConstants.PVA_MESSAGE_HEADER_SIZE); 
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.TransportSendControl#setRecipient(java.net.InetSocketAddress)
	 */
	@Override
	public final void setRecipient(InetSocketAddress sendTo) {
		this.sendTo = sendTo;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.DeserializableControl#ensureData(int)
	 */
	@Override
	public void ensureData(int size) {
		if (receiveBuffer.remaining() < size)
			throw new BufferUnderflowException();
	}

	
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.SerializableControl#cachedSerialize(org.epics.pvdata.pv.Field, java.nio.ByteBuffer)
	 */
	@Override
	public void cachedSerialize(Field field, ByteBuffer buffer) {
		// no cache
		field.serialize(buffer, this);
	}
	
	private final static FieldCreate fieldCreate = PVFactory.getFieldCreate();

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.DeserializableControl#cachedDeserialize(java.nio.ByteBuffer)
	 */
	@Override
	public Field cachedDeserialize(ByteBuffer buffer) {
		// no cache
		return fieldCreate.deserialize(buffer, this);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.DeserializableControl#alignData(int)
	 */
	@Override
	public void alignData(int alignment) {
		final int k = (alignment - 1);
		final int pos = receiveBuffer.position();
		int newpos = (pos + k) & (~k);
		receiveBuffer.position(newpos);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.Transport#setByteOrder(java.nio.ByteOrder)
	 */
	@Override
	public void setByteOrder(ByteOrder byteOrder) {
		// called from receive thread... or before processing
		receiveBuffer.order(byteOrder);

		synchronized (this) {
			sendBuffer.order(byteOrder);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.Transport#verify(long)
	 */
	@Override
	public boolean verify(long timeoutMs) {
		// noop
		return true;
	}

	@Override
	public void verified(Status status) {
		// noop
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.Transport#aliveNotification()
	 */
	@Override
	public void aliveNotification() {
		// noop
	}
	
}
