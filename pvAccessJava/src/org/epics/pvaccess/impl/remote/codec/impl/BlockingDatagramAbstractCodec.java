/*
 *
 */
package org.epics.pvaccess.impl.remote.codec.impl;

import java.nio.channels.DatagramChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * @author msekoranja
 *
 */
public abstract class BlockingDatagramAbstractCodec extends BlockingAbstractCodec {

	private final DatagramChannel channel;
	private final InetSocketAddress socketAddress;

	public BlockingDatagramAbstractCodec(
			boolean serverFlag,
			DatagramChannel channel,
			ByteBuffer receiveBuffer,
			ByteBuffer sendBuffer,
			Logger logger) throws SocketException {
		super(serverFlag, receiveBuffer, sendBuffer, channel.socket().getSendBufferSize(), logger);
		this.channel = channel;
		if (!channel.socket().isConnected())
			throw new IllegalArgumentException("only connected datagram sockets are allowed");
		this.socketAddress = (InetSocketAddress)channel.socket().getRemoteSocketAddress();
	}

	public int read(ByteBuffer dst) throws IOException {
		return channel.read(dst);
	}

	public int write(ByteBuffer src) throws IOException {
		return channel.write(src);
	}

	@Override
	void internalDestroy() {
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
		// reset, be ready for new packet
		socketBuffer.clear();
		readMode = ReadMode.NORMAL;
	}


}
