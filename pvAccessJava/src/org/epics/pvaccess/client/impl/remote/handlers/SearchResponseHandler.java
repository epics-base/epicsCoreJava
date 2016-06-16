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
import org.epics.pvaccess.client.impl.remote.search.ChannelSearchManager;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvdata.misc.SerializeHelper;


/**
 * Search response.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 */
public class SearchResponseHandler extends AbstractClientResponseHandler {

	public SearchResponseHandler(ClientContextImpl context) {
		super(context, "Search response");
	}

	
	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.AbstractResponseHandler#handleResponse(java.net.InetSocketAddress, org.epics.pvaccess.core.Transport, byte, byte, int, java.nio.ByteBuffer)
	 */
	@Override
	public void handleResponse(InetSocketAddress responseFrom, Transport transport, byte version, byte command, int payloadSize, ByteBuffer payloadBuffer) {
		super.handleResponse(responseFrom, transport, version, command, payloadSize, payloadBuffer);

		transport.ensureData(12+4+16+2);
		
		// 12-byte GUID
		byte[] guid = new byte[12]; 
		payloadBuffer.get(guid);

		final int searchSequenceId = payloadBuffer.getInt();

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

		// TODO for now we support only one
		/*final String protocol =*/ SerializeHelper.deserializeString(payloadBuffer, transport);
		
		transport.ensureData(1);
		final boolean found = payloadBuffer.get() != 0;
		if (!found)
			return;

		// reads CIDs
		final ChannelSearchManager csm = context.getChannelSearchManager();
		final int count = payloadBuffer.getShort() & 0xFFFF;
		for (int i = 0; i < count; i++)
		{
			transport.ensureData(4);
			final int cid = payloadBuffer.getInt();
			csm.searchResponse(cid, searchSequenceId, version, responseFrom);
		}
	}
}
