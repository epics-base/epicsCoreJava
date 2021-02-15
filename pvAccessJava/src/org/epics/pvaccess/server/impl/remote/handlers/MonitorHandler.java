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

import org.epics.pvaccess.impl.remote.PipelineMonitor;
import org.epics.pvaccess.impl.remote.QoS;
import org.epics.pvaccess.impl.remote.SerializationHelper;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.TransportSender;
import org.epics.pvaccess.impl.remote.server.ChannelHostingTransport;
import org.epics.pvaccess.server.impl.remote.ServerChannelImpl;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.monitor.Monitor;
import org.epics.pvdata.monitor.MonitorElement;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.Structure;

/**
 * Monitor request handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 */
public class MonitorHandler extends AbstractServerResponseHandler {

	public MonitorHandler(ServerContextImpl context) {
		super(context, "Monitor request");
	}

	private static class MonitorRequesterImpl extends BaseChannelRequester implements MonitorRequester, TransportSender {

		private volatile Monitor channelMonitor;
		private Status status;
		private volatile Structure structure;
		private volatile Monitor monitor;
		private volatile boolean unlisten = false;

		public MonitorRequesterImpl(ServerContextImpl context, ServerChannelImpl channel, int ioid, Transport transport,
				 PVStructure pvRequest) {
			super(context, channel, ioid, transport);

			startRequest(QoS.INIT.getMaskValue());
			channel.registerRequest(ioid, this);

			try {
				channelMonitor = channel.getChannel().createMonitor(this, pvRequest);
			} catch (Throwable th) {
				// simply cannot trust code above
				BaseChannelRequester.sendFailureMessage((byte)13, transport, ioid, (byte)QoS.INIT.getMaskValue(),
						statusCreate.createStatus(StatusType.FATAL, "Unexpected exception caught: " + th.getMessage(), th));
				destroy();
			}
		}

		public void unlisten(Monitor monitor) {
			unlisten = true;
			transport.enqueueSendRequest(this);
		}

		public void monitorConnect(Status status, Monitor monitor, Structure structure) {
			synchronized (this) {
				this.status = status;
				this.monitor = monitor;
				this.structure = structure;
				this.monitor = monitor;
			}
			transport.enqueueSendRequest(this);

			// self-destruction
			if (!status.isSuccess()) {
				destroy();
			}
		}

		public void monitorEvent(Monitor monitor) {

			// TODO !!! if queueSize==0, monitor.poll() has to be called and returned NOW (since there is no cache)
			//sendEvent(transport);

			// TODO implement via TransportSender
			/*
			// initiate submit to dispatcher queue, if necessary
			synchronized (register) {
				if (register.getAndSet(true))
					eventConsumer.consumeEvents(this);
			}*/
			// TODO
			// multiple ((BlockingServerTCPTransport)transport).enqueueMonitorSendRequest(this);
			transport.enqueueSendRequest(this);
		}

		/* (non-Javadoc)
		 * @see org.epics.pvdata.misc.Destroyable#destroy()
		 */
		public void destroy() {
			channel.unregisterRequest(ioid);

			// asCheck
			channel.getChannelSecuritySession().release(ioid);

			if (channelMonitor != null)
				channelMonitor.destroy();
		}

		/**
		 * @return the channelMonitor
		 */
		public Monitor getChannelMonitor() {
			return channelMonitor;
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

			/*
			control.startMessage((byte)13, Integer.SIZE/Byte.SIZE + 1);
			buffer.putInt(ioid);
			buffer.put((byte)request);
*/
			if (QoS.INIT.isSet(request))
			{
				control.startMessage((byte)13, Integer.SIZE/Byte.SIZE + 1);
				buffer.putInt(ioid);
				buffer.put((byte)request);

				synchronized (this) {
					status.serialize(buffer, control);
				}

				if (status.isSuccess())
				{
					control.cachedSerialize(structure, buffer);
				}

				stopRequest(); startRequest(QoS.DEFAULT.getMaskValue());
			}
			else
			{
				final Monitor monitor = this.monitor;
				final MonitorElement element = monitor.poll();
				if (element != null)
				{
					control.startMessage((byte)13, Integer.SIZE/Byte.SIZE + 1);
					// multiple control.ensureBuffer(Integer.SIZE/Byte.SIZE + 1);
					buffer.putInt(ioid);
					buffer.put((byte)request);

					// changedBitSet and data, if not notify only (i.e. queueSize == -1)
					final BitSet changedBitSet = element.getChangedBitSet();
					if (changedBitSet != null)
					{
						changedBitSet.serialize(buffer, control);
						element.getPVStructure().serialize(buffer, control, changedBitSet);

						// overrunBitset
						element.getOverrunBitSet().serialize(buffer, control);
					}

					monitor.release(element);
				}
				else
				{
					// TODO should I latch unlisten
					if (unlisten)
					{
						control.startMessage((byte)13, Integer.SIZE/Byte.SIZE + 1);
						buffer.putInt(ioid);
						buffer.put((byte)QoS.DESTROY.getMaskValue());
						StatusFactory.getStatusCreate().getStatusOK().serialize(buffer, control);
					}
				}
			}

			//stopRequest();

			/*
			// lastRequest
			if (QoS.DESTROY.isSet(request))
				destroy();
			*/
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
			BaseChannelRequester.sendFailureMessage((byte)13, transport, ioid, qosCode, BaseChannelRequester.badCIDStatus);
			return;
		}

		final boolean init = QoS.INIT.isSet(qosCode);
		if (init)
		{

			// pvRequest
		    final PVStructure pvRequest = SerializationHelper.deserializePVRequest(payloadBuffer, transport);

			// asCheck
			Status asStatus = channel.getChannelSecuritySession().authorizeCreateMonitor(ioid, pvRequest);
			if (!asStatus.isSuccess())
			{
				BaseChannelRequester.sendFailureMessage((byte)13, transport, ioid, (byte)QoS.INIT.getMaskValue(), asStatus);
				return;
			}

			// create...
			new MonitorRequesterImpl(context, channel, ioid, transport, pvRequest);

			// pipelining monitor (i.e. w/ flow control)
			final boolean ack = QoS.GET_PUT.isSet(qosCode);
	        if (ack)
	        {
	        	transport.ensureData(4);
	            int nfree = payloadBuffer.getInt();
				MonitorRequesterImpl request = (MonitorRequesterImpl)channel.getRequest(ioid);
				Monitor channelMonitor = request.getChannelMonitor();
				if (channelMonitor instanceof PipelineMonitor)
					((PipelineMonitor)channelMonitor).reportRemoteQueueStatus(nfree);
	        }
	    }
		else
		{
			final boolean lastRequest = QoS.DESTROY.isSet(qosCode);
			final boolean get = QoS.GET.isSet(qosCode);
			final boolean process = QoS.PROCESS.isSet(qosCode);
			final boolean ack = QoS.GET_PUT.isSet(qosCode);

			MonitorRequesterImpl request = (MonitorRequesterImpl)channel.getRequest(ioid);
			if (request == null) {
				BaseChannelRequester.sendFailureMessage((byte)13, transport, ioid, qosCode, BaseChannelRequester.badIOIDStatus);
				return;
			}

			// pipelining monitor (i.e. w/ flow control)
	        if (ack)
	        {
	        	transport.ensureData(4);
	            int nfree = payloadBuffer.getInt();
	            Monitor channelMonitor = request.getChannelMonitor();
				if (channelMonitor instanceof PipelineMonitor)
					((PipelineMonitor)channelMonitor).reportRemoteQueueStatus(nfree);
				return;
	            // note: not possible to ack and destroy
			}

	        /*
			if (!request.startRequest(qosCode)) {
				BaseChannelRequester.sendFailureMessage((byte)13, transport, ioid, qosCode, BaseChannelRequester.otherRequestPendingStatus);
				return;
			}
			*/

			// NOTE: we do a get check
			// asCheck
			Status asStatus = channel.getChannelSecuritySession().authorizeGet(ioid);
			if (!asStatus.isSuccess())
			{
				BaseChannelRequester.sendFailureMessage((byte)13, transport, ioid, qosCode, asStatus);
				if (lastRequest)
					request.destroy();
				return;
			}


			if (process)
			{
				if (get)
					request.getChannelMonitor().start();
				else
					request.getChannelMonitor().stop();
				//request.stopRequest();
			}
			else if (get)
			{
				// not supported
			}

			if (lastRequest)
				request.destroy();

		}

	}

}
