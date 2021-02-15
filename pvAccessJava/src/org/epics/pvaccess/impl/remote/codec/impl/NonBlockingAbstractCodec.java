package org.epics.pvaccess.impl.remote.codec.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.epics.pvaccess.impl.remote.codec.AbstractCodec;
import org.epics.pvaccess.impl.remote.io.PollEvents;
import org.epics.pvaccess.impl.remote.io.Poller;

public abstract class NonBlockingAbstractCodec extends AbstractCodec implements PollEvents {

	private final AtomicBoolean isOpen = new AtomicBoolean(true);

	protected final Poller poller;

	protected volatile SelectionKey key; // TODO sync? no... yes, accessible from outside... make it final?

	public NonBlockingAbstractCodec(boolean serverFlag, Poller poller, ByteBuffer receiveBuffer, ByteBuffer sendBuffer,
			int socketSendBufferSize, Logger logger) {
		super(serverFlag, receiveBuffer, sendBuffer, socketSendBufferSize, false, logger);
		this.poller = poller;
	}

	protected abstract void ready();

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.io.PollEvents#registeredNotify(java.nio.channels.SelectionKey, java.lang.Throwable)
	 */
	public void registeredNotify(SelectionKey key,
			Throwable registrationException) {
		setSenderThread();
		this.key = key;
		ready();
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.io.PollEvents#pollNotify(java.nio.channels.SelectionKey)
	 */
	public void pollNotify(SelectionKey key) throws IOException {
		if (key.isReadable())
			processRead();
		// TODO use a flag, go here immediately... avoid setting WRITE selection key
		if (key.isWritable())	// TODO else?
			processWrite();
	}

	@Override
	public void readPollOne() throws IOException {
		poller.pollOne();
	}

	@Override
	public void writePollOne() throws IOException {
		poller.pollOne();
	}

	@Override
	protected void sendBufferFull(int tries) throws IOException {
		writeOpReady = false;
		writeMode = WriteMode.WAIT_FOR_READY_SIGNAL;
		this.writePollOne();
		writeMode = WriteMode.PROCESS_SEND_QUEUE;
	}

	@Override
	public void scheduleSend() {
		//System.out.println("scheduleSend");
		key.interestOps(SelectionKey.OP_WRITE);	// TODO allow read?
	}

	@Override
	public void sendCompleted() {
		//System.out.println("sendCompleted");
		key.interestOps(SelectionKey.OP_READ);
	}

	/* (non-Javadoc)
	 * @see java.nio.channels.Channel#close()
	 */
	public void close() throws IOException {
		if (isOpen.getAndSet(false))
		{
			// TODO is this OK? yes...
			internalDestroy();
		}
	}

	abstract void internalDestroy();

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

}
