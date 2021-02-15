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

import org.epics.pvaccess.client.ChannelGet;
import org.epics.pvaccess.client.ChannelGetRequester;
import org.epics.pvaccess.impl.remote.QoS;
import org.epics.pvaccess.impl.remote.SerializationHelper;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.TransportSender;
import org.epics.pvaccess.impl.remote.server.ChannelHostingTransport;
import org.epics.pvaccess.server.impl.remote.ServerChannelImpl;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.Structure;

/**
 * Get request handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class GetHandler extends AbstractServerResponseHandler {

	public GetHandler(ServerContextImpl context) {
		super(context, "Get request");
	}

	private static class ChannelGetRequesterImpl extends BaseChannelRequester implements ChannelGetRequester, TransportSender {

		private volatile ChannelGet channelGet;
		private volatile BitSet bitSet;
		private volatile PVStructure pvStructure;
		private volatile Structure structure;
		private volatile Status status;

		public ChannelGetRequesterImpl(ServerContextImpl context, ServerChannelImpl channel, int ioid, Transport transport,
				 PVStructure pvRequest) {
			super(context, channel, ioid, transport);

			startRequest(QoS.INIT.getMaskValue());
			channel.registerRequest(ioid, this);

			try {
				channelGet = channel.getChannel().createChannelGet(this, pvRequest);
			} catch (Throwable th) {
				// simply cannot trust code above
				BaseChannelRequester.sendFailureMessage((byte)10, transport, ioid, (byte)QoS.INIT.getMaskValue(),
						statusCreate.createStatus(StatusType.FATAL, "Unexpected exception caught: " + th.getMessage(), th));
				destroy();
			}
		}

		public void channelGetConnect(Status status, ChannelGet channelGet, Structure structure) {
			// will JVM optimize subsequent volatile sets?
			this.status = status;
			this.channelGet = channelGet;
			this.structure = structure;

			transport.enqueueSendRequest(this);

			// self-destruction
			if (!status.isSuccess()) {
				destroy();
			}
		}

		public void getDone(Status status, ChannelGet channelGet, PVStructure pvStructure, BitSet bitSet) {
			// will JVM optimize subsequent volatile sets?
			this.status = status;
			this.pvStructure = pvStructure;
			this.bitSet = bitSet;

			// TODO should we check if pvStructure and bitSet are consistent/valid

			transport.enqueueSendRequest(this);
		}

		/* (non-Javadoc)
		 * @see org.epics.pvdata.misc.Destroyable#destroy()
		 */
		public void destroy() {
			channel.unregisterRequest(ioid);

			// asCheck
			channel.getChannelSecuritySession().release(ioid);

			if (channelGet != null)
				channelGet.destroy();
		}

		/**
		 * @return the channelGet
		 */
		public ChannelGet getChannelGet() {
			return channelGet;
		}

		/* (non-Javadoc)
		 * @see org.epics.pvaccess.impl.remote.TransportSender#lock()
		 */
		public void lock() {
			// noop
		}

		/* (non-Javadoc)
		 * @see org.epics.pvaccess.impl.remote.TransportSender#unlock()
		 */
		public void unlock() {
			// noop
		}

		/* (non-Javadoc)
		 * @see org.epics.pvaccess.impl.remote.TransportSender#send(java.nio.ByteBuffer, org.epics.pvaccess.impl.remote.TransportSendControl)
		 */
		public void send(ByteBuffer buffer, TransportSendControl control) {
			final int request = getPendingRequest();

			control.startMessage((byte)10, Integer.SIZE/Byte.SIZE + 1);
			buffer.putInt(ioid);
			buffer.put((byte)request);
			status.serialize(buffer, control);

			if (status.isSuccess())
			{
				if (QoS.INIT.isSet(request))
				{
					control.cachedSerialize(structure, buffer);
				}
				else
				{
					bitSet.serialize(buffer, control);
					pvStructure.serialize(buffer, control, bitSet);

					// release references
					pvStructure = null;
					bitSet = null;
				}
			}

			stopRequest();

			// lastRequest
			if (QoS.DESTROY.isSet(request))
				destroy();
		}
	};

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.AbstractResponseHandler#handleResponse(java.net.InetSocketAddress, org.epics.pvaccess.core.Transport, byte, byte, int, java.nio.ByteBuffer)
	 */
	@Override
	public void handleResponse(InetSocketAddress responseFrom, final Transport transport, byte version, byte command, int payloadSize, ByteBuffer payloadBuffer) {
		super.handleResponse(responseFrom, transport, version, command, payloadSize, payloadBuffer);

		// NOTE: we do not explicitly check if transport is OK
		final ChannelHostingTransport casTransport = (ChannelHostingTransport)transport;

		transport.ensureData(2*Integer.SIZE/Byte.SIZE+1);
		final int sid = payloadBuffer.getInt();
		final int ioid = payloadBuffer.getInt();

		// mode
		final byte qosCode = payloadBuffer.get();

		final ServerChannelImpl channel = (ServerChannelImpl)casTransport.getChannel(sid);
		if (channel == null) {
			BaseChannelRequester.sendFailureMessage((byte)10, transport, ioid, qosCode, BaseChannelRequester.badCIDStatus);
			return;
		}

		final boolean init = QoS.INIT.isSet(qosCode);
		if (init)
		{
			// pvRequest
		    final PVStructure pvRequest = SerializationHelper.deserializePVRequest(payloadBuffer, transport);

			// asCheck
			Status asStatus = channel.getChannelSecuritySession().authorizeCreateChannelGet(ioid, pvRequest);
			if (!asStatus.isSuccess())
			{
				BaseChannelRequester.sendFailureMessage((byte)10, transport, ioid, (byte)QoS.INIT.getMaskValue(), asStatus);
				return;
			}

			// create...
			new ChannelGetRequesterImpl(context, channel, ioid, transport, pvRequest);
		}
		else
		{
			final boolean lastRequest = QoS.DESTROY.isSet(qosCode);

			ChannelGetRequesterImpl request = (ChannelGetRequesterImpl)channel.getRequest(ioid);
			if (request == null) {
				BaseChannelRequester.sendFailureMessage((byte)10, transport, ioid, qosCode, BaseChannelRequester.badIOIDStatus);
				return;
			}

			if (!request.startRequest(qosCode)) {
				BaseChannelRequester.sendFailureMessage((byte)10, transport, ioid, qosCode, BaseChannelRequester.otherRequestPendingStatus);
				return;
			}

			// asCheck
			Status asStatus = channel.getChannelSecuritySession().authorizeGet(ioid);
			if (!asStatus.isSuccess())
			{
				BaseChannelRequester.sendFailureMessage((byte)10, transport, ioid, qosCode, asStatus);
				if (lastRequest)
					request.destroy();
				return;
			}

			ChannelGet channelGet = request.getChannelGet();

			if (lastRequest)
				channelGet.lastRequest();

			channelGet.get();
		}
	}

}
