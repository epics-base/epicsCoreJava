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

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.epics.pvaccess.CAConstants;
import org.epics.pvaccess.impl.remote.Context;
import org.epics.pvaccess.impl.remote.TransportClient;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.TransportSender;
import org.epics.pvaccess.impl.remote.io.Poller;
import org.epics.pvaccess.impl.remote.request.ResponseHandler;
import org.epics.pvaccess.impl.remote.server.ChannelHostingTransport;
import org.epics.pvaccess.impl.remote.server.ServerChannel;
import org.epics.pvaccess.impl.remote.tcp.NonBlockingTCPTransport;
import org.epics.pvaccess.util.IntHashMap;
import org.epics.pvdata.pv.PVField;

/**
 * Server TCP transport implementation.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class NonBlockingServerTCPTransport extends NonBlockingTCPTransport implements ChannelHostingTransport, TransportSender {

	/**
	 * Last SID cache. 
	 */
	private AtomicInteger lastChannelSID = new AtomicInteger(0);

	/**
	 * Channel table (SID -> channel mapping).
	 */
	private IntHashMap channels;

	/**
	 * Server TCP transport constructor.
	 * @param context context where transport lives in.
	 * @param channel used socket channel.
	 * @param responseHandler response handler used to process CA headers.
	 * @param receiveBufferSize receive buffer size.
	 */
	public NonBlockingServerTCPTransport(Context context, 
		// TODO
			Poller poller,
			   SocketChannel channel,
			   ResponseHandler responseHandler,
			   int receiveBufferSize) throws SocketException {
		super(context, poller, channel, responseHandler, receiveBufferSize, CAConstants.CA_DEFAULT_PRIORITY);
		// NOTE: priority not yet known, default priority is used to register/unregister
		// TODO implement priorities in Reactor... not that user will change it.. still getPriority() must return "registered" priority!
		
		final int INITIAL_SIZE = 64;
		channels = new IntHashMap(INITIAL_SIZE);
		
		//start();
	}
	
	
	/**
	 * @see org.epics.pvaccess.impl.remote.tcp.TCPTransport#internalClose()
	 */
	@Override
	protected void internalClose() {
		super.internalClose();
		destroyAllChannels();
	}

	/**
	 * Destroy all channels.
	 */
	private void destroyAllChannels() {

		ServerChannel[] channelsArray;
		synchronized (channels)
		{
    		// resource allocation optimization
    		if (channels.size() == 0)
    			return;

    		channelsArray = new ServerChannel[channels.size()];
			channels.toArray(channelsArray);

			channels.clear();
		}

		context.getLogger().fine("Transport to " + socketAddress + " still has " + channelsArray.length + " channel(s) active and closing...");
		
		for (int i = 0; i < channelsArray.length; i++)
		{
			try
			{
				channelsArray[i].destroy();
			}
			catch (Throwable th)
			{
				th.printStackTrace();
			}
		}
	}

	/**
	 * Preallocate new channel SID.
	 * @return new channel server id (SID).
	 */
	public int preallocateChannelSID()
	{
		synchronized (channels) {
			// search first free (theoretically possible loop of death)
			int sid = lastChannelSID.incrementAndGet();
			while (channels.containsKey(sid))
				sid = lastChannelSID.incrementAndGet();
			return sid;
		}
	}

	/**
	 * De-preallocate new channel SID.
	 * @param sid preallocated channel SID. 
	 */
	public void depreallocateChannelSID(int sid)
	{
		// noop
	}

	/**
	 * Register a new channel.
	 * @param sid preallocated channel SID. 
	 * @param channel channel to register.
	 */
	public void registerChannel(int sid, ServerChannel channel)
	{
		synchronized (channels) {
			channels.put(sid, channel);
		}
	}
	
	/**
	 * Unregister a new channel (and deallocates its handle).
	 * @param sid SID
	 */
	public void unregisterChannel(int sid)
	{
		synchronized (channels) {
			channels.remove(sid);
		}
	}

	/**
	 * Get channel by its SID.
	 * @param sid channel SID
	 * @return channel with given SID, <code>null</code> otherwise
	 */
	public ServerChannel getChannel(int sid)
	{
		synchronized (channels) {
			return (ServerChannel)channels.get(sid);
		}
	}

	/**
	 * Get channel count.
	 * @return channel count.
	 */
	public int getChannelCount() 
	{
		synchronized (channels) {
			return channels.size();
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.core.ChannelHostingTransport#getSecurityToken()
	 */
	public PVField getSecurityToken() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.TransportSender#lock()
	 */
	@Override
	public void lock() {
		// noop
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.TransportSender#unlock()
	 */
	@Override
	public void unlock() {
		// noop
	}

	/**
	 * CA connection validation request.
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
	@Override
	public void send(ByteBuffer buffer, TransportSendControl control) {

		//
		// set byte order control message 
		//
		
		ensureBuffer(CAConstants.CA_MESSAGE_HEADER_SIZE);
		sendBuffer.put(CAConstants.CA_MAGIC);
		sendBuffer.put(CAConstants.CA_VERSION);
		sendBuffer.put((byte)0x81);		// control + big endian
		sendBuffer.put((byte)2);		// set byte order
		sendBuffer.putInt(0);		

		
		//
		// send verification message
		//
		
		control.startMessage((byte)1, 2*Integer.SIZE/Byte.SIZE);
		
		// receive buffer size
		buffer.putInt(getReceiveBufferSize());
			
		// socket receive buffer size
		buffer.putInt(getSocketReceiveBufferSize());
		
		// send immediately
		control.flush(true);
	}

	/**
	 * Verify transport. Server side is self-verified.
	 */
	public void verify() 
	{
		enqueueSendRequest(this);
		verified();
	}


	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.Transport#acquire(org.epics.pvaccess.impl.remote.TransportClient)
	 */
	@Override
	public boolean acquire(TransportClient client) {
		// TODO Auto-generated method stub
		return false;
	}


	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.Transport#release(org.epics.pvaccess.impl.remote.TransportClient)
	 */
	@Override
	public void release(TransportClient client) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.Transport#verified()
	 */
	@Override
	public void verified() {
	}
	
	/**
     * Verify transport. Server side is self-verified.
	 * @see org.epics.pvaccess.impl.remote.Transport#verify(long)
	 */
	@Override
	public boolean verify(long timeoutMs) {
		verified();
		return true;
	}


	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.codec.impl.NonBlockingAbstractCodec#ready()
	 */
	@Override
	protected void ready() {
		enqueueSendRequest(this);
	}
	
}
