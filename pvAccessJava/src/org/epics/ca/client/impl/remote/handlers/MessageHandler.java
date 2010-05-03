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

package org.epics.ca.client.impl.remote.handlers;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.epics.ca.client.impl.remote.ClientContextImpl;
import org.epics.ca.impl.remote.ResponseRequest;
import org.epics.ca.impl.remote.Transport;
import org.epics.pvData.misc.SerializeHelper;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.Requester;

/**
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class MessageHandler extends AbstractClientResponseHandler {

	/**
	 * @param context
	 */
	public MessageHandler(ClientContextImpl context) {
		super(context, "Message");
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.AbstractResponseHandler#handleResponse(java.net.InetSocketAddress, org.epics.ca.core.Transport, byte, byte, int, java.nio.ByteBuffer)
	 */
	@Override
	public void handleResponse(InetSocketAddress responseFrom, Transport transport, byte version, byte command, int payloadSize, ByteBuffer payloadBuffer) {
		super.handleResponse(responseFrom, transport, version, command, payloadSize, payloadBuffer);
		
		transport.ensureData(Integer.SIZE/Byte.SIZE+1);
		final ResponseRequest nrr = (ResponseRequest)context.getResponseRequest(payloadBuffer.getInt());
		// we can supply message only to Requester
		final Requester requester;
		if (nrr != null && (requester = nrr.getRequester()) != null)
		{
			final MessageType type = MessageType.values()[payloadBuffer.get()];
			final String message = SerializeHelper.deserializeString(payloadBuffer, transport);
			requester.message(message, type);
		}
	}

}
