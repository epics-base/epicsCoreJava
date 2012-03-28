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

import org.epics.ca.client.ChannelArray;
import org.epics.ca.client.ChannelArrayRequester;
import org.epics.ca.impl.remote.QoS;
import org.epics.ca.impl.remote.SerializationHelper;
import org.epics.ca.impl.remote.Transport;
import org.epics.ca.impl.remote.TransportSendControl;
import org.epics.ca.impl.remote.TransportSender;
import org.epics.ca.impl.remote.server.ChannelHostingTransport;
import org.epics.ca.server.impl.remote.ServerChannelImpl;
import org.epics.ca.server.impl.remote.ServerContextImpl;
import org.epics.pvData.misc.SerializeHelper;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.Status.StatusType;

/**
 * Array request handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ArrayHandler extends AbstractServerResponseHandler {

	/**
	 * @param context
	 */
	public ArrayHandler(ServerContextImpl context) {
		super(context, "Array request");
	}

	private static class ChannelArrayRequesterImpl extends BaseChannelRequester implements ChannelArrayRequester, TransportSender {
		
		private volatile ChannelArray channelArray;
		
		private volatile PVArray pvArray;
		private Status status;
		
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
		
		/* (non-Javadoc)
		 * @see org.epics.ca.client.ChannelArrayRequester#channelArrayConnect(Status, org.epics.ca.client.ChannelArray, org.epics.pvData.pv.PVArray)
		 */
		@Override
		public void channelArrayConnect(Status status, ChannelArray channelArray, PVArray pvArray) {
			synchronized (this) {
				this.status = status;
				this.pvArray = pvArray;
				this.channelArray = channelArray;
			}
			transport.enqueueSendRequest(this);
			
			// self-destruction
			if (!status.isSuccess()) {
				destroy();
			}
		}

		/* (non-Javadoc)
		 * @see org.epics.ca.client.ChannelArrayRequester#getArrayDone(Status)
		 */
		@Override
		public void getArrayDone(Status status) {
			synchronized (this) {
				this.status = status;
			}
			transport.enqueueSendRequest(this);
		}

		/* (non-Javadoc)
		 * @see org.epics.ca.client.ChannelArrayRequester#putArrayDone(Status)
		 */
		@Override
		public void putArrayDone(Status status) {
			synchronized (this) {
				this.status = status;
			}
			transport.enqueueSendRequest(this);
		}

		/* (non-Javadoc)
		 * @see org.epics.ca.client.ChannelArrayRequester#setLengthDone(org.epics.pvData.pv.Status)
		 */
		@Override
		public void setLengthDone(Status status) {
			synchronized (this) {
				this.status = status;
			}
			transport.enqueueSendRequest(this);
		}

		/* (non-Javadoc)
		 * @see org.epics.pvData.misc.Destroyable#destroy()
		 */
		@Override
		public void destroy() {
			channel.unregisterRequest(ioid);
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
			return pvArray;
		}
		
		/* (non-Javadoc)
		 * @see org.epics.ca.impl.remote.TransportSender#lock()
		 */
		@Override
		public void lock() {
			// TODO
		}

		/* (non-Javadoc)
		 * @see org.epics.ca.impl.remote.TransportSender#unlock()
		 */
		@Override
		public void unlock() {
			// TODO
		}

		/* (non-Javadoc)
		 * @see org.epics.ca.impl.remote.TransportSender#send(java.nio.ByteBuffer, org.epics.ca.impl.remote.TransportSendControl)
		 */
		@Override
		public void send(ByteBuffer buffer, TransportSendControl control) {
			final int request = getPendingRequest();

			control.startMessage((byte)14, Integer.SIZE/Byte.SIZE + 1);
			buffer.putInt(ioid);
			buffer.put((byte)request);
			synchronized (this) {
				status.serialize(buffer, control);
			}

			if (status.isSuccess())
			{
				if (QoS.GET.isSet(request))
				{
					pvArray.serialize(buffer, control);
				}
				else if (QoS.INIT.isSet(request))
				{
					control.cachedSerialize(pvArray != null ? pvArray.getField() : null, buffer);
				}
			}
			
			stopRequest();
			
			// lastRequest
			if (QoS.DESTROY.isSet(request))
				destroy();
		}
	};

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.AbstractResponseHandler#handleResponse(java.net.InetSocketAddress, org.epics.ca.core.Transport, byte, byte, int, java.nio.ByteBuffer)
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

			// create...
		    new ChannelArrayRequesterImpl(context, channel, ioid, transport, pvRequest);
		}
		else
		{
			final boolean lastRequest = QoS.DESTROY.isSet(qosCode);
			final boolean get = QoS.GET.isSet(qosCode);
			final boolean setLength = QoS.GET_PUT.isSet(qosCode);
			
			ChannelArrayRequesterImpl request = (ChannelArrayRequesterImpl)channel.getRequest(ioid);
			if (request == null) {
				BaseChannelRequester.sendFailureMessage((byte)14, transport, ioid, qosCode, BaseChannelRequester.badIOIDStatus);
				return;
			}

			if (!request.startRequest(qosCode)) {
				BaseChannelRequester.sendFailureMessage((byte)14, transport, ioid, qosCode, BaseChannelRequester.otherRequestPendingStatus);
				return;
			}
	

			if (get)
			{
				final int offset = SerializeHelper.readSize(payloadBuffer, transport);
				final int count = SerializeHelper.readSize(payloadBuffer, transport);
				request.getChannelArray().getArray(lastRequest, offset, count);
			}
			else if (setLength)
			{
				final int length = SerializeHelper.readSize(payloadBuffer, transport);
				final int capacity = SerializeHelper.readSize(payloadBuffer, transport);
				request.getChannelArray().setLength(lastRequest, length, capacity);
			}
			else
			{
				// deserialize data to put
				final int offset = SerializeHelper.readSize(payloadBuffer, transport);
				final PVArray array = request.getPVArray();
				array.deserialize(payloadBuffer, transport);
				request.getChannelArray().putArray(lastRequest, offset, array.getLength());
			}
		}
	}

}
