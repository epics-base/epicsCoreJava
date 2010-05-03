/*
 * Copyright (c) 2009 by Cosylab
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

import org.epics.ca.impl.remote.ChannelHostingTransport;
import org.epics.ca.impl.remote.Transport;
import org.epics.ca.server.impl.remote.ServerChannelImpl;
import org.epics.ca.server.impl.remote.ServerContextImpl;
import org.epics.pvData.misc.Destroyable;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.Status;

/**
 * Cancel request handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class CancelRequestHandler extends AbstractServerResponseHandler {

	/**
	 * @param context
	 */
	public CancelRequestHandler(ServerContextImpl context) {
		super(context, "Cancel request");
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.AbstractResponseHandler#handleResponse(java.net.InetSocketAddress, org.epics.ca.core.Transport, byte, byte, int, java.nio.ByteBuffer)
	 */
	@Override
	public void handleResponse(InetSocketAddress responseFrom, Transport transport, byte version, byte command, int payloadSize, ByteBuffer payloadBuffer) {
		super.handleResponse(responseFrom, transport, version, command, payloadSize, payloadBuffer);

		// NOTE: we do not explicitly check if transport is OK
		ChannelHostingTransport casTransport = (ChannelHostingTransport)transport;

		transport.ensureData(2*Integer.SIZE/Byte.SIZE);
		final int sid = payloadBuffer.getInt();
		final int ioid = payloadBuffer.getInt();
    
		final ServerChannelImpl channel = (ServerChannelImpl)casTransport.getChannel(sid);
		if (channel == null) {
			failureResponse(transport, ioid, BaseChannelRequester.badCIDStatus);
			return;
		}
	
		final Destroyable request = channel.getRequest(ioid);
		if (request == null) {
			failureResponse(transport, ioid, BaseChannelRequester.badIOIDStatus);
			return;
		}
		
		// destroy
		request.destroy();
		
		// ... and remove from channel
		channel.unregisterRequest(ioid);
	}

	/**
	 * @param transport
	 * @param ioid
	 * @param errorStatus
	 */
	private void failureResponse(Transport transport, int ioid, Status errorStatus)
	{
		BaseChannelRequester.message(transport, ioid, errorStatus.getMessage(), MessageType.warning);
	}
	
}
