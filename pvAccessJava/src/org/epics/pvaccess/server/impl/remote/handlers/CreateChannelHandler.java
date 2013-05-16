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

package org.epics.pvaccess.server.impl.remote.handlers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.logging.Level;

import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.client.Channel.ConnectionState;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.TransportSender;
import org.epics.pvaccess.impl.remote.server.ChannelHostingTransport;
import org.epics.pvaccess.server.impl.remote.ServerChannelImpl;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;

/**
 * Create channel request handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class CreateChannelHandler extends AbstractServerResponseHandler {

	private final ChannelProvider provider;

	/**
	 * @param context
	 */
	public CreateChannelHandler(ServerContextImpl context) {
		super(context, "Create channel request");
		provider = context.getChannelProvider();
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.AbstractResponseHandler#handleResponse(java.net.InetSocketAddress, org.epics.pvaccess.core.Transport, byte, byte, int, java.nio.ByteBuffer)
	 */
	@Override
	public void handleResponse(InetSocketAddress responseFrom, Transport transport, byte version, byte command, int payloadSize, ByteBuffer payloadBuffer) {
		super.handleResponse(responseFrom, transport, version, command, payloadSize, payloadBuffer);

		// TODO for not only one request at the time is supported, i.e. dataCount == 1

		transport.ensureData((Integer.SIZE+Short.SIZE)/Byte.SIZE);
		final short count = payloadBuffer.getShort();
		if (count != 1)
			throw new UnsupportedOperationException("only 1 supported for now");
		final int cid = payloadBuffer.getInt();
		
		final String channelName = SerializeHelper.deserializeString(payloadBuffer, transport);
		if (channelName == null || channelName.length() == 0)
		{
			context.getLogger().warning("Zero length channel name, disconnecting client: " + transport.getRemoteAddress());
			disconnect(transport);
			return;
		}
		else if (channelName.length() > PVAConstants.MAX_CHANNEL_NAME_LENGTH)
		{
			context.getLogger().warning("Unreasonable channel name length, disconnecting client: " + transport.getRemoteAddress());
			disconnect(transport);
			return;
		}

		final ChannelRequester cr = new ChannelRequesterImpl(transport, channelName, cid);
		provider.createChannel(channelName, cr, transport.getPriority());
	}

	/**
	 * Disconnect.
	 */
	private void disconnect(Transport transport)
	{
		try {
			transport.close();
		} catch (IOException e) {
			// noop
		}
	}
	
	
	/**
	 * Async. completion callback support.
	 * @author msekoranja
	 */
	class ChannelRequesterImpl implements ChannelRequester, TransportSender
	{
		private final Transport transport;
		private final String channelName;
		private final int cid;

		private Status status;
		private Channel channel;
		
		/**
		 * @param transport
		 * @param channelName
		 * @param cid
		 */
		public ChannelRequesterImpl(Transport transport, String channelName, int cid) {
			this.transport = transport;
			this.channelName = channelName;
			this.cid = cid;
		}
		
		@Override
		public void channelCreated(Status status, Channel channel) {
			synchronized (this) {
				this.status = status;
				this.channel = channel;
			}
			transport.enqueueSendRequest(this);
		}


		@Override
		public void channelStateChange(Channel c, ConnectionState isConnected) {
			// noop
		}

		@Override
		public String getRequesterName() {
			return transport + "/" + cid;
		}

		@Override
		public void message(String message, MessageType messageType) {
			// no requester yet
			System.err.println("[" + messageType + "] " + message);
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

		/* (non-Javadoc)
		 * @see org.epics.pvaccess.impl.remote.TransportSender#send(java.nio.ByteBuffer, org.epics.pvaccess.impl.remote.TransportSendControl)
		 */
		@Override
		public void send(ByteBuffer buffer, TransportSendControl control) {
			final Channel channel;
			final Status status;
			synchronized (this) {
				channel = this.channel;
				status = this.status;
			}

			// error response
			if (channel == null) {
				createChannelFailedResponse(buffer, control, status);
			}
			// OK
			else {
				
				ServerChannelImpl serverChannel = null;
				try
				{
					// NOTE: we do not explicitly check if transport OK
					final ChannelHostingTransport casTransport = (ChannelHostingTransport)transport;

					//
					// create a new channel instance
					//
					int sid = casTransport.preallocateChannelSID();
					try
					{
						serverChannel = new ServerChannelImpl(channel, cid, sid, casTransport.getSecurityToken());
						
						// ack allocation and register
						casTransport.registerChannel(sid, serverChannel);
						
					} catch (Throwable th) {
						// depreallocate and rethrow
						casTransport.depreallocateChannelSID(sid);
						throw th;
					}
					
					control.startMessage((byte)7, 2*Integer.SIZE/Byte.SIZE);
					buffer.putInt(cid);
					buffer.putInt(sid);
					status.serialize(buffer, control);
					//buffer.putShort(serverChannel.getAccessRights());
					//control.flush(true);
				} catch (Throwable th) {
					context.getLogger().log(Level.WARNING, "Exception caught when creating channel: " + channelName, th);
					createChannelFailedResponse(buffer, control,
							BaseChannelRequester.statusCreate.createStatus(StatusType.FATAL, "failed to create channel", th));
					if (serverChannel != null)
						serverChannel.destroy();
				}
			}
		}

		/**
		 * @param buffer
		 * @param control
		 * @param status
		 */
		private void createChannelFailedResponse(ByteBuffer buffer, TransportSendControl control, final Status status)
		{
			control.startMessage((byte)7, 2*Integer.SIZE/Byte.SIZE);
			buffer.putInt(cid);
			buffer.putInt(-1);
			status.serialize(buffer, control);
			//control.flush(true);
		}

		
	}
	
}
