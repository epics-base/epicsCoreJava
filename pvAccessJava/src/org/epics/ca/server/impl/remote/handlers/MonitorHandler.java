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

import org.epics.ca.impl.remote.QoS;
import org.epics.ca.impl.remote.SerializationHelper;
import org.epics.ca.impl.remote.Transport;
import org.epics.ca.impl.remote.TransportSendControl;
import org.epics.ca.impl.remote.TransportSender;
import org.epics.ca.impl.remote.server.ChannelHostingTransport;
import org.epics.ca.server.impl.remote.ServerChannelImpl;
import org.epics.ca.server.impl.remote.ServerContextImpl;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.monitor.Monitor;
import org.epics.pvData.monitor.MonitorElement;
import org.epics.pvData.monitor.MonitorRequester;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.Status.StatusType;
import org.epics.pvData.pv.Structure;

/**
 * Monitor request handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class MonitorHandler extends AbstractServerResponseHandler {

	/**
	 * @param context
	 */
	public MonitorHandler(ServerContextImpl context) {
		super(context, "Monitor request");
	}

	private static class MonitorRequesterImpl extends BaseChannelRequester implements MonitorRequester, TransportSender {
		
		private volatile Monitor channelMonitor;
		private Status status;
		private volatile Structure structure;
		private volatile Monitor monitor;

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

		@Override
		public void unlisten(Monitor monitor) {
			// TODO !!!
		}
		
		@Override
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
		
		@Override
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
		 * @see org.epics.pvData.misc.Destroyable#destroy()
		 */
		@Override
		public void destroy() {
			channel.unregisterRequest(ioid);
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
			BaseChannelRequester.sendFailureMessage((byte)13, transport, ioid, qosCode, BaseChannelRequester.badCIDStatus);
			return;
		}
		
		final boolean init = QoS.INIT.isSet(qosCode);
		if (init)
		{
			
			// pvRequest
		    final PVStructure pvRequest = SerializationHelper.deserializePVRequest(payloadBuffer, transport);
			
			// create...
			new MonitorRequesterImpl(context, channel, ioid, transport, pvRequest);
		}
		else
		{
			final boolean lastRequest = QoS.DESTROY.isSet(qosCode);
			final boolean get = QoS.GET.isSet(qosCode);
			final boolean process = QoS.PROCESS.isSet(qosCode);
			
			MonitorRequesterImpl request = (MonitorRequesterImpl)channel.getRequest(ioid);
			if (request == null) {
				BaseChannelRequester.sendFailureMessage((byte)13, transport, ioid, qosCode, BaseChannelRequester.badIOIDStatus);
				return;
			}
			
			/*
			if (!request.startRequest(qosCode)) {
				BaseChannelRequester.sendFailureMessage((byte)13, transport, ioid, qosCode, BaseChannelRequester.otherRequestPendingStatus);
				return;
			}
			*/

			/*
			// check read access rights
			if (!AccessRights.READ.isSet(channel.getAccessRights()))
			{
				monitorFailureResponse(transport, ioid, qosCode, BaseChannelRequester.noReadACLStatus);
				if (lastRequest)
					request.destroy();
				return;
			}
			*/

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
