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

package org.epics.pvaccess.server.impl.remote.handlers;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.misc.SerializeHelper;


/**
 * Connection validation message handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ConnectionValidationHandler extends AbstractServerResponseHandler {

	/**
	 * @param context
	 */
	public ConnectionValidationHandler(ServerContextImpl context) {
		super(context, "Connection validation");
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.AbstractResponseHandler#handleResponse(java.net.InetSocketAddress, org.epics.pvaccess.core.Transport, byte, byte, int, java.nio.ByteBuffer)
	 */
	@Override
	public void handleResponse(InetSocketAddress responseFrom, Transport transport, byte version, byte command, int payloadSize, ByteBuffer payloadBuffer) {
		super.handleResponse(responseFrom, transport, version, command, payloadSize, payloadBuffer);

		transport.setRemoteRevision(version);

		transport.ensureData(4+2+2);
		transport.setRemoteTransportReceiveBufferSize(payloadBuffer.getInt());
		// TODO clientIntrospectionRegistryMaxSize
		/*int clientIntrospectionRegistryMaxSize = */ payloadBuffer.getShort(); // & 0x0000FFFF;
		// TODO connectionQos
		/*short connectionQos =*/ payloadBuffer.getShort();
		// TODO authNZ
		/*String authNZ = */ SerializeHelper.deserializeString(payloadBuffer, transport);
		
		// TODO call this after authNZ has done their work
		transport.verified(StatusFactory.getStatusCreate().getStatusOK());
	}
	
}
