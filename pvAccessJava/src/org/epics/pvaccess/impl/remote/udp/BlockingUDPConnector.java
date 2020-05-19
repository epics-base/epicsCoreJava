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

package org.epics.pvaccess.impl.remote.udp;

import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.channels.DatagramChannel;

import org.epics.pvaccess.impl.remote.ConnectionException;
import org.epics.pvaccess.impl.remote.Connector;
import org.epics.pvaccess.impl.remote.Context;
import org.epics.pvaccess.impl.remote.ProtocolType;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportClient;
import org.epics.pvaccess.impl.remote.request.ResponseHandler;


/**
 * UDP broadcast connector.
 * This is not a real connector since it binds.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class BlockingUDPConnector implements Connector {

	/**
	 * Context instance.
	 */
	private Context context;
	
	/**
	 * Send address.
	 */
	private InetSocketAddress[] sendAddresses;
	
	/**
	 * Reuse socket flag.
	 */
	private boolean reuseSocket;
	
	/**
	 * Broadcast flag.
	 */
	private boolean broadcast;

	public BlockingUDPConnector(Context context, boolean reuseSocket, InetSocketAddress[] sendAddresses, boolean broadcast) {
		this.context = context;
		this.reuseSocket = reuseSocket;
		this.sendAddresses = sendAddresses;
		this.broadcast = broadcast;
	}
	
	/**
	 * NOTE: transport client is ignored for broadcast (UDP). 
	 * @see org.epics.pvaccess.impl.remote.Connector#connect(org.epics.pvaccess.impl.remote.TransportClient, org.epics.pvaccess.impl.remote.request.ResponseHandler, java.net.InetSocketAddress, byte, short)
	 */
	public Transport connect(TransportClient client, ResponseHandler responseHandler, InetSocketAddress bindAddress, byte transportRevision, short priority)
		throws ConnectionException
	{
		context.getLogger().finer("Creating datagram socket to " + bindAddress + ".");
		
		DatagramChannel socket = null;
		try
		{        
			socket = DatagramChannel.open(StandardProtocolFamily.INET);

			// use blocking channel
			socket.configureBlocking(true);
		
			// set SO_BROADCAST
			if (broadcast)
				socket.socket().setBroadcast(true);
			
			// TODO tune buffer sizes?! Win32 defaults are 8k, which is OK
			//socket.socket().setReceiveBufferSize();
			//socket.socket().setSendBufferSize();

			// try to bind
			if (reuseSocket)
				socket.socket().setReuseAddress(true);
			
			socket.socket().bind(bindAddress);
			
			// create transport
			return new BlockingUDPTransport(context, responseHandler, socket,
											bindAddress, sendAddresses, transportRevision);
		}
		catch (Throwable th)
		{
			// close socket, if open
			try
			{
				if (socket != null)
					socket.close();
			}
			catch (Throwable t) { /* noop */ }

			throw new ConnectionException("Failed to bind to '" + bindAddress + "'.", bindAddress, ProtocolType.udp.name(), th);
		}
            
	}


}
