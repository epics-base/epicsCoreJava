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

import org.epics.pvaccess.client.ChannelArray;
import org.epics.pvaccess.client.ChannelArrayRequester;
import org.epics.pvaccess.client.impl.remote.BaseRequestImpl;
import org.epics.pvaccess.impl.remote.QoS;
import org.epics.pvaccess.impl.remote.SerializationHelper;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.TransportSender;
import org.epics.pvaccess.impl.remote.server.ChannelHostingTransport;
import org.epics.pvaccess.server.impl.remote.ServerChannelImpl;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.Array;
import org.epics.pvdata.pv.PVArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;

/**
 * Array request handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ArrayHandler extends AbstractServerResponseHandler {

	private static final Status fixedArrayErrorStatus =
			StatusFactory.getStatusCreate().createStatus(
					StatusType.ERROR,
					"fixed sized array returned as a ChannelArray array instance",
					null);

	public ArrayHandler(ServerContextImpl context) {
		super(context, "Array request");
	}

	private static class ChannelArrayRequesterImpl extends BaseChannelRequester implements ChannelArrayRequester, TransportSender {

		private volatile ChannelArray channelArray;

		private volatile int length;
		private volatile Status status;

		// data container
		private volatile PVArray pvPutArray;

		// reference store
		private volatile PVArray pvArray;

		private volatile Array array;

		public ChannelArrayRequesterImpl(ServerContextImpl context, ServerChannelImpl channel, int ioid, Transport transport,
										 PVStructure pvRequest) {
			super(context, channel, ioid, transport);

			startRequest(QoS.INIT.getMaskValue());
			channel.registerRequest(ioid, this);

			try {
				channelArray = channel.getChannel().createChannelArray(this, pvRequest);
			} catch (Throwable th) {
				// simply cannot trust code above
				BaseChannelRequester.sendFailureMessage((byte)14, transport, ioid, (byte)QoS.INIT.getMaskValue(),
						statusCreate.createStatus(StatusType.FATAL, "Unexpected exception caught: " + th.getMessage(), th));
				destroy();
			}
		}

		public void channelArrayConnect(Status status, ChannelArray channelArray, Array array) {

			if (status.isSuccess() && array.getArraySizeType() == Array.ArraySizeType.fixed)
			{
				this.status = fixedArrayErrorStatus;
				this.channelArray = null;
				this.array = null;
			}
			else
			{
				this.status = status;
				this.channelArray = channelArray;
				this.array = array;
			}

			if (status.isSuccess())
			{
				this.pvPutArray = (PVArray)BaseRequestImpl.reuseOrCreatePVField(array, pvPutArray);
			}

			transport.enqueueSendRequest(this);

			// self-destruction
			if (!status.isSuccess()) {
				destroy();
			}
		}

		public void getArrayDone(Status status, ChannelArray channelArray, PVArray pvArray) {
			this.status = status;
			this.pvArray = pvArray;

			transport.enqueueSendRequest(this);
		}

		public void putArrayDone(Status status, ChannelArray channelArray) {
			this.status = status;

			transport.enqueueSendRequest(this);
		}

		public void setLengthDone(Status status, ChannelArray channelArray) {
			this.status = status;

			transport.enqueueSendRequest(this);
		}

		public void getLengthDone(Status status, ChannelArray channelArray,
				int length) {
			this.status = status;
			this.length = length;

			transport.enqueueSendRequest(this);
		}

		/* (non-Javadoc)
		 * @see org.epics.pvdata.misc.Destroyable#destroy()
		 */
		public void destroy() {
			channel.unregisterRequest(ioid);

			// asCheck
			channel.getChannelSecuritySession().release(ioid);

			if (channelArray != null)
				channelArray.destroy();
		}

		/**
		 * @return the channelArray
		 */
		public ChannelArray getChannelArray() {
			return channelArray;
		}

		/**
		 * @return the pvArray
		 */
		public PVArray getPVArray() {
			return pvPutArray;
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

			control.startMessage((byte)14, Integer.SIZE/Byte.SIZE + 1);
			buffer.putInt(ioid);
			buffer.put((byte)request);
			status.serialize(buffer, control);

			if (status.isSuccess())
			{
				if (QoS.GET.isSet(request))
				{
					pvArray.serialize(buffer, control);
					pvArray = null;
				}
				else if (QoS.PROCESS.isSet(request))
				{
					SerializeHelper.writeSize(length, buffer, control);
				}
				else if (QoS.INIT.isSet(request))
				{
					control.cachedSerialize(array, buffer);
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
		ChannelHostingTransport casTransport = (ChannelHostingTransport)transport;

		transport.ensureData(2*Integer.SIZE/Byte.SIZE+1);
		final int sid = payloadBuffer.getInt();
		final int ioid = payloadBuffer.getInt();

		// mode
		final byte qosCode = payloadBuffer.get();

		final ServerChannelImpl channel = (ServerChannelImpl)casTransport.getChannel(sid);
		if (channel == null) {
			BaseChannelRequester.sendFailureMessage((byte)14, transport, ioid, qosCode, BaseChannelRequester.badCIDStatus);
			return;
		}

		final boolean init = QoS.INIT.isSet(qosCode);
		if (init)
		{
		    // pvRequest data
		    final PVStructure pvRequest = SerializationHelper.deserializePVRequest(payloadBuffer, transport);

			// asCheck
			Status asStatus = channel.getChannelSecuritySession().authorizeCreateChannelGet(ioid, pvRequest);
			if (!asStatus.isSuccess())
			{
				BaseChannelRequester.sendFailureMessage((byte)14, transport, ioid, (byte)QoS.INIT.getMaskValue(), asStatus);
				return;
			}

			// create...
		    new ChannelArrayRequesterImpl(context, channel, ioid, transport, pvRequest);
		}
		else
		{
			final boolean lastRequest = QoS.DESTROY.isSet(qosCode);
			final boolean get = QoS.GET.isSet(qosCode);
			final boolean setLength = QoS.GET_PUT.isSet(qosCode);
			final boolean getLength = QoS.PROCESS.isSet(qosCode);

			ChannelArrayRequesterImpl request = (ChannelArrayRequesterImpl)channel.getRequest(ioid);
			if (request == null) {
				BaseChannelRequester.sendFailureMessage((byte)14, transport, ioid, qosCode, BaseChannelRequester.badIOIDStatus);
				return;
			}

			if (!request.startRequest(qosCode)) {
				BaseChannelRequester.sendFailureMessage((byte)14, transport, ioid, qosCode, BaseChannelRequester.otherRequestPendingStatus);
				return;
			}

			ChannelArray channelArray = request.getChannelArray();
			if (lastRequest)
				channelArray.lastRequest();

			if (get)
			{
				final int offset = SerializeHelper.readSize(payloadBuffer, transport);
				final int count = SerializeHelper.readSize(payloadBuffer, transport);
				final int stride = SerializeHelper.readSize(payloadBuffer, transport);

				// asCheck
				Status asStatus = channel.getChannelSecuritySession().authorizeGet(ioid);
				if (!asStatus.isSuccess())
				{
					BaseChannelRequester.sendFailureMessage((byte)14, transport, ioid, qosCode, asStatus);
					if (lastRequest)
						request.destroy();
					return;
				}

				channelArray.getArray(offset, count, stride);
			}
			else if (setLength)
			{
				final int length = SerializeHelper.readSize(payloadBuffer, transport);

				// asCheck
				Status asStatus = channel.getChannelSecuritySession().authorizeSetLength(ioid);
				if (!asStatus.isSuccess())
				{
					BaseChannelRequester.sendFailureMessage((byte)14, transport, ioid, qosCode, asStatus);
					if (lastRequest)
						request.destroy();
					return;
				}

				channelArray.setLength(length);
			}
			else if (getLength)
			{
				// asCheck
				Status asStatus = channel.getChannelSecuritySession().authorizeGet(ioid);
				if (!asStatus.isSuccess())
				{
					BaseChannelRequester.sendFailureMessage((byte)14, transport, ioid, qosCode, asStatus);
					if (lastRequest)
						request.destroy();
					return;
				}

				channelArray.getLength();
			}
			else
			{
				// deserialize data to put
				final int offset = SerializeHelper.readSize(payloadBuffer, transport);
				final int stride = SerializeHelper.readSize(payloadBuffer, transport);
				// no count, we do not want to send extra data
				final PVArray array = request.getPVArray();
				array.deserialize(payloadBuffer, transport);

				// asCheck
				Status asStatus = channel.getChannelSecuritySession().authorizePut(ioid, array);
				if (!asStatus.isSuccess())
				{
					BaseChannelRequester.sendFailureMessage((byte)14, transport, ioid, qosCode, asStatus);
					if (lastRequest)
						request.destroy();
					return;
				}

				channelArray.putArray(array, offset, array.getLength(), stride);
			}
		}
	}

}
