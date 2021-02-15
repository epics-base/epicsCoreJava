package org.epics.pvaccess.impl.remote.codec.test.perf;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import org.epics.pvaccess.impl.remote.codec.AbstractCodec;
import org.epics.pvdata.pv.Field;

class TestCodec extends AbstractCodec
{
	static interface ReadPollOneCallback {
		public void readPollOne() throws IOException;
	}

	static interface WritePollOneCallback {
		public void writePollOne() throws IOException;
	}

	int closedCount = 0;
	int invalidDataStreamCount = 0;
	int scheduleSendCount = 0;
	int sendCompletedCount = 0;
	int sendBufferFullCount = 0;
	int readPollOneCount = 0;
	int writePollOneCount = 0;
	int messagesProcessed = 0;

	boolean throwExceptionOnSend = false;

	ByteBuffer readBuffer;
	final ByteBuffer writeBuffer;

	TestCodec.ReadPollOneCallback readPollOneCallback = null;
	TestCodec.WritePollOneCallback writePollOneCallback = null;

	boolean readPayload = false;
	boolean disconnected = false;

	int forcePayloadRead = -1;

	public TestCodec(int bufferSize) throws IOException {
		this(bufferSize, bufferSize);
	}

	public TestCodec(int receiveBufferSize, int sendBufferSize) throws IOException {
		this(receiveBufferSize, sendBufferSize, false);
	}

	public TestCodec(int receiveBufferSize, int sendBufferSize, boolean blocking) throws IOException {
		super(false, ByteBuffer.allocate(receiveBufferSize), ByteBuffer.allocate(sendBufferSize),
				sendBufferSize/10, blocking, Logger.getLogger("TestCodec"));
		readBuffer = ByteBuffer.allocate(receiveBufferSize);
		writeBuffer = ByteBuffer.allocate(sendBufferSize);
	}

	public ReadMode getReadMode()
	{
		return readMode;
	}

	public WriteMode getWriteMode()
	{
		return writeMode;
	}

	public ByteBuffer getSendBuffer()
	{
		return sendBuffer;
	}

	void reset()
	{
		closedCount = 0;
		invalidDataStreamCount = 0;
		scheduleSendCount = 0;
		sendCompletedCount = 0;
		sendBufferFullCount = 0;
		readPollOneCount = 0;
		writePollOneCount = 0;
		messagesProcessed = 0;
		readBuffer.clear();
		writeBuffer.clear();
	}

	public int read(ByteBuffer buffer) throws IOException {
		if (disconnected)
			return -1;

		int startPos = readBuffer.position();
		//buffer.put(readBuffer);
		//while (buffer.hasRemaining() && readBuffer.hasRemaining())
		//	buffer.put(readBuffer.get());

		int bufferRemaining = buffer.remaining();
		int readBufferRemaining = readBuffer.remaining();
		if (bufferRemaining >= readBufferRemaining)
			buffer.put(readBuffer);
		else
		{
			// TODO this could be optimized
			for (int i = 0; i < bufferRemaining; i++)
				buffer.put(readBuffer.get());
		}
		return readBuffer.position() - startPos;
	}

	public int write(ByteBuffer buffer) throws IOException {
		if (disconnected)
			return -1;	// TODO: not by the JavaDoc API spec
		if (throwExceptionOnSend)
			throw new IOException("text IO exception");

		// we could write remaining bytes, but for test this is enought
		if (buffer.remaining() > writeBuffer.remaining())
			return 0;

		int startPos = buffer.position();
		writeBuffer.put(buffer);
		return buffer.position() - startPos;
	}

	public void transferToReadBuffer() throws IOException
	{
		flushSerializeBuffer();
		writeBuffer.flip();

		readBuffer.clear();
		readBuffer.put(writeBuffer);
		readBuffer.flip();

		writeBuffer.clear();
	}

	public void addToReadBuffer() throws IOException
	{
		flushSerializeBuffer();
		writeBuffer.flip();

		//readBuffer.clear();
		readBuffer.put(writeBuffer);
		readBuffer.flip();

		writeBuffer.clear();
	}

	public void close() throws IOException {
		closedCount++;
	}

	public boolean isOpen() {
		return closedCount == 0;
	}

	@Override
	public void processControlMessage() {
		// must be here so that to prevent optimizations
		if (flags != (byte)0x81 || command != (byte)0x23)
			throw new RuntimeException("bad data");
		messagesProcessed++;
	}

	ByteBuffer payload = ByteBuffer.allocate(MessageProcessPerformance.MAX_PAYLOAD_SIZE);

	@Override
	public void processApplicationMessage() throws IOException {
		// must be here so that to prevent optimizations
		if (flags != (byte)0x80 || command != (byte)0x23)
			throw new RuntimeException("bad data");

		if (readPayload && payloadSize > 0)
		{
			payload.clear();
			// no fragmentation supported by this implementation
			int toRead = forcePayloadRead >= 0 ? forcePayloadRead : payloadSize;
			while (toRead > 0)
			{
				int partitalRead = Math.min(toRead, AbstractCodec.MAX_ENSURE_DATA_SIZE);
				ensureData(partitalRead);
				int pos = payload.position();
				payload.put(socketBuffer);
				int read = payload.position() - pos;
				toRead -= read;
			}
		}
		messagesProcessed++;
	}

	@Override
	public InetSocketAddress getLastReadBufferSocketAddress() {
		try {
			return new InetSocketAddress(InetAddress.getLocalHost(), 1234);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void invalidDataStreamHandler() {
		invalidDataStreamCount++;
	}

	@Override
	public void readPollOne() throws IOException {
		readPollOneCount++;
		if (readPollOneCallback != null)
			readPollOneCallback.readPollOne();
	}

	@Override
	public void writePollOne() throws IOException {
		writePollOneCount++;
		if (writePollOneCallback != null)
			writePollOneCallback.writePollOne();
	}

	@Override
	protected void sendBufferFull(int tries) throws IOException {
		sendBufferFullCount++;
		writeOpReady = false;
		writeMode = WriteMode.WAIT_FOR_READY_SIGNAL;
		this.writePollOne();
		writeMode = WriteMode.PROCESS_SEND_QUEUE;
	}

	@Override
	public void scheduleSend() {
		scheduleSendCount++;
	}

	@Override
	public void sendCompleted() {
		sendCompletedCount++;
	}

	@Override
	public boolean terminated() {
		return false;
	}

	public void cachedSerialize(Field field, ByteBuffer buffer) {
		// no cache
		field.serialize(buffer, this);
	}
}
