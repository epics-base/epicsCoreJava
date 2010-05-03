/*
 * Copyright (c) 2004 by Cosylab
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
import java.util.ArrayList;

import org.epics.ca.client.ChannelFind;
import org.epics.ca.client.ChannelFindRequester;
import org.epics.ca.client.ChannelProvider;
import org.epics.ca.impl.remote.QoS;
import org.epics.ca.impl.remote.Transport;
import org.epics.ca.impl.remote.TransportSendControl;
import org.epics.ca.impl.remote.TransportSender;
import org.epics.ca.server.impl.remote.ServerContextImpl;
import org.epics.ca.util.InetAddressUtil;
import org.epics.pvData.misc.SerializeHelper;
import org.epics.pvData.pv.Status;

/**
 * Search request handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class SearchHandler extends AbstractServerResponseHandler {

	private final ChannelProvider provider;
	private final ChannelFindRequesterImplObjectPool objectPool = new ChannelFindRequesterImplObjectPool();
	
	/**
	 * @param context
	 */
	public SearchHandler(ServerContextImpl context) {
		super(context, "Search request");
		provider = context.getChannelProvider();
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.AbstractResponseHandler#handleResponse(java.net.InetSocketAddress, org.epics.ca.core.Transport, byte, byte, int, java.nio.ByteBuffer)
	 */
	@Override
	public void handleResponse(InetSocketAddress responseFrom, Transport transport, byte version, byte command, int payloadSize, ByteBuffer payloadBuffer) {
		super.handleResponse(responseFrom, transport, version, command, payloadSize, payloadBuffer);

		transport.ensureData((Integer.SIZE+Short.SIZE)/Byte.SIZE+1);
		final int searchSequenceId = payloadBuffer.getInt();
		final byte qosCode = payloadBuffer.get();
		final int count = payloadBuffer.getShort() & 0xFFFF;
		final boolean responseRequired = QoS.REPLY_REQUIRED.isSet(qosCode);
		
		for (int i = 0; i < count; i++) {
			transport.ensureData(Integer.SIZE/Byte.SIZE);
			final int cid = payloadBuffer.getInt();
			final String name = SerializeHelper.deserializeString(payloadBuffer, transport);
			// no name check here...

			provider.channelFind(name, objectPool.get().set(searchSequenceId, cid, responseFrom, responseRequired));
		}
	}

	private class ChannelFindRequesterImpl implements ChannelFindRequester, TransportSender {
		
		private int searchSequenceId;
		private int cid;
		private InetSocketAddress sendTo;
		private boolean responseRequired;
		
		private boolean wasFound;
		
		public ChannelFindRequesterImpl() {
			// noop
		}

		public void clear()
		{
			synchronized (this) {
				sendTo = null;
			}
		}
		
		public ChannelFindRequesterImpl set(int searchSequenceId, int cid, InetSocketAddress sendTo, boolean responseRequired)
		{
			synchronized (this) {
				this.searchSequenceId = searchSequenceId;
				this.cid = cid;
				this.sendTo = sendTo;
				this.responseRequired = responseRequired;
			}
			return this;
		}
		
		@Override
		public void channelFindResult(Status status, ChannelFind channelFind, boolean wasFound) {
			// TODO status
			synchronized (this)
			{
				if (wasFound || responseRequired)
				{
					this.wasFound = wasFound;
					context.getBroadcastTransport().enqueueSendRequest(this);
				}
			}
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

			final int count = 1;
			control.startMessage((byte)4, (Integer.SIZE+Byte.SIZE+128+2*Short.SIZE+count*Integer.SIZE)/Byte.SIZE);

			synchronized (this)
			{
				buffer.putInt(searchSequenceId);
				buffer.put(wasFound ? (byte)1 : (byte)0);
						
				// NOTE: is it possible (very likely) that address is any local address ::ffff:0.0.0.0
				InetAddressUtil.encodeAsIPv6Address(buffer, context.getServerInetAddress());
				buffer.putShort((short)context.getServerPort());
				
				/*
				if (count > CAConstants.MAX_SEARCH_BATCH_COUNT)
					throw new IllegalArgumentException("too many search responses in a batch message");
				*/
				buffer.putShort((short)count);
				buffer.putInt(cid);

				control.setRecipient(sendTo);
			}

			// return this object to the pool
			objectPool.put(this);
		}

	};
	
	// TODO limit max, cleanup after some time
	private class ChannelFindRequesterImplObjectPool {
		private final ArrayList<ChannelFindRequesterImpl> elements = new ArrayList<ChannelFindRequesterImpl>();
		
		public ChannelFindRequesterImpl get() {
			synchronized (elements) {
				final int count = elements.size();
				if (count == 0) {
					return new ChannelFindRequesterImpl();
				}
				else {
					return elements.remove(count - 1);
				}
			}
		}

		public void put(ChannelFindRequesterImpl element) {
			element.clear();
			synchronized (elements) {
				elements.add(element);
			}
		}
	
	}
	
}
 