package org.epics.pvaccess.impl.remote.codec.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.epics.pvaccess.impl.remote.codec.AbstractCodec;

// TODO send thread is not terminated!!!

// NOTE: supports 2 threads per connection (receive and send)
public abstract class BlockingAbstractCodec extends AbstractCodec {

	private final AtomicBoolean isOpen = new AtomicBoolean(true);

	public BlockingAbstractCodec(ByteBuffer receiveBuffer, ByteBuffer sendBuffer,
			int socketSendBufferSize, Logger logger) {
		super(receiveBuffer, sendBuffer, socketSendBufferSize, true, logger);
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
		// TODO wakeup
	}

	@Override
	public void sendCompleted() {
		// noop
	}
	
	
	
	/* (non-Javadoc)
	 * @see java.nio.channels.Channel#close()
	 */
	@Override
	public void close() throws IOException {
		if (isOpen.getAndSet(false))
		{
			// always close in the same thread, same way, etc.
			
			// wakeup processSendQueue
			sendQueue.wakeup();
		}
	}

	/* (non-Javadoc)
	 * @see java.nio.channels.Channel#isOpen()
	 */
	@Override
	public boolean isOpen() {
		return isOpen.get();
	}

	private volatile Thread readThread = null;
	
	public void start()
	{
		readThread = new Thread(new Runnable() {
			@Override
			public void run() {
				receiveThread();
			}
		}, "receiveThread");
		readThread.start();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				sendThread();
			}
		}, "sendThread").start();
		
	}
	
	public void receiveThread()
	{
		while (isOpen())
		{
			try {
				processRead();
			} catch (IOException e) {
				// TODO
				// TODO Auto-generated catch block
				e.printStackTrace();
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
			} catch (IOException e) {
				// TODO
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// wait read thread to die
		try {
			readThread.join();		// TODO timeout
		} catch (InterruptedException e) {
			// noop
		}	
		
		// call internal destroy
		internalDestroy();
	}
	
	abstract void internalDestroy();
	
}

