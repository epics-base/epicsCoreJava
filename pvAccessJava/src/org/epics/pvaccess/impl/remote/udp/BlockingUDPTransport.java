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
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;

import org.epics.pvaccess.CAConstants;
import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.impl.remote.Context;
import org.epics.pvaccess.impl.remote.IntrospectionRegistry;
import org.epics.pvaccess.impl.remote.ProtocolType;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportClient;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.TransportSender;
import org.epics.pvaccess.impl.remote.request.ResponseHandler;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;


/**
 * CA UDP transport implementation.
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
		this.sendAddresses = sendAddresses;

		socketAddress = bindAddress;

		// allocate receive buffer
		receiveBuffer = ByteBuffer.allocate(CAConstants.MAX_UDP_PACKET);
		
		// allocate send buffer and non-reentrant lock
		sendBuffer = ByteBuffer.allocate(CAConstants.MAX_UDP_UNFRAGMENTED_SEND);
	}
	
	/**
	 * Start processing requests.
	 */
	public void start() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (!closed) processRead();
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
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.Transport#release(org.epics.pvaccess.impl.remote.TransportClient)
	 */
	@Override
	public void release(TransportClient client) {
		// TODO Auto-generated method stub
		
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
		while (receiveBuffer.remaining() >= CAConstants.CA_MESSAGE_HEADER_SIZE)
		{
			//
			// read header
			//
	
			// first byte is CA_MAGIC
			final byte magic = receiveBuffer.get();
			if (magic != CAConstants.CA_MAGIC)
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
	 * Send a buffer through the transport.
	 * @param buffer	buffer to send. 
	 * @return success status.
	 */
	public boolean send(ByteBuffer buffer) 
	{
		// noop check
		if (sendAddresses == null)
			return false;
			
		for (int i = 0; i < sendAddresses.length; i++)
		{
			try
			{
				// prepare buffer
				buffer.flip();

				//context.getLogger().finest("Sending " + buffer.limit() + " bytes to " + sendAddresses[i] + ".");

				channel.send(buffer, sendAddresses[i]);
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
		catch (Throwable ex) 
		{
			// TODO what to do here
			ex.printStackTrace(); 
		}
	}

	/**
	 * @see org.epics.pvaccess.impl.remote.Transport#getMinorRevision()
	 */
	public byte getMinorRevision() {
		return CAConstants.CA_PROTOCOL_REVISION;
	}

	/**
	 * Get protocol type (e.g. tpc, udp, ssl, etc.).
	 * @see org.epics.pvaccess.impl.remote.Transport#getType()
	 */
	public String getType() {
		return ProtocolType.UDP.name();
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
		return CAConstants.CA_DEFAULT_PRIORITY;
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
	public void setBroadcastAddresses(InetSocketAddress[] addresses) {
		sendAddresses = addresses;
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
	public void setRemoteMinorRevision(byte minor) {
		// noop
	}

	/**
	 * @see org.epics.pvaccess.impl.remote.Transport#setRemoteTransportReceiveBufferSize(int)
	 */
	public void setRemoteTransportReceiveBufferSize(int receiveBufferSize) {
		// noop for UDP (limited by 64k; CAConstants.MAX_UDP_SEND for CA)
	}

	/**
	 * @see org.epics.pvaccess.impl.remote.Transport#setRemoteTransportSocketReceiveBufferSize(int)
	 */
	public void setRemoteTransportSocketReceiveBufferSize(int socketReceiveBufferSize) {
		// noop for UDP (limited by 64k; CAConstants.MAX_UDP_SEND for CA)
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

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.Transport#getIntrospectionRegistry()
	 */
	public IntrospectionRegistry getIntrospectionRegistry() {
		throw new UnsupportedOperationException("not supported by UDP transport");
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
		//ensureBuffer(CAConstants.CA_MESSAGE_HEADER_SIZE + ensureCapacity);
		lastMessageStartPosition = sendBuffer.position();
		sendBuffer.put(CAConstants.CA_MAGIC);
		sendBuffer.put(CAConstants.CA_VERSION);
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
		//alignBuffer(CAConstants.CA_ALIGNMENT);
		sendBuffer.putInt(lastMessageStartPosition + (Short.SIZE/Byte.SIZE + 2), sendBuffer.position() - lastMessageStartPosition - CAConstants.CA_MESSAGE_HEADER_SIZE); 
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
		// noop for UDP (packet based)
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

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.Transport#verified()
	 */
	@Override
	public void verified() {
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
