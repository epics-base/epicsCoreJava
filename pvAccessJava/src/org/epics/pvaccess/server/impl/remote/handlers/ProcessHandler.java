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

import org.epics.pvaccess.client.ChannelProcess;
import org.epics.pvaccess.client.ChannelProcessRequester;
import org.epics.pvaccess.impl.remote.QoS;
import org.epics.pvaccess.impl.remote.SerializationHelper;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.TransportSender;
import org.epics.pvaccess.impl.remote.server.ChannelHostingTransport;
import org.epics.pvaccess.server.impl.remote.ServerChannelImpl;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;

/**
 * Process request handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 */
public class ProcessHandler extends AbstractServerResponseHandler {

	public ProcessHandler(ServerContextImpl context) {
		super(context, "Process request");
	}

	private static class ChannelProcessRequesterImpl extends BaseChannelRequester implements ChannelProcessRequester, TransportSender {

		private volatile ChannelProcess channelProcess;
		private volatile Status status;

		public ChannelProcessRequesterImpl(ServerContextImpl context, ServerChannelImpl channel, int ioid, Transport transport,
				PVStructure pvRequest) {
			super(context, channel, ioid, transport);

			startRequest(QoS.INIT.getMaskValue());
			channel.registerRequest(ioid, this);

			try
			{
				channelProcess = channel.getChannel().createChannelProcess(this, pvRequest);
			} catch (Throwable th) {
				// simply cannot trust code above
				BaseChannelRequester.sendFailureMessage((byte)16, transport, ioid, (byte)QoS.INIT.getMaskValue(),
						statusCreate.createStatus(StatusType.FATAL, "Unexpected exception caught: " + th.getMessage(), th));
				destroy();
			}
		}

		public void channelProcessConnect(Status status, ChannelProcess channelProcess) {
			this.status = status;
			this.channelProcess = channelProcess;

			transport.enqueueSendRequest(this);

			// self-destruction
			if (!status.isSuccess()) {
				destroy();
			}
		}

		public void processDone(Status status, ChannelProcess channelProcess) {
			this.status = status;
			transport.enqueueSendRequest(this);
		}

		/* (non-Javadoc)
		 * @see org.epics.pvdata.misc.Destroyable#destroy()
		 */
		public void destroy() {
			channel.unregisterRequest(ioid);

			// asCheck
			channel.getChannelSecuritySession().release(ioid);

			if (channelProcess != null)
				channelProcess.destroy();
		}

		/**
		 * @return the channelProcess
		 */
		public ChannelProcess getChannelProcess() {
			return channelProcess;
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

			control.startMessage((byte)16, Integer.SIZE/Byte.SIZE + 1);
			buffer.putInt(ioid);
			buffer.put((byte)request);
			status.serialize(buffer, control);

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
			BaseChannelRequester.sendFailureMessage((byte)16, transport, ioid, qosCode, BaseChannelRequester.badCIDStatus);
			return;
		}

		final boolean init = QoS.INIT.isSet(qosCode);
		if (init)
		{
		    // pvRequest
		    final PVStructure pvRequest = SerializationHelper.deserializePVRequest(payloadBuffer, transport);

			// asCheck
			Status asStatus = channel.getChannelSecuritySession().authorizeCreateChannelProcess(ioid, pvRequest);
			if (!asStatus.isSuccess())
			{
				BaseChannelRequester.sendFailureMessage((byte)16, transport, ioid, (byte)QoS.INIT.getMaskValue(), asStatus);
				return;
			}

		    // create...
		    new ChannelProcessRequesterImpl(context, channel, ioid, transport, pvRequest);
		}
		else
		{
			final boolean lastRequest = QoS.DESTROY.isSet(qosCode);

			ChannelProcessRequesterImpl request = (ChannelProcessRequesterImpl)channel.getRequest(ioid);
			if (request == null) {
				BaseChannelRequester.sendFailureMessage((byte)16, transport, ioid, qosCode, BaseChannelRequester.badIOIDStatus);
				return;
			}

			if (!request.startRequest(qosCode)) {
				BaseChannelRequester.sendFailureMessage((byte)16, transport, ioid, qosCode, BaseChannelRequester.otherRequestPendingStatus);
				return;
			}

			ChannelProcess channelProcess = request.getChannelProcess();

			if (lastRequest)
				channelProcess.lastRequest();

			// asCheck
			Status asStatus = channel.getChannelSecuritySession().authorizeProcess(ioid);
			if (!asStatus.isSuccess())
			{
				BaseChannelRequester.sendFailureMessage((byte)16, transport, ioid, qosCode, asStatus);
				if (lastRequest)
					request.destroy();
				return;
			}

			channelProcess.process();
		}

	}
}
