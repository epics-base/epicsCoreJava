/*
 *
 */
package org.epics.pvaccess.impl.remote.codec.test.perf;


import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.impl.remote.codec.AbstractCodec;

import com.sun.japex.Constants;
import com.sun.japex.JapexDriverBase;
import com.sun.japex.TestCase;

/**
 * @author msekoranja
 *
 */
public class MessageProcessPerformance extends JapexDriverBase {

	private static int MAX_MESSAGES_IN_BUFFER = 1000;
	private static int DEFAULT_BUFFER_SIZE = (PVAConstants.PVA_MESSAGE_HEADER_SIZE+64)*MAX_MESSAGES_IN_BUFFER+AbstractCodec.MAX_ENSURE_SIZE;
	static int MAX_PAYLOAD_SIZE = DEFAULT_BUFFER_SIZE/MAX_MESSAGES_IN_BUFFER-PVAConstants.PVA_MESSAGE_HEADER_SIZE;

	private TestCodec codec;

	/* (non-Javadoc)
	 * @see com.sun.japex.JapexDriverBase#initializeDriver()
	 */
	@Override
	public void initializeDriver() {
		super.initializeDriver();
		try
		{
			codec = new TestCodec(DEFAULT_BUFFER_SIZE);
			codec.readPayload = true;
		}
		catch (Throwable th)
		{
			throw new RuntimeException(th);
		}
	}

	private int bufferLimit;

	/* (non-Javadoc)
	 * @see com.sun.japex.JapexDriverBase#prepare(com.sun.japex.TestCase)
	 */
	@Override
	public void prepare(TestCase testCase) {
		int messagesInBuffer = testCase.getIntParam("messagesInBuffer");
		if (messagesInBuffer > MAX_MESSAGES_IN_BUFFER)
			throw new IllegalArgumentException("messagesInBuffer > MAX_MESSAGES_IN_BUFFER");

		boolean applicationMessage = testCase.getBooleanParam("applicationMessage");
		int payloadSize = testCase.getIntParam("payloadSize");
		if (payloadSize > MAX_PAYLOAD_SIZE)
			throw new IllegalArgumentException("payloadSize > MAX_PAYLOAD_SIZE");

		int alignedPayloadSize = AbstractCodec.alignedValue(payloadSize, PVAConstants.PVA_ALIGNMENT);
		bufferLimit = messagesInBuffer * (PVAConstants.PVA_MESSAGE_HEADER_SIZE+alignedPayloadSize);

		codec.reset();
		byte flags = applicationMessage ? (byte)0x80 : (byte)0x81;
		for (int i = 0; i < messagesInBuffer; i++)
		{
			codec.readBuffer.put(PVAConstants.PVA_MAGIC);
			codec.readBuffer.put(PVAConstants.PVA_VERSION);
			codec.readBuffer.put(flags);
			codec.readBuffer.put((byte)0x23);
			codec.readBuffer.putInt(payloadSize);
			int c = 0;
			for (; c < payloadSize; c++)
				codec.readBuffer.put((byte)c);
			for (; c < alignedPayloadSize; c++)
				codec.readBuffer.put((byte)0xFF);
		}

	}

	/* (non-Javadoc)
	 * @see com.sun.japex.JapexDriverBase#finish(com.sun.japex.TestCase)
	 */
	@Override
	public void finish(TestCase tc) {
		// set real count (e.g. for bulk)

	    // Get actual run time
	    double actualTime = tc.getDoubleParam(Constants.ACTUAL_RUN_TIME);

	    // TODO this works only for one thread and tps

	    // Tx = sum(I_k) / T for k in 1..N
	    double tps = codec.messagesProcessed /
	          (actualTime / 1000.0);

	    tc.setDoubleParam(Constants.RESULT_VALUE, tps);
	}


	/* (non-Javadoc)
	 * @see com.sun.japex.JapexDriverBase#run()
	 */
	@Override
	public void run() {
		// we need to reset this before calling run...
		codec.messagesProcessed = 0;
		super.run();
	}


	/* (non-Javadoc)
	 * @see com.sun.japex.JapexDriverBase#run(com.sun.japex.TestCase)
	 */
	@Override
	public void run(TestCase testCase) {
		try
		{
			codec.readBuffer.position(0);
			codec.readBuffer.limit(bufferLimit);

			while (codec.invalidDataStreamCount == 0 && codec.readBuffer.hasRemaining())
				codec.processRead();

		}
		catch (Throwable th)
		{
			throw new RuntimeException(th);
		}
	}

}
