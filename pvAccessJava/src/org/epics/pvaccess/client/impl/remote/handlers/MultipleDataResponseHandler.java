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

package org.epics.pvaccess.client.impl.remote.handlers;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.client.impl.remote.ClientContextImpl;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.request.DataResponse;

/**
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 */
public class MultipleDataResponseHandler extends AbstractClientResponseHandler {

	public MultipleDataResponseHandler(ClientContextImpl context) {
		super(context, "Multiple data response");
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.AbstractResponseHandler#handleResponse(java.net.InetSocketAddress, org.epics.pvaccess.core.Transport, byte, byte, int, java.nio.ByteBuffer)
	 */
	@Override
	public void handleResponse(InetSocketAddress responseFrom, Transport transport, byte version, byte command, int payloadSize, ByteBuffer payloadBuffer) {
		super.handleResponse(responseFrom, transport, version, command, payloadSize, payloadBuffer);
		
		// TODO add submessage payload size, so that non-existant IOID can be skipped
		// and others not lost
		
		while (true)
		{
			transport.ensureData(Integer.SIZE/Byte.SIZE);
			final int ioid = payloadBuffer.getInt();
			if (ioid == PVAConstants.PVA_INVALID_IOID)
				return;
			final DataResponse nrr = (DataResponse)context.getResponseRequest(ioid);
			if (nrr == null) {
				context.getLogger().severe("Unknown request ID within packed response message, all subsequent responses in this message will be lost!");
				return;
			}
			nrr.response(transport, version, payloadBuffer);					
		}
	}

}
