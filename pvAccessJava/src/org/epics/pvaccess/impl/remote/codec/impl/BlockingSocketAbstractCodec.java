/*
 *
 */
package org.epics.pvaccess.impl.remote.codec.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

/*
 * @author msekoranja
 *
 */
public abstract class BlockingSocketAbstractCodec extends BlockingAbstractCodec {

	protected final SocketChannel channel;
	protected final InetSocketAddress socketAddress;

	public BlockingSocketAbstractCodec(
			boolean serverFlag,
			SocketChannel channel,
			ByteBuffer receiveBuffer,
			ByteBuffer sendBuffer,
			Logger logger) throws SocketException {
		super(serverFlag, receiveBuffer, sendBuffer, channel.socket().getSendBufferSize(), logger);
		this.channel = channel;
		this.socketAddress = (InetSocketAddress)channel.socket().getRemoteSocketAddress();
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
