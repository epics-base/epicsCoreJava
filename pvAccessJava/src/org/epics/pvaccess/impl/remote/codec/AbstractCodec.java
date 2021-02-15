package org.epics.pvaccess.impl.remote.codec;

import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.TransportSender;
import org.epics.pvaccess.util.Mailbox;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * NOTE: non-blocking
 * NOTE: not good to have readPollOne() and writePollOne() in parallel
 * NOTE: while in WriteMode.WAIT_FOR_READY do not do reads (not to go into readPollOne())
 * but what if other side cannot empty receive buffer because it cannot send!
 * <p>
 * non-blocking
 * processRead -> ensureData -> pollOne -> processRead -> enusreData -> pullOne... first one has data, second not -> live-loop; solution: disable first
 * processRead -> ensureData -> pollOne -> processWrite -> enusreBuffer -> flush -> buffer full -> poolOne... same story as above
 */
public abstract class AbstractCodec
        implements ReadableByteChannel, WritableByteChannel, TransportSendControl {

    protected final Logger logger;

    // TODO tune
    static public final int MAX_MESSAGE_PROCESS = 100;
    static public final int MAX_MESSAGE_SEND = 100;

    static public final int MAX_ENSURE_SIZE = 1024;

    static public final int MAX_ENSURE_DATA_SIZE = MAX_ENSURE_SIZE / 2;
    static public final int MAX_ENSURE_BUFFER_SIZE = MAX_ENSURE_SIZE;

    protected final ByteBuffer socketBuffer;

    public enum ReadMode {NORMAL, SPLIT, SEGMENTED}

    ;
    protected ReadMode readMode = ReadMode.NORMAL;

    protected byte version;
    protected byte flags;
    protected byte command;
    protected int payloadSize;

    private int storedPayloadSize;
    private int storedPosition;
    private int storedLimit;

    private int startPosition;

    public AbstractCodec(boolean serverFlag, ByteBuffer receiveBuffer, ByteBuffer sendBuffer,
                         int socketSendBufferSize, boolean blockingProcessQueue, Logger logger) {
        if (receiveBuffer.capacity() < 2 * MAX_ENSURE_SIZE)
            throw new IllegalArgumentException("receiveBuffer.capacity() < 2*MAX_ENSURE_SIZE");
        // require aligned buffer size (not condition, but simplifies alignment code)
        if (receiveBuffer.capacity() % PVAConstants.PVA_ALIGNMENT != 0)
            throw new IllegalArgumentException("receiveBuffer.capacity() % PVAConstants.PVA_ALIGNMENT != 0");

        if (sendBuffer.capacity() < 2 * MAX_ENSURE_SIZE)
            throw new IllegalArgumentException("sendBuffer() < 2*MAX_ENSURE_SIZE");
        // require aligned buffer size (not condition, but simplifies alignment code)
        if (sendBuffer.capacity() % PVAConstants.PVA_ALIGNMENT != 0)
            throw new IllegalArgumentException("sendBuffer() % PVAConstants.PVA_ALIGNMENT != 0");

        this.clientServerFlag = serverFlag ? 0x40 : 0x00;
        this.socketBuffer = receiveBuffer;
        this.sendBuffer = sendBuffer;

        // initialize to be empty
        socketBuffer.position(socketBuffer.limit());
        startPosition = socketBuffer.position();

        // clear send
        sendBuffer.clear();

        this.maxSendPayloadSize = sendBuffer.capacity() - 2 * PVAConstants.PVA_MESSAGE_HEADER_SIZE;    // start msg + control
        this.socketSendBufferSize = socketSendBufferSize;
        this.blockingProcessQueue = blockingProcessQueue;
        this.logger = logger;
    }


    @SuppressWarnings("incomplete-switch")
    public final void processRead() throws IOException, ConnectionClosedException, InvalidDataStreamException {
        //System.out.println("processRead");
        switch (readMode) {
            case NORMAL:
                processReadNormal();
                break;
				/*
			case SPLIT:
				processReadSplit();
				break;
				*/
            case SEGMENTED:
                processReadSegmented();
                break;
        }
    }

	/*
	private final void processReadSplit() throws IOException
	{
		// read as much as available
		readToBuffer(1, false);
	}
	*/

    private final void processHeader() throws IOException {
        // magic code
        final byte magicCode = socketBuffer.get();

        // version
        version = socketBuffer.get();

        // flags
        flags = socketBuffer.get();

        // command
        command = socketBuffer.get();

        // read payload size
        payloadSize = socketBuffer.getInt();

        // check magic code
        if (magicCode != PVAConstants.PVA_MAGIC) {
            logger.warning("Invalid header received from client " + getLastReadBufferSocketAddress() + ", disconnecting...");
            invalidDataStreamHandler();
            throw new InvalidDataStreamException("invalid header received");
        }
    }

    private final void processReadNormal() throws IOException {
        try {
            int messageProcessCount = 0;
            while (messageProcessCount++ < MAX_MESSAGE_PROCESS) {
                // read as much as available, but at least for a header
                // readFromSocket checks if reading from socket is really necessary
                if (!readToBuffer(PVAConstants.PVA_MESSAGE_HEADER_SIZE, false))
                    return;

                // read header fields
                processHeader();

                final boolean isControl = ((flags & 0x01) == 0x01);
                if (isControl)
                    processControlMessage();
                else {
                    // segmented sanity check
                    final boolean notFirstSegment = (flags & 0x20) != 0;
                    if (notFirstSegment) {
                        // zero payload message is "kind of" valid
                        if (payloadSize == 0)
                            continue;

                        logger.warning("Not-a-first segmented message received in normal mode from client " + getLastReadBufferSocketAddress() + ", disconnecting...");
                        invalidDataStreamHandler();
                        throw new InvalidDataStreamException("not-a-first segmented message received in normal mode");
                    }

                    storedPayloadSize = payloadSize;
                    storedPosition = socketBuffer.position();
                    storedLimit = socketBuffer.limit();
                    socketBuffer.limit(Math.min(storedPosition + storedPayloadSize, storedLimit));
                    Throwable storedException = null;    // TODO
                    try {
                        // handle response
                        processApplicationMessage();
                    }
					/*
					catch (Throwable th) {
						storedException = th;
						throw th;
					}
					*/ finally {
                        if (!isOpen())
                            return;

                        // can be closed by now
                        // isOpen() should be efficiently implemented
                        while (true)
                        //while (isOpen())
                        {
                            // set position as whole message was read (in case code haven't done so)
                            int newPosition = alignedValue(storedPosition + storedPayloadSize, PVAConstants.PVA_ALIGNMENT);
                            // aligned buffer size ensures that there is enough space in buffer,
                            // however data might not be fully read

                            // discard the rest of the packet
                            if (newPosition > storedLimit) {
                                // processApplicationMessage() did not read up quite some buffer

                                // we only handle unused alignment bytes
                                int bytesNotRead = newPosition - socketBuffer.position();
                                if (bytesNotRead < PVAConstants.PVA_ALIGNMENT) {
                                    // make alignment bytes as real payload to enable SPLIT
                                    // no end-of-socket or segmented scenario can happen
                                    // due to aligned buffer size
                                    storedPayloadSize += bytesNotRead;
                                    // reveal currently existing padding
                                    socketBuffer.limit(storedLimit);
                                    ensureData(bytesNotRead);
                                    storedPayloadSize -= bytesNotRead;
                                    continue;
                                }

                                // TODO we do not handle this for now (maybe never)
                                logger.log(Level.WARNING, "unprocessed read buffer from client " + getLastReadBufferSocketAddress() + ", disconnecting...", storedException);
                                invalidDataStreamHandler();
                                throw new InvalidDataStreamException("unprocessed read buffer", storedException);
                            }
                            socketBuffer.limit(storedLimit);
                            socketBuffer.position(newPosition);
                            break;
                        }
                    }
                }
            }
        } catch (InvalidDataStreamException idse) {
            // noop, should be already handled (and logged)
        } catch (ConnectionClosedException cce) {
            // noop, should be already handled (and logged)
        } catch (ClosedByInterruptException cbie) {
            close();
        }
    }

    private final void processReadSegmented() throws IOException {
        while (true) {
            // read as much as available, but at least for a header
            // readFromSocket checks if reading from socket is really necessary
            readToBuffer(PVAConstants.PVA_MESSAGE_HEADER_SIZE, true);

            // read header fields
            processHeader();

            final boolean isControl = ((flags & 0x01) == 0x01);
            if (isControl)
                processControlMessage();
            else {
                // last segment bit set (means in-between segment or last segment)
                // we expect this, no non-control messages between segmented message are supported
                // NOTE: for now... it is easy to support non-semgented messages between segmented messages
                final boolean notFirstSegment = (flags & 0x20) != 0;
                if (!notFirstSegment) {
                    logger.warning("Not-a-first segmented message expected from client " + getLastReadBufferSocketAddress() + ", disconnecting...");
                    invalidDataStreamHandler();
                    throw new InvalidDataStreamException("not-a-first segmented message expected");
                }

                storedPayloadSize = payloadSize;

                // return control to caller code
                return;
            }

        }
    }

    public abstract void processControlMessage();

    public abstract void processApplicationMessage() throws IOException;

    public abstract InetSocketAddress getLastReadBufferSocketAddress();

    public abstract void invalidDataStreamHandler();

    /**
     * @param requiredBytes required bytes
     * @param persistent    persistent?
     * @return returns <code>true</code>, if buffer contains at least <code>requiredBytes</code> unread bytes.
     * @throws IOException for IO exceptions
     */
    private boolean readToBuffer(int requiredBytes, boolean persistent)
            throws IOException {
        // do we already have requiredBytes available?
        final int remainingBytes = socketBuffer.remaining();
        if (remainingBytes >= requiredBytes)
            return true;

        // assumption: remainingBytes < MAX_ENSURE_DATA_BUFFER_SIZE &&
        //			   requiredBytes < (socketBuffer.capacity() - PVA_ALIGNMENT)

        //
        // copy unread part to the beginning of the buffer
        // to make room for new data (as much as we can read)
        // NOTE: requiredBytes is expected to be small (order of 10 bytes)
        //

        // a new start position, we are careful to preserve alignment
        startPosition = MAX_ENSURE_SIZE + socketBuffer.position() % PVAConstants.PVA_ALIGNMENT;
        final int endPosition = startPosition + remainingBytes;
        for (int i = startPosition; i < endPosition; i++)
            socketBuffer.put(i, socketBuffer.get());

        // update buffer to the new position
        socketBuffer.limit(socketBuffer.capacity());
        socketBuffer.position(endPosition);

        // read at least requiredBytes bytes
        final int requiredPosition = startPosition + requiredBytes;
        while (socketBuffer.position() < requiredPosition) {
            int bytesRead = this.read(socketBuffer);
            if (bytesRead < 0) {
                close();
                throw new ConnectionClosedException("bytesRead < 0");
            }
            // non-blocking IO support
            else if (bytesRead == 0) {
                if (persistent)
                    this.readPollOne();
                else {
                    // set pointers (aka flip)
                    socketBuffer.limit(socketBuffer.position());
                    socketBuffer.position(startPosition);

                    return false;
                }
            }
        }

        // set pointers (aka flip)
        socketBuffer.limit(socketBuffer.position());
        socketBuffer.position(startPosition);

        return true;
    }

    public abstract void readPollOne() throws IOException;

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.DeserializableControl#ensureData(int)
     */
    public final void ensureData(int size) {
        // enough of data?
        if (socketBuffer.remaining() >= size)
            return;

        // to large for buffer...
        if (size > MAX_ENSURE_DATA_SIZE)    // half for SPLIT, half for SEGMENTED
            throw new IllegalArgumentException("requested for buffer size " + size + ", but maximum " + MAX_ENSURE_DATA_SIZE + " is allowed.");

        try {

            // subtract what was already processed
            final int pos = socketBuffer.position();
            storedPayloadSize -= pos - storedPosition;

            // SPLIT message case
            // no more data and we have some payload left => read buffer
            // NOTE: (storedPayloadSize >= size) does not work if size spans over multiple messages
            if (storedPayloadSize >= (storedLimit - pos)) {
                // just read up remaining payload
                // this will move current (<size) part of the buffer
                // to the beginning of the buffer
                ReadMode storedMode = readMode;
                readMode = ReadMode.SPLIT;
                readToBuffer(size, true);
                readMode = storedMode;
                storedPosition = socketBuffer.position();
                storedLimit = socketBuffer.limit();
                socketBuffer.limit(Math.min(storedPosition + storedPayloadSize, storedLimit));

                // check needed, if not enough data is available or
                // we run into segmented message
                ensureData(size);
            }
            // SEGMENTED message case
            else {
                // TODO check flags
                //if (flags && SEGMENTED_FLAGS_MASK == 0)
                //	throw IllegalStateException("segmented message expected, but current message flag does not indicate it");


                // copy remaining bytes of payload to safe area [0 to MAX_ENSURE_DATA_BUFFER_SIZE/2), if any
                // remaining is relative to payload since buffer is bounded from outside
                final int remainingBytes = socketBuffer.remaining();
                for (int i = 0; i < remainingBytes; i++)
                    socketBuffer.put(i, socketBuffer.get());

                // restore limit (there might be some data already present and readToBuffer needs to know real limit)
                socketBuffer.limit(storedLimit);

                // remember alignment offset of end of the message (to be restored)
                int storedAlignmentOffset = socketBuffer.position() % PVAConstants.PVA_ALIGNMENT;

                // skip post-message alignment bytes
                if (storedAlignmentOffset > 0) {
                    int toSkip = PVAConstants.PVA_ALIGNMENT - storedAlignmentOffset;
                    readToBuffer(toSkip, true);
                    int currentPos = socketBuffer.position();
                    socketBuffer.position(currentPos + toSkip);
                }

                // we expect segmented message, we expect header
                // that (and maybe some control packets) needs to be "removed"
                // so that we get combined payload
                ReadMode storedMode = readMode;
                readMode = ReadMode.SEGMENTED;
                processRead();
                readMode = storedMode;

                // make sure we have all the data (maybe we run into SPLIT)
                readToBuffer(size - remainingBytes + storedAlignmentOffset, true);

                // skip storedAlignmentOffset bytes (sender should padded start of segmented message)
                // SPLIT cannot mess with this, since start of the message, i.e. current position, is always aligned
                socketBuffer.position(socketBuffer.position() + storedAlignmentOffset);

                // copy before position (i.e. start of the payload)
                for (int i = remainingBytes - 1, j = socketBuffer.position() - 1; i >= 0; i--, j--)
                    socketBuffer.put(j, socketBuffer.get(i));
                startPosition = socketBuffer.position() - remainingBytes;
                socketBuffer.position(startPosition);

                storedPayloadSize += remainingBytes - storedAlignmentOffset;
                storedPosition = startPosition;
                storedLimit = socketBuffer.limit();
                socketBuffer.limit(Math.min(storedPosition + storedPayloadSize, storedLimit));

                // sequential small segmented messages in the buffer
                ensureData(size);
            }
        } catch (IOException ex) {
            try {
                close();
            } catch (IOException iex) {
                // noop, best-effort close
            }
            throw new ConnectionClosedException("Failed to ensure data to read buffer.", ex);
        }
    }


    public static final int alignedValue(int value, int alignment) {
        final int k = (alignment - 1);
        return (value + k) & (~k);
    }

    // TODO check
    public void alignData(int alignment) {

        final int k = (alignment - 1);
        final int pos = socketBuffer.position();
        int newpos = (pos + k) & (~k);
        if (pos == newpos)
            return;

        int diff = socketBuffer.limit() - newpos;
        if (diff > 0) {
            socketBuffer.position(newpos);
            return;
        }

        ensureData(diff);

        // position has changed, recalculate
        newpos = (socketBuffer.position() + k) & (~k);
        socketBuffer.position(newpos);
    }

    /// --------------------------------------------------------------- ///
    /// --------------------------------------------------------------- ///
    /// --------------------------------------------------------------- ///

    protected final ByteBuffer sendBuffer;

    private final int maxSendPayloadSize;

    private int lastMessageStartPosition = -1;
    private byte lastSegmentedMessageType = 0;
    private byte lastSegmentedMessageCommand = 0;
    private int nextMessagePayloadOffset = 0;

    /**
     * Cached byte-order flag. To be used only in send thread.
     */
    private int byteOrderFlag = 0x80;

    /**
     * Client/server flag.
     */
    private final int clientServerFlag;

    /**
     * Send buffer size.
     */
    private final int socketSendBufferSize;

    /**
     * Remote side transport socket receive buffer size.
     */
    protected int remoteTransportSocketReceiveBufferSize = PVAConstants.MAX_TCP_RECV;

    /**
     * Total bytes sent.
     */
    protected long totalBytesSent = 0;

    protected final Mailbox<TransportSender> sendQueue = new Mailbox<TransportSender>();
    protected final boolean blockingProcessQueue;

    private Thread senderThread = null;
    protected InetSocketAddress sendTo;


    public abstract void writePollOne() throws IOException;

    // why 0xFF? It gives NaN for floating points
    final static byte[] PADDING_BYTES = {
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
    };

    // TODO note alignment must be 2, 4, 8 ONLY!!!
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.SerializableControl#alignBuffer(int)
     */
    public void alignBuffer(int alignment) {

        final int k = (alignment - 1);
        final int pos = sendBuffer.position();
        int newpos = (pos + k) & (~k);
        if (pos == newpos)
            return;

		/*
		// there is always enough of space
		// since sendBuffer capacity % PVA_ALIGNMENT == 0
		sendBuffer.position(newpos);
		*/

        // for safety reasons we really pad (override previous message data)
        int padCount = newpos - pos;
        sendBuffer.put(PADDING_BYTES, 0, padCount);
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.impl.remote.TransportSendControl#startMessage(byte, int)
     */
    public final void startMessage(byte command, int ensureCapacity) {
        lastMessageStartPosition = -1;        // TODO revise this
        ensureBuffer(PVAConstants.PVA_MESSAGE_HEADER_SIZE + ensureCapacity + nextMessagePayloadOffset);
        lastMessageStartPosition = sendBuffer.position();
        sendBuffer.put(PVAConstants.PVA_MAGIC);
        sendBuffer.put(PVAConstants.PVA_VERSION);
        sendBuffer.put((byte) (lastSegmentedMessageType | byteOrderFlag | clientServerFlag));    // data message
        sendBuffer.put(command);    // command
        sendBuffer.putInt(0);        // temporary zero payload

        // apply offset
        if (nextMessagePayloadOffset > 0)
            sendBuffer.position(sendBuffer.position() + nextMessagePayloadOffset);
    }

    public final void putControlMessage(byte command, int data) {
        lastMessageStartPosition = -1;        // TODO revise this
        ensureBuffer(PVAConstants.PVA_MESSAGE_HEADER_SIZE);
        sendBuffer.put(PVAConstants.PVA_MAGIC);
        sendBuffer.put(PVAConstants.PVA_VERSION);
        sendBuffer.put((byte) (0x01 | byteOrderFlag | clientServerFlag));    // control message
        sendBuffer.put(command);    // command
        sendBuffer.putInt(data);        // data
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.impl.remote.TransportSendControl#endMessage()
     */
    public final void endMessage() {
        endMessage(false);
    }

    private final void endMessage(boolean hasMoreSegments) {
        if (lastMessageStartPosition >= 0) {
            final int lastPayloadBytePosition = sendBuffer.position();

            // align
            alignBuffer(PVAConstants.PVA_ALIGNMENT);

            // set paylaod size (non-aligned)
            final int payloadSize = lastPayloadBytePosition - lastMessageStartPosition -
                    PVAConstants.PVA_MESSAGE_HEADER_SIZE;
            this.sendBuffer.putInt(lastMessageStartPosition + (Short.SIZE / Byte.SIZE + 2),
                    payloadSize);

            // set segmented bit
            if (hasMoreSegments) {
                // first segment
                if (lastSegmentedMessageType == 0) {
                    final int flagsPosition = lastMessageStartPosition + Short.SIZE / Byte.SIZE;
                    final byte type = sendBuffer.get(flagsPosition);
                    // set first segment bit
                    sendBuffer.put(flagsPosition, (byte) (type | 0x10));
                    // first + last segment bit == in-between segment
                    lastSegmentedMessageType = (byte) (type | 0x30);
                    lastSegmentedMessageCommand = sendBuffer.get(flagsPosition + 1);
                }
                nextMessagePayloadOffset = lastPayloadBytePosition % PVAConstants.PVA_ALIGNMENT;
            } else {
                // last segment
                if (lastSegmentedMessageType != 0) {
                    final int flagsPosition = lastMessageStartPosition + Short.SIZE / Byte.SIZE;
                    // set last segment bit (by clearing first segment bit)
                    sendBuffer.put(flagsPosition, (byte) (lastSegmentedMessageType & 0xEF));
                    lastSegmentedMessageType = 0;
                }
                nextMessagePayloadOffset = 0;
            }

            // TODO
			/*
			// manage markers
			final int position = sendBuffer.position();
			final int bytesLeft = sendBuffer.remaining();
			if (position >= nextMarkerPosition && bytesLeft >= PVAConstants.PVA_MESSAGE_HEADER_SIZE)
			{
				sendBuffer.put(PVAConstants.PVA_MAGIC);
				sendBuffer.put(PVAConstants.PVA_VERSION);
				sendBuffer.put((byte)(0x01 | byteOrderFlag));	// control data
				sendBuffer.put((byte)0);	// marker
				sendBuffer.putInt((int)(totalBytesSent + position + PVAConstants.PVA_MESSAGE_HEADER_SIZE));
				nextMarkerPosition = position + markerPeriodBytes;
			}
			*/
            lastMessageStartPosition = -1;
        }
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.SerializableControl#ensureBuffer(int)
     */
    public final void ensureBuffer(int size) {
        if (sendBuffer.remaining() >= size)
            return;

        // too large for buffer...
        if (maxSendPayloadSize < size)
            throw new IllegalArgumentException("requested for buffer size " + size + ", but only " + maxSendPayloadSize + " available.");

        while (sendBuffer.remaining() < size)
            flush(false);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.SerializableControl#flushSerializeBuffer()
     */
    public void flushSerializeBuffer() {
        flush(false);
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.impl.remote.TransportSendControl#flush(boolean)
     */
    public void flush(boolean lastMessageCompleted) {

        // automatic end
        endMessage(!lastMessageCompleted);

        sendBuffer.flip();

        try {
            send(sendBuffer);
        } catch (IOException e) {
            try {
                if (isOpen())
                    close();
            } catch (IOException iex) {
                // noop, best-effort close
            }
            throw new ConnectionClosedException("Failed to send buffer.", e);
        }

        sendBuffer.clear();

        lastMessageStartPosition = -1;

        // start with last header
        if (!lastMessageCompleted && lastSegmentedMessageType != 0)
            startMessage(lastSegmentedMessageCommand, 0);
    }

    public enum WriteMode {PROCESS_SEND_QUEUE, WAIT_FOR_READY_SIGNAL}

    ;
    protected WriteMode writeMode = WriteMode.PROCESS_SEND_QUEUE;
    protected boolean writeOpReady = false;

    public final void processWrite() throws IOException, ConnectionClosedException {
        //System.out.println("processWrite");
        switch (writeMode) {
            case PROCESS_SEND_QUEUE:
                processSendQueue();
                break;
            case WAIT_FOR_READY_SIGNAL:
                writeOpReady = true;
                break;
        }
    }

    /**
     * Send a buffer through the transport.
     * NOTE: TCP sent buffer/sending has to be synchronized (not done by this method).
     *
     * @param buffer buffer to be sent
     * @throws IOException IO exception rethrown.
     */
    protected void send(ByteBuffer buffer) throws IOException {
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
        if (bytesToSend > maxBytesToSend) {
            bytesToSend = maxBytesToSend;
            buffer.limit(buffer.position() + bytesToSend);
        }

//context.getLogger().finest("Sending " + bytesToSend + " of total " + limit + " bytes in the packet to " + socketAddress + ".");
//System.out.println("Sending " + bytesToSend + " of total " + limit + " bytes in the packet to " + getLastReadBufferSocketAddress() + ".");

        int tries = 0;
        while (buffer.hasRemaining()) {

//int p = buffer.position();
            final int bytesSent = this.write(buffer);
//HexDump.hexDump("WRITE", buffer.array(), p, bytesSent);

            if (bytesSent < 0) {
                // connection lost
                close();
                throw new ConnectionClosedException("bytesSent < 0");
            } else if (bytesSent == 0) {
//context.getLogger().finest("Buffer full, position " + buffer.position() + " of total " + limit + " bytes.");
//System.out.println("Buffer full, position " + buffer.position() + " of total " + limit + " bytes.");

                sendBufferFull(tries++);
                continue;
            }
            totalBytesSent += bytesSent;

            // readjust limit
            if (bytesToSend == maxBytesToSend) {
                bytesToSend = limit - buffer.position();
                if (bytesToSend > maxBytesToSend)
                    bytesToSend = maxBytesToSend;
                buffer.limit(buffer.position() + bytesToSend);
            }
            tries = 0;

//context.getLogger().finest("Sent, position " + buffer.position() + " of total " + limit + " bytes.");
//System.out.println("Sent, position " + buffer.position() + " of total " + limit + " bytes.");
        }
    }

	/*
	// sendBufferFull for non-blocking
				writeOpReady = false;
				writeMode = WriteMode.WAIT_FOR_READY_SIGNAL;
				this.writePollOne();
				writeMode = WriteMode.PROCESS_SEND_QUEUE;
	 */

    protected abstract void sendBufferFull(int tries) throws IOException;

    public abstract void scheduleSend();

    public abstract void sendCompleted();

    // NOTE: code can use Thread.interrupt() to get out of blocking wait
    // can be called anytime (no race condition problem)
    // method ensures that messages are processed (if connection not closed) even if interrupted
    public final void processSendQueue() throws IOException {
        try {
            int senderProcessed = 0;
            while (senderProcessed++ < MAX_MESSAGE_SEND) {
                TransportSender sender = sendQueue.take(-1);
                if (sender == null) {
                    // flush
                    if (sendBuffer.position() > 0)
                        flush(true);

                    sendCompleted();    // do not schedule sending

                    if (blockingProcessQueue) {
                        if (terminated())            // termination
                            break;
                        sender = sendQueue.take(0);
                        if (sender == null)        // termination (we want to process even if shutdown)
                            break;
                    } else
                        return;
                }

                processSender(sender);
            }
        } catch (InterruptedException ie) {
            // noop, allowed and expected in blocking
        }

        // flush
        if (sendBuffer.position() > 0)
            flush(true);

    }

    public final void clearSendQueue() {
        sendQueue.clear();
    }

    public final void enqueueSendRequest(TransportSender sender) {
        sendQueue.put(sender);
        scheduleSend();
    }

    public void setSenderThread() {
        senderThread = Thread.currentThread();
    }

    private final void processSender(TransportSender sender) {
        sender.lock();
        try {
            lastMessageStartPosition = sendBuffer.position();

            sender.send(sendBuffer, this);

            // automatic end (to set payload size)
            endMessage(false);
        } catch (ConnectionClosedException cce) {
            // silence cce

            try {
                close();
            } catch (IOException e) {
                // noop
            }

            throw new ConnectionClosedException("exception caught: " + cce.getMessage());
        } catch (Throwable th) {
            logger.log(Level.FINE, "exception caught while processing send message", th);

            try {
                close();
            } catch (IOException e) {
                // noop
            }

            throw new ConnectionClosedException("exception caught: " + th.getMessage());
        } finally {
            sender.unlock();
        }
    }

    // TODO
    final boolean lowLatency = false;

    public final void enqueueSendRequest(TransportSender sender, int requiredBufferSize) {
        if (senderThread == Thread.currentThread() &&
                sendQueue.isEmpty() &&
                sendBuffer.remaining() >= requiredBufferSize) {
            processSender(sender);
            if (sendBuffer.position() > 0) {
                if (lowLatency)
                    flush(true);
                else
                    scheduleSend();
            }
        } else
            enqueueSendRequest(sender);
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.impl.remote.TransportSendControl#setRecipient(java.net.InetSocketAddress)
     */
    public void setRecipient(InetSocketAddress sendTo) {
        this.sendTo = sendTo;
    }


    public void setByteOrder(ByteOrder byteOrder) {
        socketBuffer.order(byteOrder);
        // TODO sync
        sendBuffer.order(byteOrder);
        byteOrderFlag = ByteOrder.BIG_ENDIAN == byteOrder ? 0x80 : 0x00;
    }

    public abstract boolean terminated();
}
