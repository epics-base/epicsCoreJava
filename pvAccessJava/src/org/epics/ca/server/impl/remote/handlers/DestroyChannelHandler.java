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

package org.epics.ca.server.impl.remote.handlers;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.logging.Level;

import org.epics.ca.impl.remote.ChannelHostingTransport;
import org.epics.ca.impl.remote.Transport;
import org.epics.ca.impl.remote.TransportSendControl;
import org.epics.ca.impl.remote.TransportSender;
import org.epics.ca.server.impl.remote.ServerChannelImpl;
import org.epics.ca.server.impl.remote.ServerContextImpl;

/**
 * Destroy channel request handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class DestroyChannelHandler extends AbstractServerResponseHandler {

	/**
	 * @param context
	 */
	public DestroyChannelHandler(ServerContextImpl context) {
		super(context, "Destroy channel request");
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.AbstractResponseHandler#handleResponse(java.net.InetSocketAddress, org.epics.ca.core.Transport, byte, byte, int, java.nio.ByteBuffer)
	 */
	@Override
	public void handleResponse(InetSocketAddress responseFrom, Transport transport, byte version, byte command, int payloadSize, ByteBuffer payloadBuffer) {
		super.handleResponse(responseFrom, transport, version, command, payloadSize, payloadBuffer);

		// NOTE: we do not explicitly check if transport OK
		final ChannelHostingTransport casTransport = (ChannelHostingTransport)transport;
		
		transport.ensureData(2*Integer.SIZE/Byte.SIZE);
		final int sid = payloadBuffer.getInt();
		final int cid = payloadBuffer.getInt();
		
		// get channel by SID
		final ServerChannelImpl channel = (ServerChannelImpl)casTransport.getChannel(sid);
		if (channel == null) 
		{
			if (!transport.isClosed())
				context.getLogger().log(Level.WARNING, "Trying to destroy a channel that no longer exists (SID: " + sid + ", CID: " + cid + ", client: " + responseFrom + ").");
			return;
		} 

		// destroy
		channel.destroy();
		
		// .. and unregister
		casTransport.unregisterChannel(sid);
		
		// send response back
		transport.enqueueSendRequest(
				new TransportSender() {

					@Override
					public void send(ByteBuffer buffer, TransportSendControl control) {
						control.startMessage((byte)8, 2*Integer.SIZE/Byte.SIZE);
						buffer.putInt(sid);
						buffer.putInt(cid);
					}

					@Override
					public void lock() {
						// noop
					}

					@Override
					public void unlock() {
						// noop
					}
					
			});
		
	}

}
