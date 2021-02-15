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

import org.epics.pvaccess.client.ChannelPut;
import org.epics.pvaccess.client.ChannelPutRequester;
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
 * Put request handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 */
public class PutHandler extends AbstractServerResponseHandler {

	public PutHandler(ServerContextImpl context) {
		super(context, "Put request");
	}

	private static class ChannelPutRequesterImpl extends BaseChannelRequester implements ChannelPutRequester, TransportSender {

		private volatile ChannelPut channelPut;
		private volatile Status status;

		private volatile Structure structure;

		// reference store for get
		private volatile PVStructure pvStructure;
		private volatile BitSet bitSet;

		// put container
		private volatile PVStructure putPVStructure;
		private volatile BitSet putBitSet;

		public ChannelPutRequesterImpl(ServerContextImpl context, ServerChannelImpl channel, int ioid, Transport transport,
				 PVStructure pvRequest) {
			super(context, channel, ioid, transport);

			startRequest(QoS.INIT.getMaskValue());
			channel.registerRequest(ioid, this);

			try {
				channelPut = channel.getChannel().createChannelPut(this, pvRequest);
			} catch (Throwable th) {
				// simply cannot trust code above
				BaseChannelRequester.sendFailureMessage((byte)11, transport, ioid, (byte)QoS.INIT.getMaskValue(),
						statusCreate.createStatus(StatusType.FATAL, "Unexpected exception caught: " + th.getMessage(), th));
				destroy();
			}
		}

		public void channelPutConnect(Status status, ChannelPut channelPut, Structure structure) {
			// will JVM optimize subsequent volatile sets?
			this.status = status;
			this.channelPut = channelPut;
			this.structure = structure;

			if (status.isSuccess())
			{
				this.putPVStructure = (PVStructure)BaseRequestImpl.reuseOrCreatePVField(structure, putPVStructure);
				this.putBitSet = BaseRequestImpl.createBitSetFor(putPVStructure, putBitSet);
			}

			transport.enqueueSendRequest(this);

			// self-destruction
			if (!status.isSuccess()) {
				destroy();
			}
		}

		public void putDone(Status status, ChannelPut channelPut) {
			this.status = status;

			transport.enqueueSendRequest(this);
		}

		public void getDone(Status status, ChannelPut channelPut, PVStructure pvStructure, BitSet bitSet) {
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

			if (channelPut != null)
				channelPut.destroy();
		}

		/**
		 * @return the channelPut
		 */
		public ChannelPut getChannelPut() {
			return channelPut;
		}

		/**
		 * @return the bitSet
		 */
		public BitSet getPutBitSet() {
			return putBitSet;
		}

		/**
		 * @return the pvStructure
		 */
		public PVStructure getPutPVStructure() {
			return putPVStructure;
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

			control.startMessage((byte)11, Integer.SIZE/Byte.SIZE + 1);
			buffer.putInt(ioid);
			buffer.put((byte)request);
			status.serialize(buffer, control);

			if (status.isSuccess())
			{
				if (QoS.INIT.isSet(request))
				{
					control.cachedSerialize(structure, buffer);
				}
				else if (QoS.GET.isSet(request))
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
			BaseChannelRequester.sendFailureMessage((byte)11, transport, ioid, qosCode, BaseChannelRequester.badCIDStatus);
			return;
		}

		final boolean init = QoS.INIT.isSet(qosCode);
		if (init)
		{
			// pvRequest
		    final PVStructure pvRequest = SerializationHelper.deserializePVRequest(payloadBuffer, transport);

		    // asCheck
			Status asStatus = channel.getChannelSecuritySession().authorizeCreateChannelPut(ioid, pvRequest);
			if (!asStatus.isSuccess())
			{
				BaseChannelRequester.sendFailureMessage((byte)11, transport, ioid, (byte)QoS.INIT.getMaskValue(), asStatus);
				return;
			}

			// create...
		    new ChannelPutRequesterImpl(context, channel, ioid, transport, pvRequest);
		}
		else
		{
			final boolean lastRequest = QoS.DESTROY.isSet(qosCode);
			final boolean get = QoS.GET.isSet(qosCode);

			ChannelPutRequesterImpl request = (ChannelPutRequesterImpl)channel.getRequest(ioid);
			if (request == null) {
				BaseChannelRequester.sendFailureMessage((byte)11, transport, ioid, qosCode, BaseChannelRequester.badIOIDStatus);
				return;
			}

			if (!request.startRequest(qosCode)) {
				BaseChannelRequester.sendFailureMessage((byte)11, transport, ioid, qosCode, BaseChannelRequester.otherRequestPendingStatus);
				return;
			}

			ChannelPut channelPut = request.getChannelPut();

			if (lastRequest)
				channelPut.lastRequest();

			if (get)
			{
				// asCheck
				Status asStatus = channel.getChannelSecuritySession().authorizeGet(ioid);
				if (!asStatus.isSuccess())
				{
					BaseChannelRequester.sendFailureMessage((byte)11, transport, ioid, qosCode, asStatus);
					if (lastRequest)
						request.destroy();
					return;
				}

				channelPut.get();
			}
			else
			{
				// deserialize bitSet and do a put
				final BitSet putBitSet = request.getPutBitSet();
				final PVStructure putPVStructure = request.getPutPVStructure();
				putBitSet.deserialize(payloadBuffer, transport);
				putPVStructure.deserialize(payloadBuffer, transport, putBitSet);

				// asCheck
				Status asStatus = channel.getChannelSecuritySession().authorizePut(ioid, putPVStructure, putBitSet);
				if (!asStatus.isSuccess())
				{
					BaseChannelRequester.sendFailureMessage((byte)11, transport, ioid, qosCode, asStatus);
					if (lastRequest)
						request.destroy();
					return;
				}

				channelPut.put(putPVStructure, putBitSet);
			}
		}
	}

}
