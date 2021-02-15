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

import org.epics.pvaccess.client.ChannelPutGet;
import org.epics.pvaccess.client.ChannelPutGetRequester;
import org.epics.pvaccess.client.impl.remote.BaseRequestImpl;
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
 * Put-get handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 */
public class PutGetHandler extends AbstractServerResponseHandler {

	public PutGetHandler(ServerContextImpl context) {
		super(context, "Put-get request");
	}


	private static class ChannelPutGetRequesterImpl extends BaseChannelRequester implements ChannelPutGetRequester, TransportSender {

		private volatile ChannelPutGet channelPutGet;
		private volatile Status status;

		// reference store (for gets)
		private volatile PVStructure pvPutStructure;
		private volatile BitSet pvPutBitSet;
		private volatile PVStructure pvGetStructure;
		private volatile BitSet pvGetBitSet;

		// put container
		private volatile PVStructure pvPutGetStructure;
		private volatile BitSet pvPutGetBitSet;

		private volatile Structure putStructure;
		private volatile Structure getStructure;

		public ChannelPutGetRequesterImpl(ServerContextImpl context, ServerChannelImpl channel, int ioid, Transport transport,
				PVStructure pvRequest) {
			super(context, channel, ioid, transport);

			startRequest(QoS.INIT.getMaskValue());
			channel.registerRequest(ioid, this);

			try {
				channelPutGet = channel.getChannel().createChannelPutGet(this, pvRequest);
			} catch (Throwable th) {
				// simply cannot trust code above
				BaseChannelRequester.sendFailureMessage((byte)12, transport, ioid, (byte)QoS.INIT.getMaskValue(),
						statusCreate.createStatus(StatusType.FATAL, "Unexpected exception caught: " + th.getMessage(), th));
				destroy();
			}
		}

		public void channelPutGetConnect(Status status, ChannelPutGet channelPutGet,
				Structure putStructure, Structure getStructure) {

			this.status = status;
			this.channelPutGet = channelPutGet;
			this.putStructure = putStructure;
			this.getStructure = getStructure;

			if (status.isSuccess())
			{
				this.pvPutGetStructure = (PVStructure)BaseRequestImpl.reuseOrCreatePVField(putStructure, pvPutGetStructure);
				this.pvPutGetBitSet = BaseRequestImpl.createBitSetFor(pvPutGetStructure, pvPutGetBitSet);
			}

			transport.enqueueSendRequest(this);

			// self-destruction
			if (!status.isSuccess()) {
				destroy();
			}
		}

		public void getGetDone(Status status, ChannelPutGet channelPutGet,
				PVStructure pvGetStructure, BitSet pvGetBitSet) {
			this.status = status;
			this.pvGetStructure = pvGetStructure;
			this.pvGetBitSet = pvGetBitSet;

			// TODO should we check if pvStructure and bitSet are consistent/valid

			transport.enqueueSendRequest(this);
		}

		public void getPutDone(Status status, ChannelPutGet channelPutGet,
				PVStructure pvPutStructure, BitSet pvPutBitSet) {
			this.status = status;
			this.pvPutStructure = pvPutStructure;
			this.pvPutBitSet = pvPutBitSet;

			// TODO should we check if pvStructure and bitSet are consistent/valid

			transport.enqueueSendRequest(this);
		}

		public void putGetDone(Status status, ChannelPutGet channelPutGet,
				PVStructure pvGetStructure, BitSet pvGetBitSet) {
			this.status = status;
			this.pvGetStructure = pvGetStructure;
			this.pvGetBitSet = pvGetBitSet;

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

			if (channelPutGet != null)
				channelPutGet.destroy();
		}

		/**
		 * @return the channelPutGet
		 */
		public ChannelPutGet getChannelPutGet() {
			return channelPutGet;
		}

		public PVStructure getPVPutStructure() {
			return pvPutGetStructure;
		}

		public BitSet getPVPutBitSet() {
			return pvPutGetBitSet;
		}

		/* (non-Javadoc)
		 * @see org.epics.pvaccess.impl.remote.TransportSender#lock()
		 */
		public void lock() {
			// TODO
		}

		/* (non-Javadoc)
		 * @see org.epics.pvaccess.impl.remote.TransportSender#unlock()
		 */
		public void unlock() {
			// TODO
		}

		/* (non-Javadoc)
		 * @see org.epics.pvaccess.impl.remote.TransportSender#send(java.nio.ByteBuffer, org.epics.pvaccess.impl.remote.TransportSendControl)
		 */
		public void send(ByteBuffer buffer, TransportSendControl control) {
			final int request = getPendingRequest();

			control.startMessage((byte)12, Integer.SIZE/Byte.SIZE + 1);
			buffer.putInt(ioid);
			buffer.put((byte)request);
			status.serialize(buffer, control);

			if (status.isSuccess())
			{
				if (QoS.INIT.isSet(request))
				{
					control.cachedSerialize(putStructure, buffer);
					control.cachedSerialize(getStructure, buffer);
				}
				else if (QoS.GET.isSet(request))
				{
					pvGetBitSet.serialize(buffer, control);
					pvGetStructure.serialize(buffer, control, pvGetBitSet);

					// release references
					pvGetStructure = null;
					pvGetBitSet = null;
				}
				else if (QoS.GET_PUT.isSet(request))
				{
					pvPutBitSet.serialize(buffer, control);
					pvPutStructure.serialize(buffer, control, pvPutBitSet);

					// release references
					pvPutStructure = null;
					pvPutBitSet = null;
				}
				else
				{
					pvGetBitSet.serialize(buffer, control);
					pvGetStructure.serialize(buffer, control, pvGetBitSet);

					// release references
					pvGetStructure = null;
					pvGetBitSet = null;
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

		final byte qosCode = payloadBuffer.get();

		final ServerChannelImpl channel = (ServerChannelImpl)casTransport.getChannel(sid);
		if (channel == null) {
			BaseChannelRequester.sendFailureMessage((byte)12, transport, ioid, qosCode, BaseChannelRequester.badCIDStatus);
			return;
		}

		final boolean init = QoS.INIT.isSet(qosCode);
		if (init)
		{
			// pvRequest
		    final PVStructure pvRequest = SerializationHelper.deserializePVRequest(payloadBuffer, transport);

			// asCheck
			Status asStatus = channel.getChannelSecuritySession().authorizeCreateChannelPutGet(ioid, pvRequest);
			if (!asStatus.isSuccess())
			{
				BaseChannelRequester.sendFailureMessage((byte)12, transport, ioid, (byte)QoS.INIT.getMaskValue(), asStatus);
				return;
			}

			// create...
		    new ChannelPutGetRequesterImpl(context, channel, ioid, transport, pvRequest);
		}
		else
		{
			final boolean lastRequest = QoS.DESTROY.isSet(qosCode);
			final boolean getGet = QoS.GET.isSet(qosCode);
			final boolean getPut = QoS.GET_PUT.isSet(qosCode);

			ChannelPutGetRequesterImpl request = (ChannelPutGetRequesterImpl)channel.getRequest(ioid);
			if (request == null) {
				BaseChannelRequester.sendFailureMessage((byte)12, transport, ioid, qosCode, BaseChannelRequester.badIOIDStatus);
				return;
			}

			if (!request.startRequest(qosCode)) {
				BaseChannelRequester.sendFailureMessage((byte)12, transport, ioid, qosCode, BaseChannelRequester.otherRequestPendingStatus);
				return;
			}

			ChannelPutGet channelPutGet = request.getChannelPutGet();
			if (lastRequest)
				channelPutGet.lastRequest();

			if (getGet)
			{
				// asCheck
				Status asStatus = channel.getChannelSecuritySession().authorizeGet(ioid);
				if (!asStatus.isSuccess())
				{
					BaseChannelRequester.sendFailureMessage((byte)12, transport, ioid, qosCode, asStatus);
					if (lastRequest)
						request.destroy();
					return;
				}

				channelPutGet.getGet();
			}
			else if (getPut)
			{
				// asCheck
				Status asStatus = channel.getChannelSecuritySession().authorizeGet(ioid);
				if (!asStatus.isSuccess())
				{
					BaseChannelRequester.sendFailureMessage((byte)12, transport, ioid, qosCode, asStatus);
					if (lastRequest)
						request.destroy();
					return;
				}

				channelPutGet.getPut();
			}
			else
			{
				// deserialize put data
				final BitSet bitSet = request.getPVPutBitSet();
				final PVStructure pvStructure = request.getPVPutStructure();
				bitSet.deserialize(payloadBuffer, transport);
				pvStructure.deserialize(payloadBuffer, transport, bitSet);

				// asCheck
				Status asStatus = channel.getChannelSecuritySession().authorizePutGet(ioid, pvStructure, bitSet);
				if (!asStatus.isSuccess())
				{
					BaseChannelRequester.sendFailureMessage((byte)11, transport, ioid, qosCode, asStatus);
					if (lastRequest)
						request.destroy();
					return;
				}

				channelPutGet.putGet(pvStructure, bitSet);
			}
		}
	}
}
