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

import org.epics.ca.impl.remote.Transport;
import org.epics.ca.server.impl.remote.ServerContextImpl;

/**
 * Introspection search request handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class IntrospectionSearchHandler extends AbstractServerResponseHandler {

	/**
	 * PVField factory.
	 */
	//private static final PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();

	/**
	 * @param context
	 */
	public IntrospectionSearchHandler(ServerContextImpl context) {
		super(context, "Introspection search request");
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.AbstractResponseHandler#handleResponse(java.net.InetSocketAddress, org.epics.ca.core.Transport, byte, byte, int, java.nio.ByteBuffer)
	 */
	@Override
	public void handleResponse(InetSocketAddress responseFrom, Transport transport, byte version, byte command, int payloadSize, ByteBuffer payloadBuffer) {
		super.handleResponse(responseFrom, transport, version, command, payloadSize, payloadBuffer);

		throw new RuntimeException("not implemented");
		/*
		// IOID
		final int ioid = payloadBuffer.getInt();

		// search data (non-null)
		final Field field = IntrospectionRegistry.deserializeFull(payloadBuffer);
		final PVField searchData = pvDataCreate.createPVField(null, field);
		searchData.deserialize(payloadBuffer);
		}*/
	}

}
