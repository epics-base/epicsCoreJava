package org.epics.pvaccess.impl.remote.codec.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.TransportSender;
import org.epics.pvaccess.impl.remote.codec.AbstractCodec;
import org.epics.pvaccess.impl.remote.codec.AbstractCodec.ReadMode;
import org.epics.pvaccess.impl.remote.codec.AbstractCodec.WriteMode;
import org.epics.pvaccess.impl.remote.codec.ConnectionClosedException;
import org.epics.pvaccess.impl.remote.codec.test.AbstractCodecTest.TestCodec.ReadPollOneCallback;
import org.epics.pvaccess.impl.remote.codec.test.AbstractCodecTest.TestCodec.WritePollOneCallback;
import org.epics.pvdata.pv.Field;



public class AbstractCodecTest extends TestCase {

 	public AbstractCodecTest(String methodName) {
		super(methodName);
	}

	static class PVAMessage {
		byte version;
		byte flags;
		byte command;
		int payloadSize;
		ByteBuffer payload;

		public PVAMessage(byte version, byte flags, byte command, int payloadSize) {
			this.version = version;
			this.flags = flags;
			this.command = command;
			this.payloadSize = payloadSize;
		}

	}
	static class TestCodec extends AbstractCodec
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

		boolean throwExceptionOnSend = false;

		ByteBuffer readBuffer;
		final ByteBuffer writeBuffer;

		ReadPollOneCallback readPollOneCallback = null;
		WritePollOneCallback writePollOneCallback = null;

		final ArrayList<PVAMessage> receivedAppMessages = new ArrayList<PVAMessage>();
		final ArrayList<PVAMessage> receivedControlMessages = new ArrayList<PVAMessage>();

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
			readBuffer.clear();
			writeBuffer.clear();
			receivedAppMessages.clear();
			receivedControlMessages.clear();
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
			// alignment check
			if (socketBuffer.position() % PVAConstants.PVA_ALIGNMENT != 0)
				throw new IllegalStateException("message not aligned");

			receivedControlMessages.add(new PVAMessage(version, flags, command, payloadSize));
		}

		@Override
		public void processApplicationMessage() throws IOException {
			// alignment check
		 	if (socketBuffer.position() % PVAConstants.PVA_ALIGNMENT != 0)
				throw new IllegalStateException("message not aligned");

			PVAMessage caMessage = new PVAMessage(version, flags, command, payloadSize);
			if (readPayload && payloadSize > 0)
			{
				// no fragmentation supported by this implementation
				int toRead = forcePayloadRead >= 0 ? forcePayloadRead : payloadSize;
				caMessage.payload = ByteBuffer.allocate(toRead);
				while (toRead > 0)
				{
					int partitalRead = Math.min(toRead, AbstractCodec.MAX_ENSURE_DATA_SIZE);
					ensureData(partitalRead);
					int pos = caMessage.payload.position();
					caMessage.payload.put(socketBuffer);
					int read = caMessage.payload.position() - pos;
					toRead -= read;
				}
			}
			receivedAppMessages.add(caMessage);
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
			field.serialize(buffer, this);
		}
	}

	private static int DEFAULT_BUFFER_SIZE = 10240;

	public void testHeaderProcess() throws Throwable
	{
		TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0x01);
		codec.readBuffer.put((byte)0x23);
		codec.readBuffer.putInt(0x456789AB);
		codec.readBuffer.flip();

		codec.processRead();

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(1, codec.receivedControlMessages.size());
		assertEquals(0, codec.receivedAppMessages.size());
		PVAMessage header = codec.receivedControlMessages.get(0);
		assertEquals(header.version, PVAConstants.PVA_VERSION);
		assertEquals(header.flags, 0x01);
		assertEquals(header.command, 0x23);
		assertEquals(header.payloadSize, 0x456789AB);

		codec.reset();

		// two at the time, app and control
		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0x00);
		codec.readBuffer.put((byte)0x20);
		codec.readBuffer.putInt(0x00000000);

		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0x81);
		codec.readBuffer.put((byte)0xEE);
		codec.readBuffer.putInt(0xDDCCBBAA);
		codec.readBuffer.flip();

		codec.processRead();

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(1, codec.receivedControlMessages.size());
		assertEquals(1, codec.receivedAppMessages.size());

		// app, no payload
		header = codec.receivedAppMessages.get(0);
		assertEquals(header.version, PVAConstants.PVA_VERSION);
		assertEquals(header.flags, (byte)0x00);
		assertEquals(header.command, (byte)0x20);
		assertEquals(header.payloadSize, 0x00000000);

		// control
		header = codec.receivedControlMessages.get(0);
		assertEquals(header.version, PVAConstants.PVA_VERSION);
		assertEquals(header.flags, (byte)0x81);
		assertEquals(header.command, (byte)0xEE);
		assertEquals(header.payloadSize, 0xDDCCBBAA);
	}

	public void testInvalidHeaderMagic() throws Throwable
	{
		TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		codec.readBuffer.put((byte)0x00);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0x01);
		codec.readBuffer.put((byte)0x23);
		codec.readBuffer.putInt(0x456789AB);
		codec.readBuffer.flip();

		codec.processRead();

		assertEquals(1, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(0, codec.receivedControlMessages.size());
		assertEquals(0, codec.receivedAppMessages.size());
	}

	public void testInvalidHeaderSegmentedInNormal() throws Throwable
	{
		byte[] invalidFlagsValues = new byte[] {(byte)0x20, (byte)(0x30+0x80)};
		for (int i = 0; i < invalidFlagsValues.length; i++)
		{
			TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
			codec.readBuffer.put(PVAConstants.PVA_MAGIC);
			codec.readBuffer.put(PVAConstants.PVA_VERSION);
			codec.readBuffer.put(invalidFlagsValues[i]);
			codec.readBuffer.put((byte)0x23);
			//codec.readBuffer.putInt(0);
			codec.readBuffer.putInt(i);   // to check zero-payload
			codec.readBuffer.flip();

			codec.processRead();

			//assertEquals(1, codec.invalidDataStreamCount);
			assertEquals(i != 0 ? 1 : 0, codec.invalidDataStreamCount);
			assertEquals(0, codec.closedCount);
			assertEquals(0, codec.receivedControlMessages.size());
			assertEquals(0, codec.receivedAppMessages.size());
		}
	}

	public void testInvalidHeaderPayloadNotRead() throws Throwable
	{
		TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0x80);
		codec.readBuffer.put((byte)0x23);
		codec.readBuffer.putInt(0x456789AB);
		codec.readBuffer.flip();

		codec.processRead();

		assertEquals(1, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(0, codec.receivedControlMessages.size());
		assertEquals(1, codec.receivedAppMessages.size());
	}

	public void testHeaderSplitRead() throws Throwable
	{
		TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0x01);
		codec.readBuffer.flip();

		codec.processRead();

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(0, codec.receivedControlMessages.size());
		assertEquals(0, codec.receivedAppMessages.size());

		codec.readBuffer.clear();

		codec.readBuffer.put((byte)0x23);
		codec.readBuffer.putInt(0x456789AB);
		codec.readBuffer.flip();

		codec.processRead();

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(1, codec.receivedControlMessages.size());
		assertEquals(0, codec.receivedAppMessages.size());

		// app, no payload
		PVAMessage header = codec.receivedControlMessages.get(0);
		assertEquals(header.version, PVAConstants.PVA_VERSION);
		assertEquals(header.flags, (byte)0x01);
		assertEquals(header.command, (byte)0x23);
		assertEquals(header.payloadSize, 0x456789AB);
	}

	public void testNonEmptyPayload() throws Throwable
	{
		// no misalignment
		TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		codec.readPayload = true;

		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0x80);
		codec.readBuffer.put((byte)0x23);
		codec.readBuffer.putInt(PVAConstants.PVA_ALIGNMENT);
		for (int i = 0; i < PVAConstants.PVA_ALIGNMENT; i++)
			codec.readBuffer.put((byte)i);
		codec.readBuffer.flip();

		codec.processRead();

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(0, codec.receivedControlMessages.size());
		assertEquals(1, codec.receivedAppMessages.size());

		// app, no payload
		PVAMessage header = codec.receivedAppMessages.get(0);
		assertNotNull(header.payload);
		header.payload.flip();
		assertEquals(PVAConstants.PVA_ALIGNMENT, header.payload.limit());
	}

	public void testNormalAlignment() throws Throwable
	{
		TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		codec.readPayload = true;

		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0x80);
		codec.readBuffer.put((byte)0x23);
		int payloadSize1 = PVAConstants.PVA_ALIGNMENT+1;
		codec.readBuffer.putInt(payloadSize1);
		for (int i = 0; i < payloadSize1; i++)
			codec.readBuffer.put((byte)i);
		// align
		int aligned = AbstractCodec.alignedValue(payloadSize1, PVAConstants.PVA_ALIGNMENT);
		for (int i = payloadSize1; i < aligned; i++)
			codec.readBuffer.put((byte)0xFF);


		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0x80);
		codec.readBuffer.put((byte)0x45);
		int payloadSize2 = 2*PVAConstants.PVA_ALIGNMENT-1;
		codec.readBuffer.putInt(payloadSize2);
		for (int i = 0; i < payloadSize2; i++)
			codec.readBuffer.put((byte)i);
		aligned = AbstractCodec.alignedValue(payloadSize2, PVAConstants.PVA_ALIGNMENT);
		for (int i = payloadSize2; i < aligned; i++)
			codec.readBuffer.put((byte)0xFF);

		codec.readBuffer.flip();
		codec.processRead();

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(0, codec.receivedControlMessages.size());
		assertEquals(2, codec.receivedAppMessages.size());

		PVAMessage msg = codec.receivedAppMessages.get(0);
		assertEquals(msg.payloadSize, payloadSize1);
		assertNotNull(msg.payload);
		msg.payload.flip();
		assertEquals(payloadSize1, msg.payload.limit());
		for (int i = 0; i < msg.payloadSize; i++)
			assertEquals((byte)i, msg.payload.get());

		msg = codec.receivedAppMessages.get(1);
		assertEquals(msg.payloadSize, payloadSize2);
		assertNotNull(msg.payload);
		msg.payload.flip();
		assertEquals(payloadSize2, msg.payload.limit());
		for (int i = 0; i < msg.payloadSize; i++)
			assertEquals((byte)i, msg.payload.get());
	}

	public void testSplitAlignment() throws Throwable
	{
		// "<=" used instead of "==" to suppress compiler warning
		if (PVAConstants.PVA_ALIGNMENT <= 1)
			return;

		// used to suppress dead code
		@SuppressWarnings("unused")
		final TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		codec.readPayload = true;

		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0x80);
		codec.readBuffer.put((byte)0x23);
		final int payloadSize1 = PVAConstants.PVA_ALIGNMENT+1;
		codec.readBuffer.putInt(payloadSize1);
		for (int i = 0; i < payloadSize1-2; i++)
			codec.readBuffer.put((byte)i);

		final int payloadSize2 = 2*PVAConstants.PVA_ALIGNMENT-1;

		ReadPollOneCallback pollCB = new ReadPollOneCallback() {

			public void readPollOne() throws IOException {

				if (codec.readPollOneCount == 1)
				{
					codec.readBuffer.clear();
					for (int i = payloadSize1-2; i < payloadSize1; i++)
						codec.readBuffer.put((byte)i);
					// align
					int aligned = AbstractCodec.alignedValue(payloadSize1, PVAConstants.PVA_ALIGNMENT);
					for (int i = payloadSize1; i < aligned; i++)
						codec.readBuffer.put((byte)0xFF);


					codec.readBuffer.put(PVAConstants.PVA_MAGIC);
					codec.readBuffer.put(PVAConstants.PVA_VERSION);
					codec.readBuffer.put((byte)0x80);
					codec.readBuffer.put((byte)0x45);
					codec.readBuffer.putInt(payloadSize2);
					for (int i = 0; i < payloadSize2; i++)
						codec.readBuffer.put((byte)i);
					codec.readBuffer.flip();

				}

				else if (codec.readPollOneCount == 2)
				{
					codec.readBuffer.clear();
					int aligned = AbstractCodec.alignedValue(payloadSize2, PVAConstants.PVA_ALIGNMENT);
					for (int i = payloadSize2; i < aligned; i++)
						codec.readBuffer.put((byte)0xFF);
					codec.readBuffer.flip();
				}

				else
					throw new RuntimeException("should not happen");
			}
		};
		codec.readPollOneCallback = pollCB;

		codec.readBuffer.flip();
		codec.processRead();

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(0, codec.receivedControlMessages.size());
		assertEquals(2, codec.receivedAppMessages.size());
		assertEquals(2, codec.readPollOneCount);

		PVAMessage msg = codec.receivedAppMessages.get(0);
		assertEquals(msg.payloadSize, payloadSize1);
		assertNotNull(msg.payload);
		msg.payload.flip();
		assertEquals(payloadSize1, msg.payload.limit());
		for (int i = 0; i < msg.payloadSize; i++)
			assertEquals((byte)i, msg.payload.get());

		msg = codec.receivedAppMessages.get(1);
		assertEquals(msg.payloadSize, payloadSize2);
		assertNotNull(msg.payload);
		msg.payload.flip();
		assertEquals(payloadSize2, msg.payload.limit());
		for (int i = 0; i < msg.payloadSize; i++)
			assertEquals((byte)i, msg.payload.get());
	}

	public void testSegmentedMessage() throws Throwable
	{
		// no misalignment
		final TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		codec.readPayload = true;

		// 1st
		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0x90);
		codec.readBuffer.put((byte)0x01);
		final int payloadSize1 = PVAConstants.PVA_ALIGNMENT;
		codec.readBuffer.putInt(payloadSize1);
		int c = 0;
		for (int i = 0; i < payloadSize1; i++)
			codec.readBuffer.put((byte)(c++));

		// 2nd
		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0xB0);
		codec.readBuffer.put((byte)0x01);
		final int payloadSize2 = 2*PVAConstants.PVA_ALIGNMENT;
		codec.readBuffer.putInt(payloadSize2);
		for (int i = 0; i < payloadSize2; i++)
			codec.readBuffer.put((byte)(c++));

		// control in between
		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0x81);
		codec.readBuffer.put((byte)0xEE);
		codec.readBuffer.putInt(0xDDCCBBAA);

		// 3rd
		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0xB0);
		codec.readBuffer.put((byte)0x01);
		final int payloadSize3 = PVAConstants.PVA_ALIGNMENT;
		codec.readBuffer.putInt(payloadSize3);
		for (int i = 0; i < payloadSize3; i++)
			codec.readBuffer.put((byte)(c++));

		// 4t (last)
		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0xA0);
		codec.readBuffer.put((byte)0x01);
		final int payloadSize4 = 2*PVAConstants.PVA_ALIGNMENT;
		codec.readBuffer.putInt(payloadSize4);
		for (int i = 0; i < payloadSize4; i++)
			codec.readBuffer.put((byte)(c++));

		codec.readBuffer.flip();

		final int payloadSizeSum = payloadSize1+payloadSize2+payloadSize3+payloadSize4;
		codec.forcePayloadRead = payloadSizeSum;

		codec.processRead();

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(1, codec.receivedControlMessages.size());
		assertEquals(1, codec.receivedAppMessages.size());
		assertEquals(0, codec.readPollOneCount);

		PVAMessage msg = codec.receivedAppMessages.get(0);
		assertNotNull(msg.payload);
		msg.payload.flip();
		assertEquals(payloadSizeSum, msg.payload.limit());
		for (int i = 0; i < msg.payloadSize; i++)
			assertEquals((byte)i, msg.payload.get());

		// control
		msg = codec.receivedControlMessages.get(0);
		assertEquals(msg.version, PVAConstants.PVA_VERSION);
		assertEquals(msg.flags, (byte)0x81);
		assertEquals(msg.command, (byte)0xEE);
		assertEquals(msg.payloadSize, 0xDDCCBBAA);
	}

	public void testSegmentedInvalidInBetweenFlagsMessage() throws Throwable
	{
		// no misalignment
		final TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		codec.readPayload = true;

		// 1st
		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0x90);
		codec.readBuffer.put((byte)0x01);
		final int payloadSize1 = PVAConstants.PVA_ALIGNMENT;
		codec.readBuffer.putInt(payloadSize1);
		int c = 0;
		for (int i = 0; i < payloadSize1; i++)
			codec.readBuffer.put((byte)(c++));

		// 2nd
		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0x90);	// invalid flag, should be 0xB0
		codec.readBuffer.put((byte)0x01);
		final int payloadSize2 = 2*PVAConstants.PVA_ALIGNMENT;
		codec.readBuffer.putInt(payloadSize2);
		for (int i = 0; i < payloadSize2; i++)
			codec.readBuffer.put((byte)(c++));

		// control in between
		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0x81);
		codec.readBuffer.put((byte)0xEE);
		codec.readBuffer.putInt(0xDDCCBBAA);

		// 3rd
		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0xB0);
		codec.readBuffer.put((byte)0x01);
		final int payloadSize3 = PVAConstants.PVA_ALIGNMENT;
		codec.readBuffer.putInt(payloadSize3);
		for (int i = 0; i < payloadSize3; i++)
			codec.readBuffer.put((byte)(c++));

		// 4t (last)
		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0xA0);
		codec.readBuffer.put((byte)0x01);
		final int payloadSize4 = 2*PVAConstants.PVA_ALIGNMENT;
		codec.readBuffer.putInt(payloadSize4);
		for (int i = 0; i < payloadSize4; i++)
			codec.readBuffer.put((byte)(c++));

		codec.readBuffer.flip();

		final int payloadSizeSum = payloadSize1+payloadSize2+payloadSize3+payloadSize4;
		codec.forcePayloadRead = payloadSizeSum;

		codec.processRead();

		assertEquals(1, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(0, codec.receivedControlMessages.size());
		assertEquals(0, codec.receivedAppMessages.size());
	}

	public void testSegmentedMessageAlignment() throws Throwable
	{
		final TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		codec.readPayload = true;

		// 1st
		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0x90);
		codec.readBuffer.put((byte)0x01);
		final int payloadSize1 = PVAConstants.PVA_ALIGNMENT+1;
		codec.readBuffer.putInt(payloadSize1);
		int c = 0;
		for (int i = 0; i < payloadSize1; i++)
			codec.readBuffer.put((byte)(c++));
		int aligned = AbstractCodec.alignedValue(payloadSize1, PVAConstants.PVA_ALIGNMENT);
		for (int i = payloadSize1; i < aligned; i++)
			codec.readBuffer.put((byte)0xFF);


		// 2nd
		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0xB0);
		codec.readBuffer.put((byte)0x01);
		final int payloadSize2 = 2*PVAConstants.PVA_ALIGNMENT-1;
		final int payloadSize2Real = payloadSize2 + payloadSize1 % PVAConstants.PVA_ALIGNMENT;
		codec.readBuffer.putInt(payloadSize2Real);

		// pre-message padding
		for (int i = 0; i < payloadSize1 % PVAConstants.PVA_ALIGNMENT; i++)
			codec.readBuffer.put((byte)0xEE);

		for (int i = 0; i < payloadSize2; i++)
			codec.readBuffer.put((byte)(c++));
		aligned = AbstractCodec.alignedValue(payloadSize2Real, PVAConstants.PVA_ALIGNMENT);
		for (int i = payloadSize2Real; i < aligned; i++)
			codec.readBuffer.put((byte)0xFF);

		// 3rd
		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0xB0);
		codec.readBuffer.put((byte)0x01);
		final int payloadSize3 = PVAConstants.PVA_ALIGNMENT+2;
		final int payloadSize3Real = payloadSize3 + payloadSize2Real % PVAConstants.PVA_ALIGNMENT;
		codec.readBuffer.putInt(payloadSize3Real);

		// pre-message padding required
		for (int i = 0; i < payloadSize2Real % PVAConstants.PVA_ALIGNMENT; i++)
			codec.readBuffer.put((byte)0xEE);

		for (int i = 0; i < payloadSize3; i++)
			codec.readBuffer.put((byte)(c++));
		aligned = AbstractCodec.alignedValue(payloadSize3Real, PVAConstants.PVA_ALIGNMENT);
		for (int i = payloadSize3Real; i < aligned; i++)
			codec.readBuffer.put((byte)0xFF);

		// 4t (last)
		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0xA0);
		codec.readBuffer.put((byte)0x01);
		final int payloadSize4 = 2*PVAConstants.PVA_ALIGNMENT+3;
		final int payloadSize4Real = payloadSize4 + payloadSize3Real % PVAConstants.PVA_ALIGNMENT;
		codec.readBuffer.putInt(payloadSize4Real);

		// pre-message padding required
		for (int i = 0; i < payloadSize3Real % PVAConstants.PVA_ALIGNMENT; i++)
			codec.readBuffer.put((byte)0xEE);

		for (int i = 0; i < payloadSize4; i++)
			codec.readBuffer.put((byte)(c++));
		aligned = AbstractCodec.alignedValue(payloadSize4Real, PVAConstants.PVA_ALIGNMENT);
		for (int i = payloadSize4Real; i < aligned; i++)
			codec.readBuffer.put((byte)0xFF);

		codec.readBuffer.flip();

		final int payloadSizeSum = payloadSize1+payloadSize2+payloadSize3+payloadSize4;
		codec.forcePayloadRead = payloadSizeSum;

		codec.processRead();

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(0, codec.receivedControlMessages.size());
		assertEquals(1, codec.receivedAppMessages.size());
		assertEquals(0, codec.readPollOneCount);

		PVAMessage msg = codec.receivedAppMessages.get(0);
		assertNotNull(msg.payload);
		msg.payload.flip();
		assertEquals(payloadSizeSum, msg.payload.limit());
		for (int i = 0; i < msg.payloadSize; i++)
			assertEquals((byte)i, msg.payload.get());
	}

	public void testSegmentedSplitMessage() throws Throwable
	{
		for (int firstMessagePayloadSize = 1;	// cannot be zero
		firstMessagePayloadSize <= 3*PVAConstants.PVA_ALIGNMENT;
		firstMessagePayloadSize++)
		{
			for (int secondMessagePayloadSize = 0;
					secondMessagePayloadSize <= 2*PVAConstants.PVA_ALIGNMENT;
					secondMessagePayloadSize++)
			{
				for (int thirdMessagePayloadSize = 1;		// cannot be zero
					thirdMessagePayloadSize <= 2*PVAConstants.PVA_ALIGNMENT;
					thirdMessagePayloadSize++)
				{
					//System.out.println(firstMessagePayloadSize + " / " + secondMessagePayloadSize + "  / " + thirdMessagePayloadSize);
					int splitAt = 1;
					while (true)
					{
						final TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
						codec.readPayload = true;

						// 1st
						codec.readBuffer.put(PVAConstants.PVA_MAGIC);
						codec.readBuffer.put(PVAConstants.PVA_VERSION);
						codec.readBuffer.put((byte)0x90);
						codec.readBuffer.put((byte)0x01);
						final int payloadSize1 = firstMessagePayloadSize;
						codec.readBuffer.putInt(payloadSize1);
						int c = 0;
						for (int i = 0; i < payloadSize1; i++)
							codec.readBuffer.put((byte)(c++));
						int aligned = AbstractCodec.alignedValue(payloadSize1, PVAConstants.PVA_ALIGNMENT);
						for (int i = payloadSize1; i < aligned; i++)
							codec.readBuffer.put((byte)0xFF);

						// 2nd
						codec.readBuffer.put(PVAConstants.PVA_MAGIC);
						codec.readBuffer.put(PVAConstants.PVA_VERSION);
						codec.readBuffer.put((byte)0xB0);
						codec.readBuffer.put((byte)0x01);
						final int payloadSize2 = secondMessagePayloadSize;
						final int payloadSize2Real = payloadSize2 + payloadSize1 % PVAConstants.PVA_ALIGNMENT;
						codec.readBuffer.putInt(payloadSize2Real);

						// pre-message padding
						for (int i = 0; i < payloadSize1 % PVAConstants.PVA_ALIGNMENT; i++)
							codec.readBuffer.put((byte)0xEE);

						for (int i = 0; i < payloadSize2; i++)
							codec.readBuffer.put((byte)(c++));
						aligned = AbstractCodec.alignedValue(payloadSize2Real, PVAConstants.PVA_ALIGNMENT);
						for (int i = payloadSize2Real; i < aligned; i++)
							codec.readBuffer.put((byte)0xFF);

						// 3rd
						codec.readBuffer.put(PVAConstants.PVA_MAGIC);
						codec.readBuffer.put(PVAConstants.PVA_VERSION);
						codec.readBuffer.put((byte)0xA0);
						codec.readBuffer.put((byte)0x01);
						final int payloadSize3 = thirdMessagePayloadSize;
						final int payloadSize3Real = payloadSize3 + payloadSize2Real % PVAConstants.PVA_ALIGNMENT;
						codec.readBuffer.putInt(payloadSize3Real);

						// pre-message padding required
						for (int i = 0; i < payloadSize2Real % PVAConstants.PVA_ALIGNMENT; i++)
							codec.readBuffer.put((byte)0xEE);

						for (int i = 0; i < payloadSize3; i++)
							codec.readBuffer.put((byte)(c++));
						aligned = AbstractCodec.alignedValue(payloadSize3Real, PVAConstants.PVA_ALIGNMENT);
						for (int i = payloadSize3Real; i < aligned; i++)
							codec.readBuffer.put((byte)0xFF);

						//System.out.println("\tlastElement " + (c-1));
						codec.readBuffer.flip();

						final int realReadBufferEnd = codec.readBuffer.limit();
						if (splitAt++ == realReadBufferEnd)
							break;
						codec.readBuffer.limit(splitAt);
						//System.out.println("\t" + splitAt);

						ReadPollOneCallback pollCB = new ReadPollOneCallback() {

							public void readPollOne() throws IOException {

								if (codec.readPollOneCount == 1)
								{
									codec.readBuffer.limit(realReadBufferEnd);
								}
								else
									throw new RuntimeException("should not happen");
							}
						};

						codec.readPollOneCallback = pollCB;


						final int payloadSizeSum = payloadSize1+payloadSize2+payloadSize3;
						codec.forcePayloadRead = payloadSizeSum;

						codec.processRead();
						while (codec.invalidDataStreamCount == 0 && codec.readBuffer.position() != realReadBufferEnd)
						{
							codec.readPollOneCount++;
							pollCB.readPollOne();
							codec.processRead();
						}

						assertEquals(0, codec.invalidDataStreamCount);
						assertEquals(0, codec.closedCount);
						assertEquals(0, codec.receivedControlMessages.size());
						assertEquals(1, codec.receivedAppMessages.size());
						if (splitAt == realReadBufferEnd)
							assertEquals(0, codec.readPollOneCount);
						else
							assertEquals(1, codec.readPollOneCount);

						PVAMessage msg = codec.receivedAppMessages.get(0);
						assertNotNull(msg.payload);
						msg.payload.flip();
						assertEquals(payloadSizeSum, msg.payload.limit());
						for (int i = 0; i < msg.payloadSize; i++)
							assertEquals((byte)i, msg.payload.get());
					}
				}
			}
		}
	}

	public void testStartMessage() throws Throwable
	{
		TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		codec.putControlMessage((byte)0x23, 0x456789AB);

		codec.transferToReadBuffer();

		codec.processRead();

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(1, codec.receivedControlMessages.size());
		assertEquals(0, codec.receivedAppMessages.size());
		PVAMessage header = codec.receivedControlMessages.get(0);
		assertEquals(header.version, PVAConstants.PVA_VERSION);
		assertEquals(header.flags, (byte)0x81);
		assertEquals(header.command, 0x23);
		assertEquals(header.payloadSize, 0x456789AB);

		codec.reset();

		// two at the time, app and control
		codec.setByteOrder(ByteOrder.LITTLE_ENDIAN);
		codec.startMessage((byte)0x20, 0x00000000);
		codec.endMessage();

		codec.setByteOrder(ByteOrder.BIG_ENDIAN);
		codec.putControlMessage((byte)0xEE, 0xDDCCBBAA);

		codec.transferToReadBuffer();

		codec.processRead();

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(1, codec.receivedControlMessages.size());
		assertEquals(1, codec.receivedAppMessages.size());

		// app, no payload
		header = codec.receivedAppMessages.get(0);
		assertEquals(header.version, PVAConstants.PVA_VERSION);
		assertEquals(header.flags, (byte)0x00);
		assertEquals(header.command, (byte)0x20);
		assertEquals(header.payloadSize, 0x00000000);

		// control
		header = codec.receivedControlMessages.get(0);
		assertEquals(header.version, PVAConstants.PVA_VERSION);
		assertEquals(header.flags, (byte)0x81);
		assertEquals(header.command, (byte)0xEE);
		assertEquals(header.payloadSize, 0xDDCCBBAA);
	}

	public void testStartMessageNonEmptyPayload() throws Throwable
	{
		// no misalignment
		TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		codec.readPayload = true;
		codec.startMessage((byte)0x23, 0);
		codec.ensureBuffer(PVAConstants.PVA_ALIGNMENT);
		for (int i = 0; i < PVAConstants.PVA_ALIGNMENT; i++)
			codec.getSendBuffer().put((byte)i);
		codec.endMessage();

		codec.transferToReadBuffer();

		codec.processRead();

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(0, codec.receivedControlMessages.size());
		assertEquals(1, codec.receivedAppMessages.size());

		// app, no payload
		PVAMessage header = codec.receivedAppMessages.get(0);
		assertNotNull(header.payload);
		header.payload.flip();
		assertEquals(PVAConstants.PVA_ALIGNMENT, header.payload.limit());
	}

	public void testStartMessageNormalAlignment() throws Throwable
	{
		TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		codec.readPayload = true;

		codec.startMessage((byte)0x23, 0);
		int payloadSize1 = PVAConstants.PVA_ALIGNMENT+1;
		codec.ensureBuffer(payloadSize1);
		for (int i = 0; i < payloadSize1; i++)
			codec.getSendBuffer().put((byte)i);
		codec.endMessage();

		codec.startMessage((byte)0x45, 0);
		int payloadSize2 = 2*PVAConstants.PVA_ALIGNMENT-1;
		codec.ensureBuffer(payloadSize2);
		for (int i = 0; i < payloadSize2; i++)
			codec.getSendBuffer().put((byte)i);
		codec.endMessage();

		codec.transferToReadBuffer();

		codec.processRead();

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(0, codec.receivedControlMessages.size());
		assertEquals(2, codec.receivedAppMessages.size());

		PVAMessage msg = codec.receivedAppMessages.get(0);
		assertEquals(msg.payloadSize, payloadSize1);
		assertNotNull(msg.payload);
		msg.payload.flip();
		assertEquals(payloadSize1, msg.payload.limit());
		for (int i = 0; i < msg.payloadSize; i++)
			assertEquals((byte)i, msg.payload.get());

		msg = codec.receivedAppMessages.get(1);
		assertEquals(msg.payloadSize, payloadSize2);
		assertNotNull(msg.payload);
		msg.payload.flip();
		assertEquals(payloadSize2, msg.payload.limit());
		for (int i = 0; i < msg.payloadSize; i++)
			assertEquals((byte)i, msg.payload.get());
	}

	public void testStartMessageSegmentedMessage() throws Throwable
	{
		// no misalignment
		final TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		codec.readPayload = true;

		codec.startMessage((byte)0x01, 0);

		int c = 0;

		final int payloadSize1 = PVAConstants.PVA_ALIGNMENT;
		for (int i = 0; i < payloadSize1; i++)
			codec.getSendBuffer().put((byte)(c++));
		codec.flush(false);

		final int payloadSize2 = 2*PVAConstants.PVA_ALIGNMENT;
		for (int i = 0; i < payloadSize2; i++)
			codec.getSendBuffer().put((byte)(c++));
		codec.flush(false);

		final int payloadSize3 = PVAConstants.PVA_ALIGNMENT;
		for (int i = 0; i < payloadSize3; i++)
			codec.getSendBuffer().put((byte)(c++));
		codec.flush(false);

		final int payloadSize4 = 2*PVAConstants.PVA_ALIGNMENT;
		for (int i = 0; i < payloadSize4; i++)
			codec.getSendBuffer().put((byte)(c++));
		codec.endMessage();

		codec.transferToReadBuffer();

		final int payloadSizeSum = payloadSize1+payloadSize2+payloadSize3+payloadSize4;
		codec.forcePayloadRead = payloadSizeSum;

		codec.processRead();

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(0, codec.receivedControlMessages.size());
		assertEquals(1, codec.receivedAppMessages.size());
		assertEquals(0, codec.readPollOneCount);

		PVAMessage msg = codec.receivedAppMessages.get(0);
		assertNotNull(msg.payload);
		msg.payload.flip();
		assertEquals(payloadSizeSum, msg.payload.limit());
		for (int i = 0; i < msg.payloadSize; i++)
			assertEquals((byte)i, msg.payload.get());
	}

	public void testStartMessageSegmentedMessageAlignment() throws Throwable
	{
		for (int firstMessagePayloadSize = 1;	// cannot be zero
			firstMessagePayloadSize <= 3*PVAConstants.PVA_ALIGNMENT;
			firstMessagePayloadSize++)
		{
			for (int secondMessagePayloadSize = 0;
					secondMessagePayloadSize <= 2*PVAConstants.PVA_ALIGNMENT;
					secondMessagePayloadSize++)
			{
				for (int thirdMessagePayloadSize = 1;		// cannot be zero
					thirdMessagePayloadSize <= 2*PVAConstants.PVA_ALIGNMENT;
					thirdMessagePayloadSize++)
				{
					for (int fourthMessagePayloadSize = 1;		// cannot be zero
					fourthMessagePayloadSize <= 2*PVAConstants.PVA_ALIGNMENT;
					fourthMessagePayloadSize++)
					{
						final TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
						codec.readPayload = true;

						codec.startMessage((byte)0x01, 0);

						int c = 0;

						final int payloadSize1 = firstMessagePayloadSize;
						for (int i = 0; i < payloadSize1; i++)
							codec.getSendBuffer().put((byte)(c++));
						codec.flush(false);

						final int payloadSize2 = secondMessagePayloadSize;
						for (int i = 0; i < payloadSize2; i++)
							codec.getSendBuffer().put((byte)(c++));
						codec.flush(false);

						final int payloadSize3 = thirdMessagePayloadSize;
						for (int i = 0; i < payloadSize3; i++)
							codec.getSendBuffer().put((byte)(c++));
						codec.flush(false);

						final int payloadSize4 = fourthMessagePayloadSize;
						for (int i = 0; i < payloadSize4; i++)
							codec.getSendBuffer().put((byte)(c++));
						codec.endMessage();

						codec.transferToReadBuffer();

						final int payloadSizeSum = payloadSize1+payloadSize2+payloadSize3+payloadSize4;
						codec.forcePayloadRead = payloadSizeSum;

						codec.processRead();

						assertEquals(0, codec.invalidDataStreamCount);
						assertEquals(0, codec.closedCount);
						assertEquals(0, codec.receivedControlMessages.size());
						assertEquals(1, codec.receivedAppMessages.size());
						assertEquals(0, codec.readPollOneCount);

						PVAMessage msg = codec.receivedAppMessages.get(0);
						assertNotNull(msg.payload);
						msg.payload.flip();
						assertEquals(payloadSizeSum, msg.payload.limit());
						for (int i = 0; i < msg.payloadSize; i++)
							assertEquals((byte)i, msg.payload.get());
					}
				}
			}
		}
	}

	public void testReadNormalConnectionLoss() throws Throwable
	{
		final TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		codec.readPayload = true;
		codec.disconnected = true;

		codec.readBuffer.put(PVAConstants.PVA_MAGIC);
		codec.readBuffer.put(PVAConstants.PVA_VERSION);
		codec.readBuffer.put((byte)0x01);
		codec.readBuffer.put((byte)0x23);
		codec.readBuffer.putInt(0x456789AB);
		codec.readBuffer.flip();

		codec.processRead();

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(1, codec.closedCount);
		assertEquals(0, codec.receivedControlMessages.size());
		assertEquals(0, codec.receivedAppMessages.size());
	}

	public void testSegmentedSplitConnectionLoss() throws Throwable
	{
		for (int firstMessagePayloadSize = 1;	// cannot be zero
		firstMessagePayloadSize <= 3*PVAConstants.PVA_ALIGNMENT;
		firstMessagePayloadSize++)
		{
			for (int secondMessagePayloadSize = 0;
					secondMessagePayloadSize <= 2*PVAConstants.PVA_ALIGNMENT;
					secondMessagePayloadSize++)
			{
				for (int thirdMessagePayloadSize = 1;		// cannot be zero
					thirdMessagePayloadSize <= 2*PVAConstants.PVA_ALIGNMENT;
					thirdMessagePayloadSize++)
				{
					//System.out.println(firstMessagePayloadSize + " / " + secondMessagePayloadSize + "  / " + thirdMessagePayloadSize);
					int splitAt = 1;
					while (true)
					{
						final TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
						codec.readPayload = true;

						// 1st
						codec.readBuffer.put(PVAConstants.PVA_MAGIC);
						codec.readBuffer.put(PVAConstants.PVA_VERSION);
						codec.readBuffer.put((byte)0x90);
						codec.readBuffer.put((byte)0x01);
						final int payloadSize1 = firstMessagePayloadSize;
						codec.readBuffer.putInt(payloadSize1);
						int c = 0;
						for (int i = 0; i < payloadSize1; i++)
							codec.readBuffer.put((byte)(c++));
						int aligned = AbstractCodec.alignedValue(payloadSize1, PVAConstants.PVA_ALIGNMENT);
						for (int i = payloadSize1; i < aligned; i++)
							codec.readBuffer.put((byte)0xFF);

						// 2nd
						codec.readBuffer.put(PVAConstants.PVA_MAGIC);
						codec.readBuffer.put(PVAConstants.PVA_VERSION);
						codec.readBuffer.put((byte)0xB0);
						codec.readBuffer.put((byte)0x01);
						final int payloadSize2 = secondMessagePayloadSize;
						final int payloadSize2Real = payloadSize2 + payloadSize1 % PVAConstants.PVA_ALIGNMENT;
						codec.readBuffer.putInt(payloadSize2Real);

						// pre-message padding
						for (int i = 0; i < payloadSize1 % PVAConstants.PVA_ALIGNMENT; i++)
							codec.readBuffer.put((byte)0xEE);

						for (int i = 0; i < payloadSize2; i++)
							codec.readBuffer.put((byte)(c++));
						aligned = AbstractCodec.alignedValue(payloadSize2Real, PVAConstants.PVA_ALIGNMENT);
						for (int i = payloadSize2Real; i < aligned; i++)
							codec.readBuffer.put((byte)0xFF);

						// 3rd
						codec.readBuffer.put(PVAConstants.PVA_MAGIC);
						codec.readBuffer.put(PVAConstants.PVA_VERSION);
						codec.readBuffer.put((byte)0xA0);
						codec.readBuffer.put((byte)0x01);
						final int payloadSize3 = thirdMessagePayloadSize;
						final int payloadSize3Real = payloadSize3 + payloadSize2Real % PVAConstants.PVA_ALIGNMENT;
						codec.readBuffer.putInt(payloadSize3Real);

						// pre-message padding required
						for (int i = 0; i < payloadSize2Real % PVAConstants.PVA_ALIGNMENT; i++)
							codec.readBuffer.put((byte)0xEE);

						for (int i = 0; i < payloadSize3; i++)
							codec.readBuffer.put((byte)(c++));
						aligned = AbstractCodec.alignedValue(payloadSize3Real, PVAConstants.PVA_ALIGNMENT);
						for (int i = payloadSize3Real; i < aligned; i++)
							codec.readBuffer.put((byte)0xFF);

						//System.out.println("\tlastElement " + (c-1));
						codec.readBuffer.flip();

						final int realReadBufferEnd = codec.readBuffer.limit();
						if (splitAt++ == realReadBufferEnd-1)
							break;
						codec.readBuffer.limit(splitAt);
						//System.out.println("\t" + splitAt);

						ReadPollOneCallback pollCB = new ReadPollOneCallback() {

							public void readPollOne() throws IOException {

								if (codec.readPollOneCount == 1)
								{
									codec.disconnected = true;
								}
								else
									throw new RuntimeException("should not happen");
							}
						};

						codec.readPollOneCallback = pollCB;


						final int payloadSizeSum = payloadSize1+payloadSize2+payloadSize3;
						codec.forcePayloadRead = payloadSizeSum;

						codec.processRead();
						while (codec.closedCount == 0 && codec.invalidDataStreamCount == 0 && codec.readBuffer.position() != realReadBufferEnd)
						{
							codec.readPollOneCount++;
							pollCB.readPollOne();
							codec.processRead();
						}

						assertEquals(1, codec.closedCount);
						assertEquals(0, codec.invalidDataStreamCount);
						/*
						assertEquals(0, codec.receivedControlMessages.size());
						boolean splitAtLastMessagePadding = splitAt != realReadBufferEnd &&
															AbstractCodec.alignedValue(splitAt, PVAConstants.PVA_ALIGNMENT) ==
															AbstractCodec.alignedValue(realReadBufferEnd, PVAConstants.PVA_ALIGNMENT);
						assertEquals(splitAtLastMessagePadding ? 1 : 0, codec.receivedAppMessages.size());
						*/
					}
				}
			}
		}
	}

	public void testSendConnectionLoss() throws Throwable
	{
		final TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		codec.readPayload = true;
		codec.disconnected = true;

		codec.putControlMessage((byte)0x23, 0x456789AB);

		try
		{
			codec.transferToReadBuffer();
			fail("connection lost, but not reported");
		}
		catch (ConnectionClosedException cce) {
			// expected
		}

		assertEquals(1, codec.closedCount);
	}

	public void testEnqueueSendRequest() throws Throwable
	{
		final TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);

		TransportSender sender = new TransportSender() {

			public void unlock() {
			}

			public void lock() {
			}

			public void send(ByteBuffer buffer, TransportSendControl control) {
				codec.startMessage((byte)0x20, 0x00000000);
				codec.endMessage();
			}
		};

		TransportSender sender2 = new TransportSender() {

			public void unlock() {
			}

			public void lock() {
			}

			public void send(ByteBuffer buffer, TransportSendControl control) {
				codec.putControlMessage((byte)0xEE, 0xDDCCBBAA);
			}
		};

		// process
		codec.enqueueSendRequest(sender);
		codec.enqueueSendRequest(sender2);
		codec.processSendQueue();

		codec.transferToReadBuffer();

		codec.processRead();

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(1, codec.receivedControlMessages.size());
		assertEquals(1, codec.receivedAppMessages.size());

		// app, no payload
		PVAMessage header = codec.receivedAppMessages.get(0);
		assertEquals(header.version, PVAConstants.PVA_VERSION);
		assertEquals(header.flags, (byte)0x80);
		assertEquals(header.command, (byte)0x20);
		assertEquals(header.payloadSize, 0x00000000);

		// control
		header = codec.receivedControlMessages.get(0);
		assertEquals(header.version, PVAConstants.PVA_VERSION);
		assertEquals(header.flags, (byte)0x81);
		assertEquals(header.command, (byte)0xEE);
		assertEquals(header.payloadSize, 0xDDCCBBAA);
	}

	public void testEnqueueSendDirectRequest() throws Throwable
	{
		final TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);

		TransportSender sender = new TransportSender() {

			public void unlock() {
			}

			public void lock() {
			}

			public void send(ByteBuffer buffer, TransportSendControl control) {
				codec.startMessage((byte)0x20, 0x00000000);
				codec.endMessage();
			}
		};

		TransportSender sender2 = new TransportSender() {

			public void unlock() {
			}

			public void lock() {
			}

			public void send(ByteBuffer buffer, TransportSendControl control) {
				codec.putControlMessage((byte)0xEE, 0xDDCCBBAA);
			}
		};

		// thread not right
		codec.enqueueSendRequest(sender, PVAConstants.PVA_MESSAGE_HEADER_SIZE);
		assertEquals(1, codec.scheduleSendCount);
		assertEquals(0, codec.receivedControlMessages.size());
		assertEquals(0, codec.receivedAppMessages.size());

		codec.setSenderThread();
		// not empty queue
		codec.enqueueSendRequest(sender2, PVAConstants.PVA_MESSAGE_HEADER_SIZE);
		assertEquals(2, codec.scheduleSendCount);
		assertEquals(0, codec.receivedControlMessages.size());
		assertEquals(0, codec.receivedAppMessages.size());

		// send will be triggered after last TransportSender was processed
		assertEquals(0, codec.sendCompletedCount);
		codec.processSendQueue();
		assertEquals(1, codec.sendCompletedCount);

		codec.transferToReadBuffer();

		codec.processRead();

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(1, codec.receivedControlMessages.size());
		assertEquals(1, codec.receivedAppMessages.size());

		// app, no payload
		PVAMessage header = codec.receivedAppMessages.get(0);
		assertEquals(header.version, PVAConstants.PVA_VERSION);
		assertEquals(header.flags, (byte)0x80);
		assertEquals(header.command, (byte)0x20);
		assertEquals(header.payloadSize, 0x00000000);

		// control
		header = codec.receivedControlMessages.get(0);
		assertEquals(header.version, PVAConstants.PVA_VERSION);
		assertEquals(header.flags, (byte)0x81);
		assertEquals(header.command, (byte)0xEE);
		assertEquals(header.payloadSize, 0xDDCCBBAA);

		assertEquals(0, codec.getSendBuffer().position());

		// now queue is empty and thread is right
		codec.enqueueSendRequest(sender2, PVAConstants.PVA_MESSAGE_HEADER_SIZE);
		assertEquals(PVAConstants.PVA_MESSAGE_HEADER_SIZE, codec.getSendBuffer().position());
		assertEquals(3, codec.scheduleSendCount);
		assertEquals(1, codec.sendCompletedCount);
		codec.processWrite();
		assertEquals(2, codec.sendCompletedCount);	// since not done via schedule
		codec.transferToReadBuffer();
		codec.processRead();
		assertEquals(2, codec.receivedControlMessages.size());
		assertEquals(1, codec.receivedAppMessages.size());

		header = codec.receivedControlMessages.get(1);
		assertEquals(header.version, PVAConstants.PVA_VERSION);
		assertEquals(header.flags, (byte)0x81);
		assertEquals(header.command, (byte)0xEE);
		assertEquals(header.payloadSize, 0xDDCCBBAA);
	}

	public void testSendPerPartes() throws Throwable
	{
		final int bytesToSent = DEFAULT_BUFFER_SIZE - 2*PVAConstants.PVA_MESSAGE_HEADER_SIZE;
		final TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		codec.readPayload = true;

		TransportSender sender = new TransportSender() {

			public void unlock() {
			}

			public void lock() {
			}

			public void send(ByteBuffer buffer, TransportSendControl control) {
				codec.startMessage((byte)0x12, bytesToSent);
				for (int i = 0; i < bytesToSent; i++)
					codec.getSendBuffer().put((byte)i);
				codec.endMessage();
			}
		};

		// process
		codec.enqueueSendRequest(sender);
		codec.processSendQueue();

		codec.transferToReadBuffer();

		codec.processRead();

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(0, codec.receivedControlMessages.size());
		assertEquals(1, codec.receivedAppMessages.size());

		// app
		PVAMessage header = codec.receivedAppMessages.get(0);
		assertEquals(header.version, PVAConstants.PVA_VERSION);
		assertEquals(header.flags, (byte)0x80);
		assertEquals(header.command, (byte)0x12);
		assertEquals(header.payloadSize, bytesToSent);
		assertNotNull(header.payload);
		header.payload.flip();
		assertEquals(bytesToSent, header.payload.limit());
		for (int i = 0; i < header.payloadSize; i++)
			assertEquals((byte)i, header.payload.get());

	}

	public void testSendException() throws Throwable
	{
		final TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		codec.throwExceptionOnSend = true;

		TransportSender sender = new TransportSender() {

			public void unlock() {
			}

			public void lock() {
			}

			public void send(ByteBuffer buffer, TransportSendControl control) {
				codec.putControlMessage((byte)0x01, 0x00112233);
			}
		};

		// process
		codec.enqueueSendRequest(sender);

		try
		{
			codec.processSendQueue();
			fail("ConnectionClosedException expected");
		} catch (ConnectionClosedException cce) {
			// OK
		}

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(1, codec.closedCount);
		assertEquals(0, codec.receivedControlMessages.size());
		assertEquals(0, codec.receivedAppMessages.size());
	}

	public void testSendHugeMessagePartes() throws Throwable
	{
		final int bytesToSent = 10*DEFAULT_BUFFER_SIZE+1;
		final TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		codec.readPayload = true;
		codec.readBuffer = ByteBuffer.allocate(11*DEFAULT_BUFFER_SIZE);

		WritePollOneCallback wpollCB = new WritePollOneCallback() {
			public void writePollOne() throws IOException {
				codec.processWrite();	// this should return immediately

				// now we fake reading
				codec.writeBuffer.flip();

				// in this test we made sure readBuffer is big enough
				//System.out.println(codec.readBuffer.position() + "/" + codec.readBuffer.limit());
				//System.out.println("after: " + (codec.readBuffer.position()+codec.writeBuffer.remaining()));
				codec.readBuffer.put(codec.writeBuffer);

				codec.writeBuffer.clear();
			}
		};
		codec.writePollOneCallback = wpollCB;

		TransportSender sender = new TransportSender() {

			public void unlock() {
			}

			public void lock() {
			}

			public void send(ByteBuffer buffer, TransportSendControl control) {
				codec.startMessage((byte)0x12, 0);
				int toSend = bytesToSent;
				int c = 0;
				while (toSend > 0)
				{
					int sendNow = Math.min(toSend, AbstractCodec.MAX_ENSURE_BUFFER_SIZE);
					codec.ensureBuffer(sendNow);
					for (int i = 0; i < sendNow; i++)
						codec.getSendBuffer().put((byte)(c++));
					toSend -= sendNow;
				}
				codec.endMessage();
			}
		};

		// process
		codec.enqueueSendRequest(sender);
		codec.processSendQueue();

		codec.addToReadBuffer();

		codec.forcePayloadRead = bytesToSent;
		codec.processRead();

		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(0, codec.closedCount);
		assertEquals(0, codec.receivedControlMessages.size());
		assertEquals(1, codec.receivedAppMessages.size());

		// app
		PVAMessage header = codec.receivedAppMessages.get(0);
		assertEquals(header.version, PVAConstants.PVA_VERSION);
		assertEquals(header.flags, (byte)(0x80 | 0x10));	// segmented
		assertEquals(header.command, (byte)0x12);
		//assertEquals(header.payloadSize, bytesToSent);	payloadSize contains only first message payload size
		assertNotNull(header.payload);
		header.payload.flip();
		assertEquals(bytesToSent, header.payload.limit());
		for (int i = 0; i < header.payloadSize; i++)
			assertEquals((byte)i, header.payload.get());

	}

	public void testRecipient() throws Throwable
	{
		final TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		InetSocketAddress addr = new InetSocketAddress(InetAddress.getLocalHost(), 1234);
		codec.setRecipient(addr);

		// nothing to test, depends on implementation
	}

	public void testClearSendQueue() throws Throwable
	{
		final TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);

		TransportSender sender = new TransportSender() {

			public void unlock() {
			}

			public void lock() {
			}

			public void send(ByteBuffer buffer, TransportSendControl control) {
				codec.startMessage((byte)0x20, 0x00000000);
				codec.endMessage();
			}
		};

		TransportSender sender2 = new TransportSender() {

			public void unlock() {
			}

			public void lock() {
			}

			public void send(ByteBuffer buffer, TransportSendControl control) {
				codec.putControlMessage((byte)0xEE, 0xDDCCBBAA);
			}
		};

		codec.enqueueSendRequest(sender);
		codec.enqueueSendRequest(sender2);

		codec.clearSendQueue();

		codec.processSendQueue();

		assertEquals(0, codec.getSendBuffer().position());
		assertEquals(0, codec.writeBuffer.position());
	}

	// used to suppress "if (PVAConstants.PVA_ALIGNMENT > 1)" dead code
	@SuppressWarnings("unused")
	public void testInvalidArguments() throws Throwable
	{
		try
		{
			// too small
			new TestCodec(1, DEFAULT_BUFFER_SIZE);
			fail("too small buffer accepted");
		} catch (IllegalArgumentException iae) {
			// OK
		}

		try
		{
			// too small
			new TestCodec(DEFAULT_BUFFER_SIZE, 1);
			fail("too small buffer accepted");
		} catch (IllegalArgumentException iae) {
			// OK
		}

		if (PVAConstants.PVA_ALIGNMENT > 1)
		{
			try
			{
				// non aligned
				new TestCodec(2*AbstractCodec.MAX_ENSURE_SIZE+1, DEFAULT_BUFFER_SIZE);
				fail("non-aligned buffer size accepted");
			} catch (IllegalArgumentException iae) {
				// OK
			}

			try
			{
				// non aligned
				new TestCodec(DEFAULT_BUFFER_SIZE, 2*AbstractCodec.MAX_ENSURE_SIZE+1);
				fail("non-aligned buffer size accepted");
			} catch (IllegalArgumentException iae) {
				// OK
			}
		}

		TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);

		try
		{
			codec.ensureBuffer(DEFAULT_BUFFER_SIZE+1);
			fail("too big size accepted");
		} catch (IllegalArgumentException iae) {
			// OK
		}

		try
		{
			codec.ensureData(AbstractCodec.MAX_ENSURE_DATA_SIZE+1);
			fail("too big size accepted");
		} catch (IllegalArgumentException iae) {
			// OK
		}
	}

	public void testDefaultModes() throws Throwable
	{
		TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);
		assertEquals(ReadMode.NORMAL, codec.getReadMode());
		assertEquals(ReadMode.NORMAL, ReadMode.valueOf("NORMAL"));
		assertEquals(WriteMode.PROCESS_SEND_QUEUE, codec.getWriteMode());
		assertEquals(WriteMode.PROCESS_SEND_QUEUE, WriteMode.valueOf("PROCESS_SEND_QUEUE"));
	}

	public void testEnqueueSendRequestExceptionThrown() throws Throwable
	{
		final TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE);

		TransportSender sender = new TransportSender() {

			public void unlock() {
			}

			public void lock() {
			}

			public void send(ByteBuffer buffer, TransportSendControl control) {
				throw new RuntimeException("expected test exception");
			}
		};

		TransportSender sender2 = new TransportSender() {

			public void unlock() {
			}

			public void lock() {
			}

			public void send(ByteBuffer buffer, TransportSendControl control) {
				codec.putControlMessage((byte)0xEE, 0xDDCCBBAA);
			}
		};

		// process
		codec.enqueueSendRequest(sender);
		codec.enqueueSendRequest(sender2);

		try
		{
			codec.processSendQueue();
			fail("ConnectionClosedException expected");
		} catch (ConnectionClosedException cce) {
			// OK
		}

		codec.transferToReadBuffer();

		codec.processRead();


		assertEquals(0, codec.invalidDataStreamCount);
		assertEquals(1, codec.closedCount);
		assertEquals(0, codec.receivedControlMessages.size());
		assertEquals(0, codec.receivedAppMessages.size());
		/*
		// control must go through
		PVAMessage header = codec.receivedControlMessages.get(0);
		assertEquals(header.version, PVAConstants.PVA_VERSION);
		assertEquals(header.flags, (byte)0x81);
		assertEquals(header.command, (byte)0xEE);
		assertEquals(header.payloadSize, 0xDDCCBBAA);
		*/
	}

	public void testBlockingProcessQueueTest() throws Throwable
	{
		final TestCodec codec = new TestCodec(DEFAULT_BUFFER_SIZE, DEFAULT_BUFFER_SIZE, true);
		final AtomicBoolean processTreadExited = new AtomicBoolean(false);

		Thread processThread = new Thread(
				new Runnable() {

					public void run() {
						try {
							// this should block
							codec.processSendQueue();
						}
						catch (Throwable th) {
							th.printStackTrace();
						}
						finally {
							processTreadExited.set(true);
						}
					}
				}, "processSendQueue");
		processThread.start();

		Thread.sleep(3000);

		assertEquals(false, processTreadExited.get());

		// let's put something into it
		TransportSender sender = new TransportSender() {

			public void unlock() {
			}

			public void lock() {
			}

			public void send(ByteBuffer buffer, TransportSendControl control) {
				codec.putControlMessage((byte)0x01, 0x00112233);
			}
		};
		codec.enqueueSendRequest(sender);

		processThread.interrupt();

		Thread.sleep(3000);

		assertEquals(PVAConstants.PVA_MESSAGE_HEADER_SIZE, codec.writeBuffer.position());
		assertEquals(true, processTreadExited.get());

	}
}
