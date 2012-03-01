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

package org.epics.ca.server.impl.remote.tcp;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.epics.ca.CAException;
import org.epics.ca.impl.remote.Context;
import org.epics.ca.impl.remote.Transport;
import org.epics.ca.server.impl.remote.ServerContextImpl;
import org.epics.ca.server.impl.remote.ServerResponseHandler;

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
	/**
	 * @param context
	 * @param port
	 * @param receiveBufferSize
	 * @throws CAException
	 */
	public BlockingTCPAcceptor(Context context, int port, int receiveBufferSize) throws CAException {
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
				context.getLogger().finer("Accepted connection from CA client: " + address);
				
				// enable TCP_NODELAY (disable Nagle's algorithm)
				socket.socket().setTcpNoDelay(true);
				
				// enable TCP_KEEPALIVE
				socket.socket().setKeepAlive(true);

				// TODO tune buffer sizes?!
				//socket.socket().setReceiveBufferSize();
				//socket.socket().setSendBufferSize();
				
				// create transport
				// each transport should have its own response handler since it is not "shareable" // TODO not anymore, make it sahreable
				final Transport transport = new BlockingServerTCPTransport(context, socket, new ServerResponseHandler((ServerContextImpl)context), receiveBufferSize);
				//final Transport transport = new NonBlockingServerTCPTransport(context, poller, socket, new ServerResponseHandler((ServerContextImpl)context), receiveBufferSize);
	
				// validate connection
				if (!validateConnection(transport, address))
				{
					transport.close();
					context.getLogger().finer("Connection to CA client " + address + " failed to be validated, closing it.");
					return;
				}
				
				context.getLogger().finer("Serving to CA client: " + address);
	
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
			transport.verify(0);
			return true;
		}
		catch (Throwable th) {
			th.printStackTrace();
			context.getLogger().log(Level.FINEST, "Validation of " + address + " failed.", th);
			return false;
		}
	}
	
	/**
	 * Initialize connection acception. 
	 * @return port where server is listening
	 */
	private int initialize(int port) throws CAException
	{
		// specified bind address
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
					
					@Override
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
					throw new CAException("Failed to create acceptor to " + bindAddress, be);
				}				
			}
			catch (Throwable th)
			{
				throw new CAException("Failed to create acceptor to " + bindAddress, th);
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
