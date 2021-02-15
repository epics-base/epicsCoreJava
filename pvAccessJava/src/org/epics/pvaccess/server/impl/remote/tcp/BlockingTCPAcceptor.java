/*
 * Copyright (c) 2004 by Cosylab
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file "LICENSE-CAJ". If the license is not included visit Cosylab web site,
 * <http://www.cosylab.com>.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package org.epics.pvaccess.server.impl.remote.tcp;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.impl.remote.Context;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;

/**
 * Channel Access Server TCP acceptor.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class BlockingTCPAcceptor {

	/**
	 * Context instance.
	 */
	private Context context;

	/**
	 * Bind server socket address.
	 */
	private InetSocketAddress bindAddress = null;

	/**
	 * Server socket channel.
	 */
	private ServerSocketChannel serverSocketChannel = null;

	/**
	 * Receive buffer size.
	 */
	private int receiveBufferSize;

	/**
	 * Destroyed flag.
	 */
	private AtomicBoolean destroyed = new AtomicBoolean(false);

	//private final PollerImpl poller;

	public BlockingTCPAcceptor(Context context, int port, int receiveBufferSize) throws PVAException {
		this.context = context;
		this.receiveBufferSize = receiveBufferSize;

		/*
		// TODO!!!
		try {
			poller = new PollerImpl();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		poller.start();
		 */
		initialize(port);
	}

	/**
	 * Handle IO events.
	 */
	public void handleEvents() {
		// rise level if port is assigned dynamically
		context.getLogger().finer("Accepting connections at " + bindAddress + ".");

		while (!destroyed.get() && serverSocketChannel.isOpen())
		{
			try
			{
				// this will block
				SocketChannel socket = serverSocketChannel.accept();

				SocketAddress address = socket.socket().getRemoteSocketAddress();
				context.getLogger().finer("Accepted connection from PVA client: " + address);

				// enable TCP_NODELAY (disable Nagle's algorithm)
				socket.socket().setTcpNoDelay(true);

				// enable TCP_KEEPALIVE
				socket.socket().setKeepAlive(true);

				// do NOT tune socket buffer sizes, this will disable auto-tuning

				// create transport
				final Transport transport = new BlockingServerTCPTransport(context, socket, ((ServerContextImpl)context).getServerResponseHandler(), receiveBufferSize);
				//final Transport transport = new NonBlockingServerTCPTransport(context, poller, socket, ((ServerContextImpl)context).getServerResponseHandler(), receiveBufferSize);

				// validate connection
				if (!validateConnection(transport, address))
				{
					// TODO
					// wait for negative response to be sent back and
					// hold off the client for retrying at very high rate
					Thread.sleep(1000);

					transport.close();
					context.getLogger().finer("Connection to PVA client " + address + " failed to be validated, closing it.");
					continue;
				}

				context.getLogger().finer("Serving to PVA client: " + address);

			}
			catch (AsynchronousCloseException ace)
			{
				// noop
			}
			catch (Throwable th)
			{
				// TODO remove !!!
				th.printStackTrace();
			}
		}
	}


	/**
	 * Validate connection by sending a validation message request.
	 * @return <code>true</code> on success.
	 */
	private boolean validateConnection(Transport transport, SocketAddress address)
	{
		try {
			return transport.verify(5000);
		}
		catch (Throwable th) {
			th.printStackTrace();
			context.getLogger().log(Level.FINEST, "Validation of " + address + " failed.", th);
			return false;
		}
	}

	/**
	 * Initialize connection acceptance.
	 * @return port where server is listening
	 */
	private int initialize(int port) throws PVAException
	{
		// specified bind address
		// TODO EPICS_PVAS_INTF_ADDR_LIST, same as EPICS_CAS_INTF_ADDR_LIST
		bindAddress = new InetSocketAddress(port);

		int tryCount = 0;
		while (true)
		{
			tryCount++;

			try
			{
				context.getLogger().finer("Creating acceptor to " + bindAddress + ".");

				serverSocketChannel = ServerSocketChannel.open();
				serverSocketChannel.socket().bind(bindAddress);
				serverSocketChannel.configureBlocking(true);

				// update bind address, if dynamically port selection was used
				if (bindAddress.getPort() == 0)
				{
					bindAddress = new InetSocketAddress(serverSocketChannel.socket().getLocalPort());
					context.getLogger().info("Using dynamically assigned TCP port " + bindAddress.getPort() + ".");
				}

				new Thread(new Runnable() {

					public void run() {
						handleEvents();
					}
				}, "TCP-acceptor").start();

				// all OK, return
				return bindAddress.getPort();
			}
			catch (BindException be)
			{
				// failed to bind to specified bind address,
				// try to get port dynamically, but only once
				if (tryCount == 1)
				{
					context.getLogger().info("Configured TCP port " + port + " is unavailable, trying to assign it dynamically.");
					bindAddress = new InetSocketAddress(0);
				}
				else
				{
					throw new PVAException("Failed to create acceptor to " + bindAddress, be);
				}
			}
			catch (Throwable th)
			{
				throw new PVAException("Failed to create acceptor to " + bindAddress, th);
			}
		}

	}

	/**
	 * Bind socket address.
	 * @return bind socket address, <code>null</code> if not binded.
	 */
	public InetSocketAddress getBindAddress()
	{
		return bindAddress;
		/*
		return (serverSocketChannel != null) ?
				serverSocketChannel.socket().getInetAddress() : null;
		*/
	}

	/**
	 * Destroy acceptor (stop listening).
	 */
	public void destroy()
	{
		if (destroyed.getAndSet(true))
			return;

		if (serverSocketChannel != null)
		{
			context.getLogger().finer("Stopped accepting connections at " + bindAddress + ".");
			try {
				serverSocketChannel.socket().close();
			} catch (IOException e) {
				// just log
				context.getLogger().log(Level.FINE, "Failed to close acceptor socket at " + bindAddress + ".", e);
			}
		}
	}

}
