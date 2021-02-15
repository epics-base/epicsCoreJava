/*
 *
 */
package org.epics.pvaccess.impl.remote.io.impl.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.TransportSender;
import org.epics.pvaccess.impl.remote.codec.AbstractCodec;
import org.epics.pvaccess.impl.remote.io.PollEvents;
import org.epics.pvaccess.impl.remote.io.Poller;
import org.epics.pvaccess.impl.remote.io.impl.PollerImpl;
import org.epics.pvdata.pv.Field;

/**
 * @author msekoranja
 *
 */
public class LatencyTest extends AbstractCodec implements PollEvents {

	interface ReadyListener {
		void ready(AbstractCodec codec);
	}

	final Poller poller;
	final SocketChannel channel;
	SelectionKey key;
	final ReadyListener readyListener;
	final InetSocketAddress socketAddress;

	public LatencyTest(Poller poller, SocketChannel channel, ReadyListener readyListener) throws IOException
	{
		super(true, ByteBuffer.allocateDirect(10240), ByteBuffer.allocateDirect(10240), channel.socket().getSendBufferSize(), false, Logger.getLogger("TestAC"));
		// initialize socketBuffer
		this.poller = poller;
		this.channel = channel;
		this.readyListener = readyListener;
		this.socketAddress = (InetSocketAddress)channel.socket().getRemoteSocketAddress();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.jam.io.PollEvents#registeredNotify(java.nio.channels.SelectionKey, java.lang.Throwable)
	 */
	public void registeredNotify(SelectionKey key,
			Throwable registrationException) {
		setSenderThread();
		this.key = key;
		if (readyListener != null)
			readyListener.ready(this);
	}

	/* (non-Javadoc)
	 * @see com.cosylab.jam.io.PollEvents#pollNotify(java.nio.channels.SelectionKey)
	 */
	public void pollNotify(SelectionKey key) throws IOException {
		if (key.isReadable())
			processRead();
		if (key.isWritable())
			processWrite();
		if (key.isConnectable()) {
			channel.finishConnect();
			key.interestOps(SelectionKey.OP_READ);
		}
	}

	public int read(ByteBuffer dst) throws IOException {
		return channel.read(dst);
	}

	/* (non-Javadoc)
	 * @see com.cosylab.jam.io.AbstractCodec#invalidDataStreamHandler()
	 */
	@Override
	public void invalidDataStreamHandler() {
		// for TCP we close
		try {
			close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.cosylab.jam.io.AbstractCodec#getLastReadBufferSocketAddress()
	 */
	@Override
	public InetSocketAddress getLastReadBufferSocketAddress() {
		return socketAddress;
	}

	public void close() throws IOException {
		channel.close();
	}

	public boolean isOpen() {
		return channel.isOpen();
	}

	public int write(ByteBuffer src) throws IOException {
		return channel.write(src);
	}

	final TransportSender sender = new TransportSender(){

		/* (non-Javadoc)
		 * @see org.epics.pvaccess.client.Lockable#lock()
		 */
		public void lock() {
			// TODO Auto-generated method stub

		}

		/* (non-Javadoc)
		 * @see org.epics.pvaccess.client.Lockable#unlock()
		 */
		public void unlock() {
			// TODO Auto-generated method stub

		}

		int c = 0;
		long avg = 0;

		long lastTime = 0;
		/* (non-Javadoc)
		 * @see org.epics.pvaccess.impl.remote.TransportSender#send(java.nio.ByteBuffer, org.epics.pvaccess.impl.remote.TransportSendControl)
		 */
		public void send(ByteBuffer buffer, TransportSendControl control) {
			long t = System.nanoTime();
			avg += t-lastTime;
			int BIN = 100000;
			if (c++ % BIN == 0)
			{
				System.out.println(avg/(double)BIN);
				avg = 0;
			}
			lastTime = t;
			control.ensureBuffer(PVAConstants.PVA_MESSAGE_HEADER_SIZE);
			buffer.put(PVAConstants.PVA_MAGIC);
			buffer.put(PVAConstants.PVA_VERSION);
			buffer.put((byte)0x81);
			buffer.put((byte)0);
			buffer.putInt(0);
		}

	};

	@Override
	public void processControlMessage() {
		//System.out.println("processControlMessage():" + command);
		enqueueSendRequest(sender, PVAConstants.PVA_MESSAGE_HEADER_SIZE);
	}

	@Override
	public void processApplicationMessage() throws IOException {
		System.out.println("processApplicationMessage():" + command);
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
		key.interestOps(SelectionKey.OP_WRITE);	// TODO allow read?
	}

	@Override
	public void sendCompleted() {
		key.interestOps(SelectionKey.OP_READ);
	}

	@Override
	public boolean terminated() {
		return false;
	}

	public void cachedSerialize(Field field, ByteBuffer buffer) {
		// no cache
		field.serialize(buffer, this);
	}


	static class Acceptor implements PollEvents, TransportSender, ReadyListener
	{
		final Poller poller;
		final ServerSocketChannel serverSocket;

		public Acceptor(Poller poller, ServerSocketChannel serverSocket)
		{
			this.poller = poller;
			this.serverSocket = serverSocket;
		}

		public void pollNotify(SelectionKey key) {
			System.out.println(key.readyOps());
			try {
				SocketChannel socketChannel = serverSocket.accept();
				if (socketChannel != null)
				{
					socketChannel.configureBlocking(false);
					LatencyTest codec = new LatencyTest(poller, socketChannel, this);
					poller.add(socketChannel, codec, SelectionKey.OP_READ);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void registeredNotify(SelectionKey key,
				Throwable registrationException) {
			// noop
		}

		/**
		 * PVA connection validation request.
		 * A server sends a validate connection message when it receives a new connection.
		 * The message indicates that the server is ready to receive requests; the client must
		 * not send any messages on the connection until it has received the validate connection message
		 * from the server. No reply to the message is expected by the server.
		 * The purpose of the validate connection message is two-fold:
		 * It informs the client of the protocol version supported by the server.
		 * It prevents the client from writing a request message to its local transport
		 * buffers until after the server has acknowledged that it can actually process the
		 * request. This avoids a race condition caused by the server's TCP/IP stack
		 * accepting connections in its backlog while the server is in the process of shutting down:
		 * if the client were to send a request in this situation, the request
		 * would be lost but the client could not safely re-issue the request because that
		 * might violate at-most-once semantics.
		 * The validate connection message guarantees that a server is not in the middle
		 * of shutting down when the server's TCP/IP stack accepts an incoming connection
		 * and so avoids the race condition.
		 * @see org.epics.pvaccess.impl.remote.TransportSender#send(java.nio.ByteBuffer, org.epics.pvaccess.impl.remote.TransportSendControl)
		 */
		public void send(ByteBuffer buffer, TransportSendControl control) {

			control.ensureBuffer(PVAConstants.PVA_MESSAGE_HEADER_SIZE);
			buffer.put(PVAConstants.PVA_MAGIC);
			buffer.put(PVAConstants.PVA_VERSION);
			buffer.put((byte)0x81);
			buffer.put((byte)0);
			buffer.putInt(0);

			// send immediately
			control.flush(true);
		}

		/* (non-Javadoc)
		 * @see org.epics.pvaccess.client.Lockable#lock()
		 */
		public void lock() {
		}

		/* (non-Javadoc)
		 * @see org.epics.pvaccess.client.Lockable#unlock()
		 */
		public void unlock() {
		}

		public void ready(AbstractCodec codec) {
			codec.setSenderThread();
			codec.enqueueSendRequest(this, PVAConstants.PVA_MESSAGE_HEADER_SIZE);
		}

	}

	public static void main(String[] args) throws Throwable {

		final PollerImpl poller = new PollerImpl();
		poller.start();

		ServerSocketChannel serverSocket = ServerSocketChannel.open();
		serverSocket.socket().bind(new InetSocketAddress(1234));
		serverSocket.configureBlocking(false);
		poller.add(serverSocket, new Acceptor(poller, serverSocket), SelectionKey.OP_ACCEPT);


		SocketChannel clientSocket = SocketChannel.open();
		clientSocket.configureBlocking(false);
		poller.add(clientSocket, new LatencyTest(poller, clientSocket, null), SelectionKey.OP_CONNECT);
		clientSocket.connect(new InetSocketAddress("192.168.1.102", 1234));

	}

}
