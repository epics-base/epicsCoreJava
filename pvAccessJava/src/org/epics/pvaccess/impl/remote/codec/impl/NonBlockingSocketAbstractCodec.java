/*
 *
 */
package org.epics.pvaccess.impl.remote.codec.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

import org.epics.pvaccess.impl.remote.io.Poller;

/**
 * @author msekoranja
 *
 */
public abstract class NonBlockingSocketAbstractCodec extends NonBlockingAbstractCodec {

	protected final SocketChannel channel;
	protected final InetSocketAddress socketAddress;

	public NonBlockingSocketAbstractCodec(
			boolean serverFlag,
			Poller poller,
			SocketChannel channel,
			ByteBuffer receiveBuffer,
			ByteBuffer sendBuffer,
			Logger logger) throws SocketException {
		super(serverFlag, poller, receiveBuffer, sendBuffer, channel.socket().getSendBufferSize(), logger);
		this.channel = channel;
		this.socketAddress = (InetSocketAddress)channel.socket().getRemoteSocketAddress();

		// TODO
		try {
			channel.configureBlocking(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		poller.add(channel, this, SelectionKey.OP_READ);
	}

	public int read(ByteBuffer dst) throws IOException {
		return channel.read(dst);
	}

	public int write(ByteBuffer src) throws IOException {
		return channel.write(src);
	}

	@Override
	protected void internalDestroy() {
		if (channel.isOpen())
		{
			try {
				channel.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();	// TODO
			}
		}
	}

	@Override
	public InetSocketAddress getLastReadBufferSocketAddress() {
		return socketAddress;
	}

	@Override
	public void invalidDataStreamHandler() {
		// invalid stream, close TCP connection
		try {
			close();		// TODO
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
