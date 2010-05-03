/*
 * Copyright (c) 2009 by Cosylab
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

package org.epics.ca.impl.remote.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.epics.ca.CAConstants;
import org.epics.ca.impl.remote.ConnectedTransport;
import org.epics.ca.impl.remote.Context;
import org.epics.ca.impl.remote.ProtocolType;
import org.epics.ca.impl.remote.ResponseHandler;
import org.epics.ca.impl.remote.TransportSendControl;
import org.epics.ca.impl.remote.TransportSender;
import org.epics.ca.util.GrowingCircularBuffer;


/**
 * TCP transport implementation.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public abstract class BlockingTCPTransport implements ConnectedTransport, TransportSendControl {

	@SuppressWarnings("serial")
	private static class ClosedException extends RuntimeException {

		public ClosedException(String message) {
			super(message);
		}

		public ClosedException(Throwable cause) {
			super(cause);
		}
		
	}
	
	/**
	 * Connection status.
	 */
	protected volatile boolean closed = false;

	/**
	 * Context instance.
	 */
	protected final Context context;

	/**
	 * Corresponding channel.
	 */
	protected final SocketChannel channel;

	/**
	 * Cached socket address.
	 */
	protected final InetSocketAddress socketAddress;

	/**
	 * Send buffer.
	 */
	protected final ByteBuffer sendBuffer;

	/**
	 * Remote side transport revision (minor).
	 */
	protected byte remoteTransportRevision;

	/**
	 * Remote side transport receive buffer size.
	 */
	protected int remoteTransportReceiveBufferSize = CAConstants.MAX_TCP_RECV;

	/**
	 * Remote side transport socket receive buffer size.
	 */
	protected int remoteTransportSocketReceiveBufferSize = CAConstants.MAX_TCP_RECV;

	/**
	 * Priority.
	 * NOTE: Priority cannot just be changed, since it is registered in transport registry with given priority.
	 */
	protected final short priority;
	// TODO to be implemeneted 
	
	/**
	 * CAS response handler.
	 */
	protected final ResponseHandler responseHandler;

	/**
	 * Read sync. object monitor.
	 */
	protected final Object readMonitor = new Object();

	/**
	 * Total bytes received.
	 */
	protected volatile long totalBytesReceived = 0;
	
	/**
	 * Total bytes sent.
	 */
	protected volatile long totalBytesSent = 0;
	
	/**
	 * Marker to send.
	 */
	protected AtomicInteger markerToSend = new AtomicInteger(0);
	
	/**
	 * Send buffer size.
	 */
	private final int maxPayloadSize;

	/**
	 * Send buffer size.
	 */
	private int socketSendBufferSize;

	/**
	 * Default marker period.
	 */
	private static final int MARKER_PERIOD = 1024;
	
	/**
	 * Marker "period" in bytes (every X bytes marker should be set).
	 */
	private long markerPeriodBytes = MARKER_PERIOD;
	
	/**
	 * Next planned marker position.
	 */
	private long nextMarkerPosition = markerPeriodBytes;

	/**
	 * Send pending flag.
	 */
	private boolean sendPending = false;

	/**
     * Last message start position.
     */
    private int lastMessageStartPosition = 0;
	
	
	private final ByteBuffer socketBuffer;

	/**
	 * TCP transport constructor.
	 * @param context context where transport lives in.
	 * @param channel used socket channel.
	 * @param responseHandler response handler used to process CA headers.
	 * @param receiveBufferSize receive buffer size.
	 * @param priority transport priority.
	 */
	public BlockingTCPTransport(Context context, 
					   SocketChannel channel,
					   ResponseHandler responseHandler,
					   int receiveBufferSize,
					   short priority) {
		this.context = context;
		this.channel = channel;
		this.responseHandler = responseHandler;
		this.remoteTransportRevision = 0;
		this.priority = priority;

		//socketBuffer = ByteBuffer.allocate(Math.max(CAConstants.MAX_TCP_RECV + MAX_ENSURE_DATA_BUFFER_SIZE, receiveBufferSize));
		/// TODO
		socketBuffer = ByteBuffer.allocateDirect(Math.max(CAConstants.MAX_TCP_RECV + MAX_ENSURE_DATA_BUFFER_SIZE, receiveBufferSize));
		socketBuffer.position(socketBuffer.limit());
		startPosition = socketBuffer.position();
		
		// allocate buffer
		sendBuffer = ByteBuffer.allocateDirect(socketBuffer.capacity());
		maxPayloadSize = sendBuffer.capacity() - 2*CAConstants.CA_MESSAGE_HEADER_SIZE; // one for header, one for flow control
		
		// get send buffer size
        try {
			socketSendBufferSize = channel.socket().getSendBufferSize();
		} catch (SocketException e) {
			socketSendBufferSize = CAConstants.MAX_TCP_RECV;
			context.getLogger().log(Level.WARNING, "Unable to retrieve socket send buffer size.", e);
		}
		
		socketAddress = (InetSocketAddress)channel.socket().getRemoteSocketAddress();
		
		// prepare buffer
		clearAndReleaseBuffer();
		
		// add to registry
		context.getTransportRegistry().put(this);
	}

	/**
	 * Start processing requests.
	 */
	public void start() {
		Thread rcvThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (!closed) processReadCached(false, null, CAConstants.CA_MESSAGE_HEADER_SIZE, false);
			}
		}, "TCP-receive " + socketAddress);
		//rcvThread.setPriority(Thread.MIN_PRIORITY);
		rcvThread.start();

		Thread sendThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				processSendQueue();
				
				// connection resource are freed by write thread
				freeConnectionResorces();
			}
		}, "TCP-send " + socketAddress);
		//sendThread.setPriority(Thread.MIN_PRIORITY);
		sendThread.start();
			
	}
	
	/** 
	 * Close connection.
	 * @param forced	flag indicating if forced (e.g. forced disconnect) is required 
	 */
	public synchronized void close(boolean forced) {

		// already closed check
		if (closed)
			return;

		closed = true;

		// remove from registry
		context.getTransportRegistry().remove(this);

		// clean resources
		internalClose(forced);
		
		// notify send queue
		synchronized (sendQueue) {
			sendQueue.notifyAll();
		}
	}

	/** 
	 * Close and free connection resources.
	 */
	public void freeConnectionResorces() {

		freeSendBuffers();
		
		context.getLogger().finer("Connection to " + socketAddress + " closed.");

		try {
			if (channel.isOpen())
				channel.close();
		} catch (IOException e) {
			// noop
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.core.Transport#isClosed()
	 */
	public boolean isClosed() {
		return closed;
	}
	
	/**
	 * Called to any resources just before closing transport
	 * @param forced	flag indicating if forced (e.g. forced disconnect) is required
	 */
	protected void internalClose(boolean forced)
	{
		// noop
	}

	/**
	 * Free all send buffers (return them to the cached buffer allocator).
	 */
	private void freeSendBuffers() {
		// TODO ?
	}
	
	/**
	 * Set minor revision number.
	 * @param minorRevision	minor revision number.
	 */
	public void setRemoteMinorRevision(byte minorRevision) {
		this.remoteTransportRevision = minorRevision;
	}

	/**
	 * Get remote transport receive buffer size (in bytes).
	 * @return remote transport receive buffer size
	 */
	public int getRemoteTransportReceiveBufferSize() {
		return remoteTransportReceiveBufferSize;
	}

	/**
	 * Set remote transport receive buffer size (in bytes).
	 * @param remoteTransportReceiveBufferSize remote transport receive buffer size.
	 * @see org.epics.ca.impl.remote.Transport#setRemoteTransportReceiveBufferSize(int)
	 */
	public void setRemoteTransportReceiveBufferSize(
			int remoteTransportReceiveBufferSize) {
		this.remoteTransportReceiveBufferSize = remoteTransportReceiveBufferSize;
	}

	/**
	 * @see org.epics.ca.impl.remote.Transport#setRemoteTransportSocketReceiveBufferSize(int)
	 */
	public void setRemoteTransportSocketReceiveBufferSize(int socketReceiveBufferSize) {
		this.remoteTransportSocketReceiveBufferSize = socketReceiveBufferSize;
	}

	/**
	 * Get major revision number.
	 * @see org.epics.ca.impl.remote.Transport#getMajorRevision()
	 */
	public byte getMajorRevision() {
		return CAConstants.CA_MAJOR_PROTOCOL_REVISION;
	}

	/**
	 * @see org.epics.ca.impl.remote.Transport#getMinorRevision()
	 */
	public byte getMinorRevision() {
		return CAConstants.CA_MINOR_PROTOCOL_REVISION;
	}

	/**
	 * Get protocol type (e.g. tpc, udp, ssl, etc.).
	 * @see org.epics.ca.impl.remote.Transport#getType()
	 */
	public String getType() {
		return ProtocolType.TCP.name();
	}

	enum ReceiveStage { READ_FROM_SOCKET, PROCESS_HEADER, PROCESS_PAYLOAD };
	private ReceiveStage stage = ReceiveStage.READ_FROM_SOCKET;

	private short magicAndVersion;
	private byte packetType;
	private byte command;
	private int payloadSize;
	
	private int storedPayloadSize;
	private int storedPosition;
	private int storedLimit;
	
	private int startPosition;
	
	/**
	 * Process input (read) IO event.
	 */
	protected void processReadCached(boolean nestedCall, ReceiveStage inStage, int requiredBytes, boolean addToBuffer) {
		try
		{ 
			while (!closed)
			{
				if (stage == ReceiveStage.READ_FROM_SOCKET || inStage != null)
				{
					final int currentStartPosition;
					if (addToBuffer)
					{
						currentStartPosition = socketBuffer.position();
						socketBuffer.position(socketBuffer.limit());
						socketBuffer.limit(socketBuffer.capacity());
					}
					else
					{
						// add to bytes read
						totalBytesReceived += (socketBuffer.position() - startPosition);

						// copy remaining bytes, if any
						final int remainingBytes = socketBuffer.remaining();
						final int endPosition = MAX_ENSURE_DATA_BUFFER_SIZE + remainingBytes;
						for (int i = MAX_ENSURE_DATA_BUFFER_SIZE; i < endPosition; i++)
							socketBuffer.put(i, socketBuffer.get());
						
						currentStartPosition = startPosition = MAX_ENSURE_DATA_BUFFER_SIZE;
						socketBuffer.position(MAX_ENSURE_DATA_BUFFER_SIZE + remainingBytes);
						socketBuffer.limit(socketBuffer.capacity());
					}
					
					// read at least requiredBytes bytes
					
					final int requiredPosition = (currentStartPosition + requiredBytes);
					while (socketBuffer.position() < requiredPosition)
					{
						// read
						final int bytesRead = channel.read(socketBuffer);
						if (bytesRead < 0)
						{
							// error (disconnect, end-of-stream) detected
							close (true);
							
							if (nestedCall)
								throw new ClosedException("bytesRead < 0");
							
							//System.out.println("-------------------!!!!!!!!!!!!!!!!!!!!!!!!!!-------------------- closed");
							return; 
						}
					}
					socketBuffer.limit(socketBuffer.position());
					socketBuffer.position(currentStartPosition);
					
					// notify liveness
					aliveNotification();

					// exit
					if (inStage != null)
						return;
					
					stage = ReceiveStage.PROCESS_HEADER;
				}

				if (stage == ReceiveStage.PROCESS_HEADER)
				{
					// ensure CAConstants.CA_MESSAGE_HEADER_SIZE bytes of data
					if (socketBuffer.remaining() < CAConstants.CA_MESSAGE_HEADER_SIZE)
						processReadCached(true, ReceiveStage.PROCESS_HEADER, CAConstants.CA_MESSAGE_HEADER_SIZE, false);

					// first byte is CA_MAGIC
					// second byte version - major/minor nibble 
					// check magic and version at once
					magicAndVersion = socketBuffer.getShort(); 
					if ((short)(magicAndVersion & 0xFFF0) != CAConstants.CA_MAGIC_AND_MAJOR_VERSION) {
						// error... disconnect
						context.getLogger().warning("Invalid header received from client " + socketAddress + ", disconnecting...");
						close (true);
						return; 
					}
					
					// data vs. control packet
					packetType = socketBuffer.get();
					
					// command
					command = socketBuffer.get();

					// read payload size
					payloadSize = socketBuffer.getInt();

					// data
					final byte type = (byte)(packetType & 0x0F);
					if (type == 0)
					{
						stage = ReceiveStage.PROCESS_PAYLOAD;
					}
					else if (type == 1)
					{
						if (command == 0)
						{
							if (markerToSend.getAndSet(payloadSize) == 0)
								; // TODO send back response
						}
						else //if (command == 1)
						{
							int difference = (int)totalBytesSent - payloadSize + CAConstants.CA_MESSAGE_HEADER_SIZE;
							// overrun check
							if (difference < 0)
								difference += Integer.MAX_VALUE;
							remoteBufferFreeSpace = remoteTransportReceiveBufferSize + remoteTransportSocketReceiveBufferSize - difference; 
							// TODO if this is calculated wrong, this can be critical !!!
						}
						
						// no payload
						//stage = ReceiveStage.PROCESS_HEADER;
						continue;
					}
					else
					{
						context.getLogger().severe("Unknown packet type " + type + ", received from client " + socketAddress + ", disconnecting...");
						close(true);
						return;
					}
				}
				
				if (stage == ReceiveStage.PROCESS_PAYLOAD)
				{
					// read header
					final byte version = (byte)(magicAndVersion & 0xFF);
					// last segment bit set (means in-between segment or last segment)
					final boolean notFirstSegment = (packetType & 0x20) != 0;

					storedPayloadSize = payloadSize;

					// if segmented, exit reading code
					if (nestedCall && notFirstSegment)
						return;
					
					// NOTE: nested data (w/ payload) messages between segmented messages are not supported
					storedPosition = socketBuffer.position();
					storedLimit = socketBuffer.limit();
					socketBuffer.limit(Math.min(storedPosition + storedPayloadSize, storedLimit));
					try
					{
						// handle response					
						responseHandler.handleResponse(socketAddress, this, version, command, payloadSize, socketBuffer);
					}
					finally
					{
						socketBuffer.limit(storedLimit);
						int newPosition = storedPosition + storedPayloadSize;
						if (newPosition > storedLimit)
						{
							newPosition -= storedLimit;
							socketBuffer.position(storedLimit);
							processReadCached(true, ReceiveStage.PROCESS_PAYLOAD, newPosition, false);
							newPosition += startPosition;
						}
						socketBuffer.position(newPosition);
						// TODO discard all possible segments?!!!
					}
					
					stage = ReceiveStage.PROCESS_HEADER;
					
					continue;
				}				
				
			}
		} catch (ClosedException ce) {
			if (nestedCall)
				throw ce;
		} catch (AsynchronousCloseException ace) {
			// close connection
			close(true);
			
			if (nestedCall)
				throw new ClosedException(ace);
			
		} catch (Throwable th) {
			context.getLogger().log(Level.SEVERE, "Unexpected exception caught in thread processing requests.", th);
			
			// close connection
			close(true);

			if (nestedCall)
				throw new ClosedException(th);
		}
		
		return;
	}

	private final static int MAX_ENSURE_DATA_BUFFER_SIZE = 1024;
		
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.DeserializableControl#ensureData(int)
	 */
	@Override
	public void ensureData(int size) {
		// enough of data?
		if (socketBuffer.remaining() >= size)
			return;
		
		// to large for buffer...
		if (maxPayloadSize < size)
			throw new RuntimeException("requested for buffer size " + size + ", but only " + maxPayloadSize + " available.");

		// subtract what was already processed
		storedPayloadSize -= socketBuffer.position() - storedPosition;

		// no more data and we have some payload left => read buffer
		if (storedPayloadSize >= size)
		{
			//System.out.println("storedPayloadSize >= size, remaining:" + socketBuffer.remaining());

			// just read up remaining payload
			// since there is no data on the buffer, read to the beginning of it, at least size bytes
			processReadCached(true, ReceiveStage.PROCESS_PAYLOAD, size, false);
			storedPosition = socketBuffer.position();
			storedLimit = socketBuffer.limit();
			socketBuffer.limit(Math.min(storedPosition + storedPayloadSize, storedLimit));
		}
		else
		{
			// copy remaining bytes, if any
			final int remainingBytes = socketBuffer.remaining();
			for (int i = 0; i < remainingBytes; i++)
				socketBuffer.put(i, socketBuffer.get());
			
			// read what is left
			socketBuffer.limit(storedLimit);
			
			stage = ReceiveStage.PROCESS_HEADER;
			processReadCached(true, null, size, false);

			// copy before position
			for (int i = remainingBytes - 1, j = socketBuffer.position() - 1; i >= 0; i--, j--)
				socketBuffer.put(j, socketBuffer.get(i));
			startPosition = socketBuffer.position() - remainingBytes;
			socketBuffer.position(startPosition);
			
			storedPosition = startPosition; //socketBuffer.position();
			storedLimit = socketBuffer.limit();
			socketBuffer.limit(Math.min(storedPosition + storedPayloadSize, storedLimit));

			// add if missing...
			if (!closed && socketBuffer.remaining() < size)
				ensureData(size);
		}
		
		if (closed)
			throw new ClosedException("transport closed");
	}

	
	protected volatile long remoteBufferFreeSpace = Long.MAX_VALUE;	

	/**
	 * Send a buffer through the transport.
	 * NOTE: TCP sent buffer/sending has to be synchronized (not done by this method).
	 * @param buffer	buffer to be sent
	 * @return success indicator
	 * @throws IOException 
	 */
	protected boolean send(ByteBuffer buffer) throws IOException
	{
		try
		{
			// TODO simply use value from marker???!!!
			// On Windows, limiting the buffer size is important to prevent
	        // poor throughput performances when transferring large amount of
	        // data. See Microsoft KB article KB823764.
			// We do it also for other systems just to be safe.
			final int maxBytesToSend = Math.min(socketSendBufferSize, remoteTransportSocketReceiveBufferSize) / 2;

			final int limit = buffer.limit();
	        int bytesToSend = limit - buffer.position();

//context.getLogger().finest("Total bytes to send: " + bytesToSend);
//System.out.println("Total bytes to send: " + bytesToSend);

	        // limit sending
	        if (bytesToSend > maxBytesToSend)
	        {
	        	bytesToSend = maxBytesToSend;
	            buffer.limit(buffer.position() + bytesToSend);
	        }

//context.getLogger().finest("Sending " + bytesToSend + " of total " + limit + " bytes in the packet to " + socketAddress + ".");
//System.out.println("Sending " + bytesToSend + " of total " + limit + " bytes in the packet to " + socketAddress + ".");

	        while (buffer.hasRemaining())
	        {
				final int bytesSent = channel.write(buffer);
	        	if (bytesSent < 0)
	        	{
	        		// connection lost
	        		throw new IOException("bytesSent < 0");
	        	}
	        	else if (bytesSent == 0)
	        	{
//context.getLogger().finest("Buffer full, position " + buffer.position() + " of total " + limit + " bytes.");
//System.out.println("Buffer full, position " + buffer.position() + " of total " + limit + " bytes.");
	        		// buffers full, reset the limit and indicate that there are more data to be sent
	        		if (bytesSent == maxBytesToSend)
	        			buffer.limit(limit);

//context.getLogger().finest("Send buffer full for " + socketAddress + ", waiting...");
//System.out.println("Send buffer full for " + socketAddress + ", waiting...");
	        		return false;
	        	}
				totalBytesSent += bytesSent;

	        	// readjust limit
        		if (bytesToSend == maxBytesToSend)
                {
        			bytesToSend = limit - buffer.position();
                    if(bytesToSend > maxBytesToSend)
    		        	bytesToSend = maxBytesToSend;
		            buffer.limit(buffer.position() + bytesToSend);
                }

//context.getLogger().finest("Sent, position " + buffer.position() + " of total " + limit + " bytes.");
//System.out.println("Sent, position " + buffer.position() + " of total " + limit + " bytes.");
	        }
	        
	        // all sent
	        return true;

		}
		catch (IOException ioex) 
		{
			// close connection
			close(true);
			throw ioex;
		}
	}
	
    private int sendBufferSentPosition = 0;
    
    /**
	 * Flush send buffer (blocks until flushed).
	 * @return success flag.
	 */
    private boolean flush()
    {
		// request issues, has not sent anything yet (per partes)
		if (!sendPending)
		{
			sendPending = true;
			
			// start sending from the start
			sendBufferSentPosition = 0;
			
			// if not set skip marker otherwise set it
			final int markerValue = markerToSend.getAndSet(0);
			if (markerValue == 0)
				sendBufferSentPosition = CAConstants.CA_MESSAGE_HEADER_SIZE;
			else
				sendBuffer.putInt(4, markerValue);
		}
		
		boolean success = false;
    	try
    	{
    		// remember current position
    		final int currentPos = sendBuffer.position();

    		// set to send position
    		sendBuffer.position(sendBufferSentPosition);
    		sendBuffer.limit(currentPos);
    		
			success = send(sendBuffer);

			// all sent?
    		if (success)
    		{
    			clearAndReleaseBuffer();
    		}
    		else
    		{
    			// remember position
				sendBufferSentPosition = sendBuffer.position();

				// .. reset to previous state
				sendBuffer.position(currentPos);
				sendBuffer.limit(sendBuffer.capacity());
    		}

    	} catch (IOException ie) {
    		// error, release lock
    		clearAndReleaseBuffer();
    	} catch (Throwable th) {
    		// unexpected exception
    		// print stack trace
    		th.printStackTrace();
    		
    		// error, release lock
    		clearAndReleaseBuffer();
    	}
    	
    	return success;
    }

    /**
     * Internal method that clears and releases buffer. 
     * sendLock and sendBufferLock must be hold while calling this method. 
     */
    private final void clearAndReleaseBuffer()
    {
    	// NOTE: take care that nextMarkerPosition is set right
		// fix position to be correct when buffer is cleared
    	// do not include pre-buffered flow control message; not 100% correct, but OK
		nextMarkerPosition -= sendBuffer.position() - CAConstants.CA_MESSAGE_HEADER_SIZE;
		synchronized (sendQueue) {
			flushRequested = false;
		}
    	sendBuffer.clear();

    	sendPending = false;
		
		// prepare ACK marker
		sendBuffer.putShort(CAConstants.CA_MAGIC_AND_VERSION);
		sendBuffer.put((byte)1);	// control data
		sendBuffer.put((byte)1);	// marker ACK
		sendBuffer.putInt(0);
		
    }
    
	/**
	 * Beacon arrival.
	 * @see org.epics.ca.impl.remote.Transport#aliveNotification()
	 */
	public void aliveNotification() {
		// noop
	}

	/**
	 * @see org.epics.ca.impl.remote.Transport#changedTransport()
	 */
	public void changedTransport() {
		// noop
	}

	/**
	 * @see org.epics.ca.impl.remote.Transport#getContext()
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * @see org.epics.ca.impl.remote.Transport#getRemoteAddress()
	 */
	public InetSocketAddress getRemoteAddress() {
		return socketAddress;
	}

	/**
	 * @see org.epics.ca.impl.remote.Transport#getPriority()
	 */
	public short getPriority() {
		return priority;
	}

	/**
	 * @see org.epics.ca.impl.remote.Transport#getReceiveBufferSize()
	 */
	public int getReceiveBufferSize() {
		return socketBuffer.capacity();
	}
	
	/**
	 * @see org.epics.ca.impl.remote.Transport#getSocketReceiveBufferSize()
	 */
	public int getSocketReceiveBufferSize() {
		try {
			return channel.socket().getReceiveBufferSize();
		} catch (SocketException e) {
			// error
			return -1;
		}
	}

	
	protected boolean verified = false;
	private Object verifiedMonitor = new Object();
	
	/* (non-Javadoc)
	 * @see org.epics.ca.core.Transport#isVerified()
	 */
	@Override
	public boolean isVerified() {
		synchronized (verifiedMonitor) {
			return verified;
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.core.Transport#verified()
	 */
	@Override
	public void verified() {
		synchronized (verifiedMonitor) {
			verified = true;
			verifiedMonitor.notifyAll();
		}
	}
	
	public boolean waitUntilVerified(long timeoutMs) {
		synchronized (verifiedMonitor) {
			try {
				final long start = System.currentTimeMillis();
				while (!verified && (System.currentTimeMillis() - start) < timeoutMs)
						verifiedMonitor.wait(timeoutMs);
			} catch (InterruptedException e) {
				// noop
			}
			return verified;
		}
	}


	
	private final GrowingCircularBuffer<TransportSender> sendQueue = new GrowingCircularBuffer<TransportSender>(100);
	private boolean flushRequested = false;
	
	// TODO public IF
	public void requestFlush()
	{
		synchronized (sendQueue)
		{
			if (flushRequested)
				return;
			flushRequested = true;
			sendQueue.notify();
		}
	}
	
	public enum SendQueueFlushStrategy { IMMEDIATE, DELAYED, USER_CONTROLED };
	private SendQueueFlushStrategy flushStrategy = SendQueueFlushStrategy.DELAYED;
	private final static int delay = 1;		// TODO configurable		// can gain performance !!!!
	
	public SendQueueFlushStrategy getSendQueueFlushStrategy() {
		return flushStrategy;
	};
	
	public void setSendQueueFlushStrategy(SendQueueFlushStrategy flushStrategy) {
		this.flushStrategy = flushStrategy;
	};
	
	private final void processSendQueue() {
		while (!closed) {
			
			TransportSender sender;
			
			synchronized (sendQueue)
			{
				sender = sendQueue.extract();
				// wait for new message
				while (sender == null && !flushRequested && !closed)
				{
					try {
						if (flushStrategy == SendQueueFlushStrategy.DELAYED) {
							if (delay > 0) sendQueue.wait(delay);
							if (sendQueue.size() == 0)
							{
//								if (hasMonitors || sendBuffer.position() > CAConstants.CA_MESSAGE_HEADER_SIZE)
								if (sendBuffer.position() > CAConstants.CA_MESSAGE_HEADER_SIZE)
									flushRequested = true;
								else
									sendQueue.wait();
							}
						}
						else
							sendQueue.wait();
					} catch (InterruptedException e) {
						// noop
					}
					sender = sendQueue.extract();
				}
			}
			
			// always do flush from this thread
			if (flushRequested)
			{
				/*
				if (hasMonitors)
				{
					monitorSender.send(sendBuffer, this);
				}
				*/
				 
				flush();
			}
			
			if (sender != null)
			{
				sender.lock();
				try {
					lastMessageStartPosition = sendBuffer.position();
					sender.send(sendBuffer, this);

					if (flushStrategy == SendQueueFlushStrategy.IMMEDIATE)
					{
						flush(true);
					}
					else
					{
						// automatic end (to set payload)
						endMessage(false);
					}
					
				} catch (ClosedException ce) {
					// noop
				} catch (Throwable th) {
					context.getLogger().log(Level.WARNING, "Exception caught in thread processing send requests.", th);
					sendBuffer.position(lastMessageStartPosition);
				}
				finally {
					sender.unlock();
				}
			}
					
		}
	}
	
	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.Transport#enqueueSendRequest(org.epics.ca.impl.remote.TransportSender)
	 */
	@Override
	public final void enqueueSendRequest(TransportSender sender) {
		synchronized (sendQueue) {
			sendQueue.insert(sender);
			sendQueue.notify();
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.SerializableControl#ensureBuffer(int)
	 */
	@Override
	public final void ensureBuffer(int size) {
		if (sendBuffer.remaining() >= size)
			return;
		
		// to large for buffer...
		if (maxPayloadSize < size)
			throw new RuntimeException("requested for buffer size " + size + ", but only " + maxPayloadSize + " available.");
		
		while (sendBuffer.remaining() < size && !closed)
			flush(false);
		
		if (closed)
			throw new ClosedException("transport closed");
	}

	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.SerializableControl#flushSerializeBuffer()
	 */
	@Override
	public void flushSerializeBuffer() {
		flush(false);
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.TransportSendControl#flush(boolean)
	 */
	@Override
	public void flush(boolean lastMessageCompleted) {
		
		// automatic end
		endMessage(!lastMessageCompleted);
		
		boolean moreToSend = true;
		// TODO closed check !!! 
		while (moreToSend)
		{
			moreToSend = !flush();
		
			// all sent, exit
			if (!moreToSend)
				break;
			
			// TODO solve this sleep in a better way
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// noop
			}
		}

		lastMessageStartPosition = sendBuffer.position();
		// start with last header
		if (!lastMessageCompleted && lastSegmentedMessageType != 0)
			startMessage(lastSegmentedMessageCommand, 0);
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.TransportSendControl#start(byte, int)
	 */
	@Override
	public final void startMessage(byte command, int ensureCapacity) {
		lastMessageStartPosition = -1;
		ensureBuffer(CAConstants.CA_MESSAGE_HEADER_SIZE + ensureCapacity);
		lastMessageStartPosition = sendBuffer.position();
		sendBuffer.putShort(CAConstants.CA_MAGIC_AND_VERSION);
		sendBuffer.put(lastSegmentedMessageType);	// data
		sendBuffer.put(command);	// command
		sendBuffer.putInt(0);		// temporary zero payload
	}
	
	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.TransportSendControl#endMessage()
	 */
	@Override
	public final void endMessage() {
		endMessage(false);
	}
	
	private byte lastSegmentedMessageType = 0;
	private byte lastSegmentedMessageCommand = 0;

	/**
	 * Manage payload size and flags.
	 * @param segmented
	 */
	private final void endMessage(boolean hasMoreSegments) {
		if (lastMessageStartPosition >= 0)
		{
			sendBuffer.putInt(lastMessageStartPosition + (Short.SIZE/Byte.SIZE + 2), sendBuffer.position() - lastMessageStartPosition - CAConstants.CA_MESSAGE_HEADER_SIZE); 
			
			// set segmented bit
			if (hasMoreSegments) {
				// first segment
				if (lastSegmentedMessageType == 0)
				{
					final int flagsPosition = lastMessageStartPosition + Short.SIZE/Byte.SIZE;
					final byte type = sendBuffer.get(flagsPosition);
					// set fist segment bit
					sendBuffer.put(flagsPosition, (byte)(type | 0x10));
					// first + last segment bit == in-between segment
					lastSegmentedMessageType = (byte)(type | 0x30);
					lastSegmentedMessageCommand = sendBuffer.get(flagsPosition + 1);
				}
			}
			else
			{
				// last segment
				if (lastSegmentedMessageType != 0)
				{
					final int flagsPosition = lastMessageStartPosition + Short.SIZE/Byte.SIZE;
					// set last segment bit (by clearing first segment bit)
					sendBuffer.put(flagsPosition, (byte)(lastSegmentedMessageType & 0xEF));
					lastSegmentedMessageType = 0;
				}
			}
			
			// manage markers
			final int position = sendBuffer.position();
			final int bytesLeft = sendBuffer.remaining();
			if (position >= nextMarkerPosition && bytesLeft >= CAConstants.CA_MESSAGE_HEADER_SIZE)
			{
				sendBuffer.putShort(CAConstants.CA_MAGIC_AND_VERSION);
				sendBuffer.put((byte)1);	// control data
				sendBuffer.put((byte)0);	// marker
				sendBuffer.putInt((int)(totalBytesSent + position + CAConstants.CA_MESSAGE_HEADER_SIZE));
				nextMarkerPosition = position + markerPeriodBytes;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.TransportSendControl#setRecipient(java.net.InetSocketAddress)
	 */
	@Override
	public void setRecipient(InetSocketAddress sendTo) {
		// noop for TCP
	}


	
	
	
	private final GrowingCircularBuffer<TransportSender> monitorSendQueue = new GrowingCircularBuffer<TransportSender>(100);

	
	private TransportSender monitorSender = new TransportSender() {
		
		@Override
		public void unlock() {
			// noop
		}
		
		@Override
		public void send(ByteBuffer buffer, TransportSendControl control) {

			control.startMessage((byte)19, 0);

			while (true) {


				TransportSender sender;
				synchronized (monitorSendQueue) {
					sender = monitorSendQueue.extract();
				}
				
				if (sender == null) {
					control.ensureBuffer(Integer.SIZE/Byte.SIZE);
					buffer.putInt(CAConstants.CAJ_INVALID_IOID);
					break;
				}
				
				sender.send(buffer, control);
				
			}
			
		}
		
		@Override
		public void lock() {
			// noop
		}
	};

	//private boolean hasMonitors = false;
	
	public final void enqueueMonitorSendRequest(TransportSender sender) {
		synchronized (monitorSendQueue) {
			monitorSendQueue.insert(sender);
			if (monitorSendQueue.size() == 1)
				enqueueSendRequest(monitorSender);
		}
		/*
		synchronized (sendQueue) {
			hasMonitors = true;
			sendQueue.notify();
		}
		*/
	}


}
