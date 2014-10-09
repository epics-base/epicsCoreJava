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

package org.epics.pvaccess.client.impl.remote;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.epics.pvaccess.client.impl.remote.handlers.BadResponse;
import org.epics.pvaccess.client.impl.remote.handlers.BeaconHandler;
import org.epics.pvaccess.client.impl.remote.handlers.ConnectionValidatedHandler;
import org.epics.pvaccess.client.impl.remote.handlers.ConnectionValidationHandler;
import org.epics.pvaccess.client.impl.remote.handlers.CreateChannelHandler;
import org.epics.pvaccess.client.impl.remote.handlers.DataResponseHandler;
import org.epics.pvaccess.client.impl.remote.handlers.EchoResponse;
import org.epics.pvaccess.client.impl.remote.handlers.MessageHandler;
import org.epics.pvaccess.client.impl.remote.handlers.MultipleDataResponseHandler;
import org.epics.pvaccess.client.impl.remote.handlers.NoopResponse;
import org.epics.pvaccess.client.impl.remote.handlers.SearchResponseHandler;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.request.ResponseHandler;
import org.epics.pvaccess.impl.security.AuthNZHandler;
import org.epics.pvaccess.util.HexDump;


/**
 * PVA response handler - main handler which dispatches responses to appripriate handlers.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ClientResponseHandler implements ResponseHandler {

	/**
	 * Table of response handlers for each command ID.
	 */
	private final ResponseHandler[] handlerTable;

	/**
	 * Context instance.
	 */
	private final ClientContextImpl context;
	
	/**
	 * @param context
	 */
	public ClientResponseHandler(ClientContextImpl context) {
		this.context = context;
		final ResponseHandler badResponse = new BadResponse(context);
		final ResponseHandler dataResponse = new DataResponseHandler(context);
		
		handlerTable = new ResponseHandler[]
			{
				new BeaconHandler(context), /*  0 */
				new ConnectionValidationHandler(context), /*  1 */
				new EchoResponse(context), /*  2 */
				new NoopResponse(context, "Search"), /*  3 */
				new SearchResponseHandler(context), /*  4 */
				new AuthNZHandler(context.getDebugLevel() >= 3), /*  5 */
				new NoopResponse(context, "Access rights change"), /*  6 - access rights change */
				new CreateChannelHandler(context), /*  7 */
				new NoopResponse(context, "Destroy channel"), /*  8 */ // TODO it might be useful to implement this...
				new ConnectionValidatedHandler(context), /*  9 */
				dataResponse, /* 10 - get response */
				dataResponse, /* 11 - put response */
				dataResponse, /* 12 - put-get response */
				dataResponse, /* 13 - monitor response */
				dataResponse, /* 14 - array response */
				badResponse, /* 15 - destroy request */
				dataResponse, /* 16 - process response */
				dataResponse, /* 17 - get field response */
				new MessageHandler(context), /* 18 - message to Requester */
				new MultipleDataResponseHandler(context), /* 19 - grouped monitors */
				dataResponse, /* 20 - RPC response */
				badResponse, /* 21 - cancel request */
				badResponse, /* 22 */
				badResponse, /* 23 */
				badResponse, /* 24 */
				badResponse, /* 25 */
				badResponse, /* 26 */
				badResponse, /* 27 */
			};
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.core.ResponseHandler#handleResponse(java.net.InetSocketAddress, org.epics.pvaccess.core.Transport, byte, byte, int, java.nio.ByteBuffer)
	 */
	public final void handleResponse(InetSocketAddress responseFrom, Transport transport, byte version, byte command, int payloadSize, ByteBuffer payloadBuffer) {
		
		if (command < 0 || command >= handlerTable.length)
		{
			context.getLogger().fine("Invalid (or unsupported) command: " + command + ".");
			// TODO remove debug output
			HexDump.hexDump("Invalid PVA header " + command + " + , its payload buffer", payloadBuffer.array(), payloadBuffer.position(), payloadSize);
			return;
		}
		
		// delegate
		handlerTable[command].handleResponse(responseFrom, transport, version, command, payloadSize, payloadBuffer);
	}

}
