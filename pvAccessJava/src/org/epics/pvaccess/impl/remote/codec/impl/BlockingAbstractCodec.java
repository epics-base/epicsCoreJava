package org.epics.pvaccess.impl.remote.codec.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.impl.remote.codec.AbstractCodec;
import org.epics.pvaccess.impl.remote.codec.ConnectionClosedException;

// TODO check if send thread is terminated

// NOTE: supports 2 threads per connection (receive and send)
public abstract class BlockingAbstractCodec extends AbstractCodec {

	// TODO not via config
	private final boolean debug = Integer.getInteger(PVAConstants.PVACCESS_DEBUG, 0) >= 3;

	private final AtomicBoolean isOpen = new AtomicBoolean(true);

	public BlockingAbstractCodec(boolean serverFlag, ByteBuffer receiveBuffer, ByteBuffer sendBuffer,
			int socketSendBufferSize, Logger logger) {
		super(serverFlag, receiveBuffer, sendBuffer, socketSendBufferSize, true, logger);
	}

	@Override
	public void readPollOne() throws IOException {
		throw new IllegalStateException("should not be called for blocking IO");
	}

	@Override
	public void writePollOne() throws IOException {
		throw new IllegalStateException("should not be called for blocking IO");
	}

	@Override
	protected void sendBufferFull(int tries) throws IOException {
		// TODO constants
		try {
			Thread.sleep(Math.max(tries * 100, 1000));
		} catch (InterruptedException e) {
			// noop
		}
	}

	@Override
	public void scheduleSend() {
		// noop since we wait in processSendQueue
	}

	@Override
	public void sendCompleted() {
		// noop
	}



	/* (non-Javadoc)
	 * @see java.nio.channels.Channel#close()
	 */
	public void close() throws IOException {
		if (isOpen.getAndSet(false))
		{
			// always close in the same thread, same way, etc.

			// unblock read
			readThread.interrupt();

			// wakeup processSendQueue
			sendQueue.wakeup();
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.codec.AbstractCodec#terminated()
	 */
	@Override
	public boolean terminated() {
		return !isOpen();
	}

	/* (non-Javadoc)
	 * @see java.nio.channels.Channel#isOpen()
	 */
	public boolean isOpen() {
		return isOpen.get();
	}

	private volatile Thread readThread = null;
	private volatile Thread sendThread = null;

	public void start()
	{
		readThread = new Thread(new Runnable() {
			public void run() {
				receiveThread();
			}
		}, "receiveThread");
		readThread.start();

		sendThread = new Thread(new Runnable() {
			public void run() {
				sendThread();
			}
		}, "sendThread");
		sendThread.start();

	}

	public void receiveThread()
	{
		while (isOpen())
		{
			try {
				processRead();
			} catch (ConnectionClosedException cce) {
				// noop
			} catch (IOException e) {

				if (debug)
					logger.log(Level.FINER, "IO exception caught in read thread.", e);
			}
		}
	}

	public void sendThread()
	{
		setSenderThread();
		while (isOpen())
		{
			try {
				processWrite();
			} catch (ConnectionClosedException cce) {
				// noop
			} catch (IOException e) {

				if (debug)
					logger.log(Level.FINER, "IO exception caught in send thread.", e);

			}
		}

		// wait read thread to die
		try {
			readThread.join(3000);
		} catch (InterruptedException e) {
			// noop
		}

		// call internal destroy
		internalDestroy();
	}

	abstract void internalDestroy();

}

