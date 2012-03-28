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

import org.epics.ca.client.GetFieldRequester;
import org.epics.ca.impl.remote.Transport;
import org.epics.ca.impl.remote.TransportSendControl;
import org.epics.ca.impl.remote.TransportSender;
import org.epics.ca.impl.remote.server.ChannelHostingTransport;
import org.epics.ca.server.impl.remote.ServerChannelImpl;
import org.epics.ca.server.impl.remote.ServerContextImpl;
import org.epics.pvData.misc.SerializeHelper;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.Status;

/**
 * Get field request handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class GetFieldHandler extends AbstractServerResponseHandler {

	/**
	 * @param context
	 */
	public GetFieldHandler(ServerContextImpl context) {
		super(context, "Get field request");
	}

	private static class GetFieldRequesterImpl extends BaseChannelRequester implements GetFieldRequester, TransportSender {
		
		private Status status;
		private Field field;
		
		public GetFieldRequesterImpl(ServerContextImpl context, ServerChannelImpl channel, int ioid, Transport transport) {
			super(context, channel, ioid, transport);
		}
		
		@Override
		public void getDone(Status status, Field field) {
			synchronized (this) {
				this.status = status;
				this.field = field;
			}
			transport.enqueueSendRequest(this);
		}

		@Override
		public void destroy() {
			// noop
		}

		/* (non-Javadoc)
		 * @see org.epics.ca.impl.remote.TransportSender#lock()
		 */
		@Override
		public void lock() {
			// noop
		}

		/* (non-Javadoc)
		 * @see org.epics.ca.impl.remote.TransportSender#unlock()
		 */
		@Override
		public void unlock() {
			// noop
		}

		/* (non-Javadoc)
		 * @see org.epics.ca.impl.remote.TransportSender#send(java.nio.ByteBuffer, org.epics.ca.impl.remote.TransportSendControl)
		 */
		@Override
		public void send(ByteBuffer buffer, TransportSendControl control) {
			control.startMessage((byte)17, Integer.SIZE/Byte.SIZE);
			buffer.putInt(ioid);
			synchronized (this) {
				status.serialize(buffer, control);
				control.cachedSerialize(field, buffer);
			}
		}

	};

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.AbstractResponseHandler#handleResponse(java.net.InetSocketAddress, org.epics.ca.core.Transport, byte, byte, int, java.nio.ByteBuffer)
	 */
	@Override
	public void handleResponse(InetSocketAddress responseFrom, final Transport transport, byte version, byte command, int payloadSize, ByteBuffer payloadBuffer) {
		super.handleResponse(responseFrom, transport, version, command, payloadSize, payloadBuffer);

		// NOTE: we do not explicitly check if transport is OK
		final ChannelHostingTransport casTransport = (ChannelHostingTransport)transport;

		transport.ensureData(2*Integer.SIZE/Byte.SIZE);
		final int sid = payloadBuffer.getInt();
		final int ioid = payloadBuffer.getInt();

		final ServerChannelImpl channel = (ServerChannelImpl)casTransport.getChannel(sid);
		if (channel == null) {
			getFieldFailureResponse(transport, ioid, BaseChannelRequester.badCIDStatus);
			return;
		}
			
		final String subField = SerializeHelper.deserializeString(payloadBuffer, transport);

		// issue request
		channel.getChannel().getField(new GetFieldRequesterImpl(context, channel, ioid, transport), subField);
	}

	/**
	 * @param transport
	 * @param ioid
	 * @param errorStatus
	 */
	private void getFieldFailureResponse(final Transport transport, final int ioid, final Status errorStatus)
	{
		transport.enqueueSendRequest(
				new TransportSender() {

					@Override
					public void send(ByteBuffer buffer, TransportSendControl control) {
						control.startMessage((byte)17, Integer.SIZE/Byte.SIZE);
						buffer.putInt(ioid);
						errorStatus.serialize(buffer, control);
					}

					@Override
					public void lock() {
						// noop
					}

					@Override
					public void unlock() {
						// noop
					}
					
			});
	}
}
