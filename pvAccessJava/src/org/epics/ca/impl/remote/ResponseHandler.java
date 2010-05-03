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

package org.epics.ca.impl.remote;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Interface defining response handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public interface ResponseHandler {

	/**
	 * Handle response.
	 * @param responseFrom	remove address of the responder, <code>null</code> if unknown. 
	 * @param transport	response source transport.
	 * @param version message version.
	 * @param payloadSize size of this message data available in the <code>payloadBuffer</code>.
	 * @param payloadBuffer	message payload data.
	 * 						Note that this might not be the only message in the buffer.
	 * 						Code must not manilupate buffer. 
	 */
	public void handleResponse(InetSocketAddress responseFrom, Transport transport,
							   byte version, byte command, int payloadSize,
							   ByteBuffer payloadBuffer);
}
