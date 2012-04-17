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

package org.epics.pvaccess.server.impl.remote;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.request.ResponseHandler;
import org.epics.pvaccess.server.impl.remote.handlers.ArrayHandler;
import org.epics.pvaccess.server.impl.remote.handlers.BadResponse;
import org.epics.pvaccess.server.impl.remote.handlers.CancelRequestHandler;
import org.epics.pvaccess.server.impl.remote.handlers.ConnectionValidationHandler;
import org.epics.pvaccess.server.impl.remote.handlers.CreateChannelHandler;
import org.epics.pvaccess.server.impl.remote.handlers.DestroyChannelHandler;
import org.epics.pvaccess.server.impl.remote.handlers.EchoHandler;
import org.epics.pvaccess.server.impl.remote.handlers.GetFieldHandler;
import org.epics.pvaccess.server.impl.remote.handlers.GetHandler;
import org.epics.pvaccess.server.impl.remote.handlers.IntrospectionSearchHandler;
import org.epics.pvaccess.server.impl.remote.handlers.MonitorHandler;
import org.epics.pvaccess.server.impl.remote.handlers.NoopResponse;
import org.epics.pvaccess.server.impl.remote.handlers.ProcessHandler;
import org.epics.pvaccess.server.impl.remote.handlers.PutGetHandler;
import org.epics.pvaccess.server.impl.remote.handlers.PutHandler;
import org.epics.pvaccess.server.impl.remote.handlers.RPCHandler;
import org.epics.pvaccess.server.impl.remote.handlers.SearchHandler;
import org.epics.pvaccess.util.HexDump;

/**
 * CAS request handler - main handler which dispatches requests to appropriate handlers.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public final class ServerResponseHandler implements ResponseHandler {

	/**
	 * Table of response handlers for each command ID.
	 */
	private final ResponseHandler[] handlerTable;

	/**
	 * Context instance.
	 */
	private final ServerContextImpl context;
	
	/**
	 * @param context
	 */
	public ServerResponseHandler(ServerContextImpl context) {
		this.context = context;

		final ResponseHandler badResponse = new BadResponse(context);
		
		handlerTable = new ResponseHandler[]
			{
				new NoopResponse(context, "Beacon"), /*  0 */
				new ConnectionValidationHandler(context), /*  1 */
				new EchoHandler(context), /*  2 */
				new SearchHandler(context), /*  3 */
				badResponse, /*  4 */
				new IntrospectionSearchHandler(context), /*  5 */
				badResponse, /*  6 */
				new CreateChannelHandler(context), /*  7 */
				new DestroyChannelHandler(context), /*  8 */
				badResponse, /*  9 */
				new GetHandler(context), /* 10 */
				new PutHandler(context), /* 11 */
				new PutGetHandler(context), /* 12 */
				new MonitorHandler(context), /* 13 */
				new ArrayHandler(context), /* 14 */
				new CancelRequestHandler(context), /* 15 */
				new ProcessHandler(context), /* 16 */
				new GetFieldHandler(context), /* 17 */
				badResponse, /* 18 */
				badResponse, /* 19 */
				new RPCHandler(context), /* 20 */
				badResponse, /* 21 */
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
			HexDump.hexDump("Invalid CA header " + command + " + , its payload buffer", payloadBuffer.array(), payloadBuffer.position(), payloadSize);
			return;
		}
		
		// delegate
		handlerTable[command].handleResponse(responseFrom, transport, version, command, payloadSize, payloadBuffer);
	}

}
