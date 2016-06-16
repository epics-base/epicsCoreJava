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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.logging.Level;

import org.epics.pvaccess.client.impl.remote.ClientContextImpl;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.udp.BlockingUDPTransport;
import org.epics.pvaccess.util.InetAddressUtil;

/**
 * Search request handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 */
public class SearchHandler extends AbstractClientResponseHandler {

	public SearchHandler(ClientContextImpl context) {
		super(context, "Search request");
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.AbstractResponseHandler#handleResponse(java.net.InetSocketAddress, org.epics.pvaccess.core.Transport, byte, byte, int, java.nio.ByteBuffer)
	 */
	@Override
	public void handleResponse(InetSocketAddress responseFrom, Transport transport, byte version, byte command, int payloadSize, ByteBuffer payloadBuffer) {
		super.handleResponse(responseFrom, transport, version, command, payloadSize, payloadBuffer);

		transport.ensureData(4+1+3+16+2);

		final int startPosition = payloadBuffer.position();

		/*final int searchSequenceId =*/ payloadBuffer.getInt();
		final byte qosCode = payloadBuffer.get();

		// reserved part
		payloadBuffer.get();
		payloadBuffer.getShort();

		// 128-bit IPv6 address
		byte[] byteAddress = new byte[16]; 
		payloadBuffer.get(byteAddress);
		
		final int port = payloadBuffer.getShort() & 0xFFFF;
		
		
		// NOTE: Java knows how to compare IPv4/IPv6 :)
		
		InetAddress addr;
		try {
			addr = InetAddress.getByAddress(byteAddress);
		} catch (UnknownHostException e) {
			context.getLogger().log(Level.FINER, "Invalid address '" +  new String(byteAddress) + "' in search response received from: " + responseFrom, e);
			return;
		}

		// accept given address if explicitly specified by sender
		if (!addr.isAnyLocalAddress())
			responseFrom = new InetSocketAddress(addr, port);
		else
			responseFrom = new InetSocketAddress(responseFrom.getAddress(), port);


        // we ignore the rest, since we care only about data relevant
        // to do the local multicast

		// 
		// locally broadcast if unicast (qosCode & 0x80 == 0x80)
		//
		if ((qosCode & 0x80) == 0x80)
		{
			BlockingUDPTransport bt = 
					//context.getLocalMulticastTransport();
					context.getSearchTransport();
			if (bt != null)
			{
				// clear unicast flag
				payloadBuffer.put(startPosition+4, (byte)(qosCode & ~0x80));
				
				// update response address
				payloadBuffer.position(startPosition+8);
				InetAddressUtil.encodeAsIPv6Address(payloadBuffer, responseFrom.getAddress());
				
				payloadBuffer.position(payloadBuffer.limit());		// send will call flip()
				
				bt.send(payloadBuffer, context.getLocalMulticastAddress());
				return;
			}
		}
		
	}
	
}
 
