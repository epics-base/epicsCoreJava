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

package org.epics.pvaccess.client.impl.remote.handlers;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.epics.pvaccess.client.impl.remote.ClientContextImpl;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvdata.misc.SerializeHelper;


/**
 * Connection validation message handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ConnectionValidationHandler extends AbstractClientResponseHandler {

	/**
	 * @param context
	 */
	public ConnectionValidationHandler(ClientContextImpl context) {
		super(context, "Connection validation");
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.AbstractResponseHandler#handleResponse(java.net.InetSocketAddress, org.epics.pvaccess.core.Transport, byte, byte, int, java.nio.ByteBuffer)
	 */
	@Override
	public void handleResponse(InetSocketAddress responseFrom, Transport transport, byte version, byte command, int payloadSize, ByteBuffer payloadBuffer) {
		super.handleResponse(responseFrom, transport, version, command, payloadSize, payloadBuffer);

		transport.setRemoteRevision(version);

		transport.ensureData(4+2);
		transport.setRemoteTransportReceiveBufferSize(payloadBuffer.getInt());
		// TODO
		// TODO serverIntrospectionRegistryMaxSize
		/*int serverIntrospectionRegistryMaxSize = */ payloadBuffer.getShort(); // & 0x0000FFFF;
		// TODO authNZ
		int size = SerializeHelper.readSize(payloadBuffer, transport);
		List<String> offeredSecurityPlugins = new ArrayList<String>(size);
		for (int i = 0; i < size; i++)
			offeredSecurityPlugins.add(SerializeHelper.deserializeString(payloadBuffer, transport));

		transport.authNZInitialize(offeredSecurityPlugins);
	}

}
