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

package org.epics.pvaccess.server.impl.remote.handlers;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.epics.pvaccess.client.ChannelFind;
import org.epics.pvaccess.client.ChannelFindRequester;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.impl.remote.ProtocolType;
import org.epics.pvaccess.impl.remote.QoS;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.TransportSender;
import org.epics.pvaccess.impl.remote.udp.BlockingUDPTransport;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvaccess.util.InetAddressUtil;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.misc.Timer.TimerCallback;
import org.epics.pvdata.misc.Timer.TimerNode;
import org.epics.pvdata.misc.TimerFactory;
import org.epics.pvdata.pv.Status;

/**
 * Search request handler.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 */
public class SearchHandler extends AbstractServerResponseHandler {

	private final ChannelFindRequesterImplObjectPool objectPool = new ChannelFindRequesterImplObjectPool();

	private final Random random = new Random();
	private static final int MAX_SERVER_SEARCH_RESPONSE_DELAY_MS = 100;

	public SearchHandler(ServerContextImpl context) {
		super(context, "Search request");
	}

	// TODO for now only TCP supported
	private static final String SUPPORTED_PROTOCOL = ProtocolType.tcp.name();

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.AbstractResponseHandler#handleResponse(java.net.InetSocketAddress, org.epics.pvaccess.core.Transport, byte, byte, int, java.nio.ByteBuffer)
	 */
	@Override
	public void handleResponse(InetSocketAddress responseFrom, Transport transport, byte version, byte command, int payloadSize, ByteBuffer payloadBuffer) {
		super.handleResponse(responseFrom, transport, version, command, payloadSize, payloadBuffer);

		transport.ensureData(4+1+3+16+2);

		final int startPosition = payloadBuffer.position();

		final int searchSequenceId = payloadBuffer.getInt();
		final byte qosCode = payloadBuffer.get();

		// reserved part
		payloadBuffer.get();
		payloadBuffer.getShort();

		// 128-bit IPv6 address
		byte[] byteAddress = new byte[16];
		payloadBuffer.get(byteAddress);

		final int port = payloadBuffer.getShort() & 0xFFFF;


		// NOTE: Java knows how to compare IPv4/IPv6 :)

		InetAddress addr;
		try {
			addr = InetAddress.getByAddress(byteAddress);
		} catch (UnknownHostException e) {
			context.getLogger().log(Level.FINER, "Invalid address '" +  new String(byteAddress) + "' in search response received from: " + responseFrom, e);
			return;
		}

		// accept given address if explicitly specified by sender
		if (!addr.isAnyLocalAddress())
			responseFrom = new InetSocketAddress(addr, port);
		else
			responseFrom = new InetSocketAddress(responseFrom.getAddress(), port);


		//
		// locally broadcast if unicast (qosCode & 0x80 == 0x80)
		//
		if ((qosCode & 0x80) == 0x80)
		{
			BlockingUDPTransport bt = context.getLocalMulticastTransport();
			if (bt != null)
			{
				// clear unicast flag
				payloadBuffer.put(startPosition+4, (byte)(qosCode & ~0x80));

				// update response address
				payloadBuffer.position(startPosition+8);
				InetAddressUtil.encodeAsIPv6Address(payloadBuffer, responseFrom.getAddress());

				payloadBuffer.position(payloadBuffer.limit());		// send will call flip()

				bt.send(payloadBuffer);
				return;
			}
		}



		final int protocolsCount = SerializeHelper.readSize(payloadBuffer, transport);
		boolean allowed = (protocolsCount == 0);
		for (int i = 0; i < protocolsCount; i++)
		{
			String protocol = SerializeHelper.deserializeString(payloadBuffer, transport);
			if (SUPPORTED_PROTOCOL.equals(protocol))
				allowed = true;
		}

		// NOTE: we do not stop reading the buffer

		transport.ensureData(2);
		final int count = payloadBuffer.getShort() & 0xFFFF;

		// TODO DoS attack
		final boolean responseRequired = QoS.REPLY_REQUIRED.isSet(qosCode);

		// TODO bloom filter or similar server selection (by GUID)

		if (count > 0)
		{
			for (int i = 0; i < count; i++) {
				transport.ensureData(4);
				final int cid = payloadBuffer.getInt();
				final String name = SerializeHelper.deserializeString(payloadBuffer, transport);
				// no name check here...

				if (allowed)
				{
					List<ChannelProvider> providers = context.getChannelProviders();
					ChannelFindRequesterImpl cfri = objectPool.get().set(context.getLogger(), searchSequenceId, name, cid, responseFrom, responseRequired, providers.size());
					for (ChannelProvider provider : providers)
						provider.channelFind(name, cfri);
				}
			}
		}
		else
		{
			// server search response
			if (allowed)
			{
				double delay = random.nextInt(MAX_SERVER_SEARCH_RESPONSE_DELAY_MS) / 1000.0;
				final InetSocketAddress rf = responseFrom;
				TimerNode timerNode = TimerFactory.createNode(new TimerCallback() {

					public void timerStopped() {
						// noop
					}

					public void callback() {
						objectPool.get().set(context.getLogger(), searchSequenceId, rf).channelFindResult(StatusFactory.getStatusCreate().getStatusOK(), null, false);
					}
				});
				// delay response to avoid "UDP search storms"
				context.getTimer().scheduleAfterDelay(timerNode, delay);
			}
		}
	}

	private class ChannelFindRequesterImpl implements ChannelFindRequester, TransportSender {

		private Logger logger;

		private boolean serverSearch;
		private int searchSequenceId;
		private String channelName;
		private int cid;
		private InetSocketAddress sendTo;
		private boolean responseRequired;

		private boolean wasFound;

		private int expectedResponseCount;
		private int responseCount;

		public ChannelFindRequesterImpl() {
			// noop
		}

		public void clear()
		{
			synchronized (this) {
				logger = null;
				channelName = null;
				sendTo = null;
				responseCount = 0;
				wasFound = false;
			}
		}

		public ChannelFindRequesterImpl set(Logger logger, int searchSequenceId, String channelName, int cid, InetSocketAddress sendTo, boolean responseRequired, int expectedResponseCount)
		{
			synchronized (this) {
				this.serverSearch = false;
				this.searchSequenceId = searchSequenceId;
				this.channelName = channelName;
				this.cid = cid;
				this.sendTo = sendTo;
				this.responseRequired = responseRequired;
				this.expectedResponseCount = expectedResponseCount;
			}
			return this;
		}

		// server search
		public ChannelFindRequesterImpl set(Logger logger, int searchSequenceId, InetSocketAddress sendTo)
		{
			synchronized (this) {
				this.logger = logger;
				this.serverSearch = true;
				this.searchSequenceId = searchSequenceId;
				this.channelName = null;
				this.cid = 0;
				this.sendTo = sendTo;
				this.responseRequired = true;
				this.expectedResponseCount = 1;
			}
			return this;
		}

		public void channelFindResult(Status status, ChannelFind channelFind, boolean wasFound) {
			// TODO status
			synchronized (this)
			{
				responseCount++;
				if (responseCount > expectedResponseCount)
				{
					if ((responseCount+1) == expectedResponseCount)
						logger.fine("More responses received than expected for channel '" + channelName + "'!");
					return;
				}

				if (this.wasFound && wasFound)
				{
			        logger.fine("Channel '" + channelName + "' is hosted by different channel providers!");
			        return;
			    }

			    if (wasFound || (responseRequired && (responseCount == expectedResponseCount)))
			    {
			    	// register mapping
			        if (wasFound && expectedResponseCount > 1)
			            context.getChannelNameToProviderMap().put(channelName, channelFind.getChannelProvider());

					this.wasFound = wasFound;
					context.getBroadcastTransport().enqueueSendRequest(this);
				}
			}
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

			control.startMessage((byte)4, 12+4+16+2);

			synchronized (this)
			{
				buffer.put(context.getGUID());

				buffer.putInt(searchSequenceId);

				// NOTE: is it possible (very likely) that address is any local address ::ffff:0.0.0.0
				InetAddressUtil.encodeAsIPv6Address(buffer, context.getServerInetAddress());
				buffer.putShort((short)context.getServerPort());

				SerializeHelper.serializeString(SUPPORTED_PROTOCOL, buffer, control);

				control.ensureBuffer(1);
				buffer.put(wasFound ? (byte)1 : (byte)0);

				/*
				if (count > PVAConstants.MAX_SEARCH_BATCH_COUNT)
					throw new IllegalArgumentException("too many search responses in a batch message");
				*/
				if (!serverSearch)
				{
					// TODO for now we do not gather search responses
					buffer.putShort((short)1);
					buffer.putInt(cid);
				}
				else
				{
					buffer.putShort((short)0);
				}

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

