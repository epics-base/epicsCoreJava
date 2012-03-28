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

import org.epics.ca.client.ChannelPut;
import org.epics.ca.client.ChannelPutRequester;
import org.epics.ca.impl.remote.QoS;
import org.epics.ca.impl.remote.SerializationHelper;
import org.epics.ca.impl.remote.Transport;
import org.epics.ca.impl.remote.TransportSendControl;
import org.epics.ca.impl.remote.TransportSender;
import org.epics.ca.impl.remote.server.ChannelHostingTransport;
import org.epics.ca.server.impl.remote.ServerChannelImpl;
import org.epics.ca.server.impl.remote.ServerContextImpl;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.Status.StatusType;

/**
 * Put request handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class PutHandler extends AbstractServerResponseHandler {

	/**
	 * @param context
	 */
	public PutHandler(ServerContextImpl context) {
		super(context, "Put request");
	}

	private static class ChannelPutRequesterImpl extends BaseChannelRequester implements ChannelPutRequester, TransportSender {
		
		private volatile ChannelPut channelPut;
		private volatile BitSet bitSet;
		private volatile PVStructure pvStructure;
		private Status status;
		
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

		@Override
		public void channelPutConnect(Status status, ChannelPut channelPut, PVStructure pvStructure, BitSet bitSet) {
			synchronized (this) {
				this.bitSet = bitSet;
				this.pvStructure = pvStructure;
				this.status = status;
				this.channelPut = channelPut;
			}
			transport.enqueueSendRequest(this);

			// self-destruction
			if (!status.isSuccess()) {
				destroy();
			}
		}

		@Override
		public void putDone(Status status) {
			synchronized (this)
			{
				this.status = status;
			}
			transport.enqueueSendRequest(this);
		}

		@Override
		public void getDone(Status status) {
			synchronized (this)
			{
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
		public BitSet getBitSet() {
			return bitSet;
		}

		/**
		 * @return the pvStructure
		 */
		public PVStructure getPVStructure() {
			return pvStructure;
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

			control.startMessage((byte)11, Integer.SIZE/Byte.SIZE + 1);
			buffer.putInt(ioid);
			buffer.put((byte)request);
			synchronized (this) {
				status.serialize(buffer, control);
			}

			if (status.isSuccess())
			{
				if (QoS.INIT.isSet(request))
				{
					control.cachedSerialize(pvStructure != null ? pvStructure.getField() : null, buffer);
				}
				else if (QoS.GET.isSet(request))
				{
					pvStructure.serialize(buffer, control);
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
			/*
			// check process access rights
			if (process && !AccessRights.PROCESS.isSet(channel.getAccessRights()))
			{
				putResponse(transport, ioid, qosCode, BaseChannelRequester.noReadACLStatus);
				return;
			}
			*/

			// pvRequest
		    final PVStructure pvRequest = SerializationHelper.deserializePVRequest(payloadBuffer, transport);
	
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

			if (get)
			{
				/*
				// check read access rights
				if (!AccessRights.READ.isSet(channel.getAccessRights()))
				{
					putResponse(transport, ioid, qosCode, BaseChannelRequester.noReadACLStatus);
					if (lastRequest)
						request.destroy();
					return;
				}
				*/

				// no destroy w/ get
				request.getChannelPut().get();
			}
			else
			{
				/*
				// check write access rights
				if (!AccessRights.WRITE.isSet(channel.getAccessRights()))
				{
					putResponse(transport, ioid, qosCode, BaseChannelRequester.noWriteACLStatus);
					if (lastRequest)
						request.destroy();
					return;
				}
				*/
				
				// deserialize bitSet and do a put
				final BitSet putBitSet = request.getBitSet();
				putBitSet.deserialize(payloadBuffer, transport);
				request.getPVStructure().deserialize(payloadBuffer, transport, putBitSet);
				request.getChannelPut().put(lastRequest);
			}
		}
	}

}
