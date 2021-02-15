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

package org.epics.pvaccess.client.impl.remote.search;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.client.impl.remote.ClientContextImpl;
import org.epics.pvaccess.impl.remote.ProtocolType;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.udp.BlockingUDPTransport.InetAddressType;
import org.epics.pvaccess.impl.remote.utils.GUID;
import org.epics.pvaccess.util.InetAddressUtil;
import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.misc.Timer.TimerCallback;
import org.epics.pvdata.misc.Timer.TimerNode;
import org.epics.pvdata.misc.TimerFactory;
import org.epics.pvdata.pv.Field;


/**
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class SimpleChannelSearchManagerImpl implements ChannelSearchManager, TimerCallback, Runnable {

	/**
	 * Context.
	 */
	private final ClientContextImpl context;

	/**
	 * Canceled flag.
	 */
	private volatile boolean canceled = false;

	/**
	 * Search (datagram) sequence number.
	 */
	private final AtomicInteger sequenceNumber = new AtomicInteger(0);

	/**
	 * Send byte buffer (frame)
	 */
	private final ByteBuffer sendBuffer;

    /**
     * Set of registered channels.
     */
    private final Map<Integer, SearchInstance> channels =
    		Collections.synchronizedMap(new HashMap<Integer, SearchInstance>());

    private final ArrayList<SearchInstance> immediateSearch = new ArrayList<SearchInstance>(128);

    private final TimerNode timerNode;
    private long lastTimeSent = 0;

    // 225ms +/- 25ms random
    private static final double ATOMIC_PERIOD = 0.225;
    private static final int PERIOD_JITTER_MS = 25;


	private final short responsePort;
	private final InetAddress responseAddress;

	public SimpleChannelSearchManagerImpl(ClientContextImpl context)
	{
		this.context = context;

		// set search response address
		InetSocketAddress responseSocketAddress = context.getSearchTransport().getRemoteAddress();
		responsePort = (short)responseSocketAddress.getPort();
		responseAddress = responseSocketAddress.getAddress();

		// create and initialize send buffer
		sendBuffer = ByteBuffer.allocate(PVAConstants.MAX_UDP_UNFRAGMENTED_SEND);
		initializeSendBuffer();

		// add some jitter so that all the clients do not send at the same time
		double period = ATOMIC_PERIOD + (new Random().nextInt(2*PERIOD_JITTER_MS+1) - PERIOD_JITTER_MS)/(double)1000;
		timerNode = TimerFactory.createNode(this);
		context.getTimer().schedulePeriodic(timerNode, period, period);

		new Thread(this, "pvAccess immediate-search").start();
	}

	public void run()
	{
		while (!canceled)
		{
			try
			{
				synchronized (immediateSearch) {
					try {
						// wait if empty
						if (immediateSearch.size() == 0)
							immediateSearch.wait();

						if (canceled)
							return;

						// coalescence...
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// noop
					}
				}

				SearchInstance[] sis;
				synchronized (immediateSearch) {
					if (immediateSearch.size() == 0)
						return;
					sis = new SearchInstance[immediateSearch.size()];
					immediateSearch.toArray(sis);
					immediateSearch.clear();
				}

				send(sis);
			}
			catch (Exception th)
			{
				// should never happen, be we are careful and verbose
				th.printStackTrace();
			}

		}
	}
	/**
	 * Cancel.
	 */
	public synchronized void cancel()
	{
		if (canceled)
			return;
		canceled = true;

		// wake-up
		synchronized (immediateSearch) {
			immediateSearch.notifyAll();
		}

		timerNode.cancel();
	}

	private final static int DATA_COUNT_POSITION = PVAConstants.PVA_MESSAGE_HEADER_SIZE + 4+1+3+16+2+1+4;
	private final static int CAST_POSITION = PVAConstants.PVA_MESSAGE_HEADER_SIZE + 4;
    private final static int PAYLOAD_POSITION = 4;

    /**
	 * Initialize send buffer.
	 */
	private void initializeSendBuffer()
	{
		// new buffer
		sendBuffer.clear();
		sendBuffer.put(PVAConstants.PVA_MAGIC);
		sendBuffer.put(PVAConstants.PVA_VERSION);
		sendBuffer.put((byte)0x80);	// data + big endian
		sendBuffer.put((byte)3);	// search
		sendBuffer.putInt(4+1+3+16+2+1);		// "zero" payload
		sendBuffer.putInt(sequenceNumber.incrementAndGet());

		// multicast vs unicast mask
		sendBuffer.put((byte)0x00);

		// reserved part
		sendBuffer.put((byte)0);
		sendBuffer.putShort((short)0);

		// NOTE: is it possible (very likely) that address is any local address ::ffff:0.0.0.0
		InetAddressUtil.encodeAsIPv6Address(sendBuffer, responseAddress);
		sendBuffer.putShort((short)responsePort);

		// TODO now not only TCP supported
		// note: this affects DATA_COUNT_POSITION
		sendBuffer.put((byte)1);
		SerializeHelper.serializeString(ProtocolType.tcp.name(), sendBuffer);
		sendBuffer.putShort((short)0);	// count
	}

	/**
	 * Flush send buffer.
	 */
	private synchronized void flushSendBuffer()
	{

		sendBuffer.put(CAST_POSITION, (byte)0x80);	// unicast, no reply required
		context.getSearchTransport().send(sendBuffer, InetAddressType.UNICAST);

		sendBuffer.put(CAST_POSITION, (byte)0x00);	// b/m-cast, no reply required
		context.getSearchTransport().send(sendBuffer, InetAddressType.BROADCAST_MULTICAST);

		initializeSendBuffer();
	}

	private static final TransportSendControl mockTransportSendControl = new TransportSendControl() {

		public void endMessage() {
		}

		public void flush(boolean lastMessageCompleted) {
		}

		public void setRecipient(InetSocketAddress sendTo) {
		}

		public void startMessage(byte command, int ensureCapacity) {
		}

		public void ensureBuffer(int size) {
		}

		public void alignBuffer(int alignment) {
			throw new UnsupportedOperationException("alignBuffer not supported");
		}

		public void flushSerializeBuffer() {
		}

		public void cachedSerialize(Field field, ByteBuffer buffer) {
			// no cache
			field.serialize(buffer, this);
		}

	};

    /**
	 * Send search message.
	 * @return success status.
	 */
	private static boolean generateSearchRequestMessage(SearchInstance si, ByteBuffer requestMessage, TransportSendControl control)
	{
	    short dataCount = requestMessage.getShort(DATA_COUNT_POSITION);

		dataCount++;
		if (dataCount >= PVAConstants.MAX_SEARCH_BATCH_COUNT)
			return false;

		final String name = si.getChannelName();
		// not nice...
		final int addedPayloadSize = Integer.SIZE/Byte.SIZE + (1 + Integer.SIZE/Byte.SIZE + name.length());

		if (requestMessage.remaining() < addedPayloadSize)
			return false;

		requestMessage.putInt(si.getChannelID());
		SerializeHelper.serializeString(name, requestMessage, control);

		requestMessage.putInt(PAYLOAD_POSITION, requestMessage.position() - PVAConstants.PVA_MESSAGE_HEADER_SIZE);
		requestMessage.putShort(DATA_COUNT_POSITION, dataCount);

		return true;
	}

	/**
	 * Generate (put on send buffer) search request
	 * @param channel channel
	 * @param allowNewFrame flag indicating if new search request message is allowed to be put in new frame.
	 * @return <code>true</code> if new frame was sent.
	 */
	private synchronized boolean generateSearchRequestMessage(SearchInstance channel, boolean allowNewFrame, boolean flush)
	{
		boolean success = generateSearchRequestMessage(channel, sendBuffer, mockTransportSendControl);
		// buffer full, flush
		if (!success)
		{
			flushSendBuffer();
			if (allowNewFrame)
				generateSearchRequestMessage(channel, sendBuffer, mockTransportSendControl);
			if (flush)
				flushSendBuffer();
			return true;
		}

		if (flush)
			flushSendBuffer();

		return flush;
	}


	/**
	 * Get number of registered channels.
	 * @return number of registered channels.
	 */
	public int registeredCount() {
		synchronized (channels) {
			return channels.size();
		}
	}

	public void register(SearchInstance channel)
	{
		register(channel, false);
	}

	/**
	 * Register channel.
	 * @param channel channel to register.
	 * @param penalize register with penalty (do not issue search immediately).
	 */
	public void register(SearchInstance channel, boolean penalize)
	{
		if (canceled)
			return;

		synchronized (channels)
		{
			// overrided if already registered
			channels.put(channel.getChannelID(), channel);
			channel.getUserValue().set(penalize ? MAX_FALLBACK_COUNT_VALUE : DEFAULT_COUNT_VALUE);
		}

		// put to immediate, batched list
		synchronized (immediateSearch) {
			immediateSearch.add(channel);
			if (immediateSearch.size() == 1)
				immediateSearch.notify();
		}
	}


	/**
	 * Unregister channel.
	 * @param channel channel to unregister.
	 */
	public void unregister(SearchInstance channel)
	{
		synchronized (channels)
		{
			channels.remove(channel.getChannelID());
		}
	}

	/**
	 * Search response from server (channel found).
	 * @param guid server GUID.
	 * @param cid	client channel ID.
	 * @param seqNo	search sequence number.
	 * @param minorRevision	server minor PVA revision.
	 * @param serverAddress	server address.
	 */
	public void searchResponse(GUID guid, int cid, int seqNo, byte minorRevision, InetSocketAddress serverAddress)
	{
		// first remove
		SearchInstance si;
		synchronized (channels) {
			si = (SearchInstance)channels.remove(cid);
		}

		if (si == null) {
			// minor hack to enable duplicate reports
			si = context.getChannel(cid);
			if (si != null)
				si.searchResponse(guid, minorRevision, serverAddress);
			return;
		}

		// then notify SearchInstance
		si.searchResponse(guid, minorRevision, serverAddress);
	}

	/**
	 * New server detected.
	 * Boost searching of all channels.
	 */
	public void newServerDetected()
	{
		boost();
		callback();
	}

	private final static int DEFAULT_COUNT_VALUE = 1;
	private final static int BOOST_VALUE = 1;
	// must be power of two (so that search is done)
	private final static int MAX_COUNT_VALUE = 1 << 8;
	private final static int MAX_FALLBACK_COUNT_VALUE = (1 << 7) + 1;

	private void boost()
	{
//		synchronized (channels) {
//			if (channels.size() == 0)
//				return;
//
//			// TODO no copy when iterator is supported
//			SearchInstance[] sis = new SearchInstance[channels.size()];
//			channels.values().toArray(sis);
//
//
//			for (SearchInstance si : sis)
//				si.getUserValue().set(BOOST_VALUE);
//		}
        synchronized (channels) {
            for (SearchInstance searchInstance : channels.values()) {
                searchInstance.getUserValue().set(BOOST_VALUE);
            }
        }
	}

	public void callback() {

		// high-frequency beacon anomaly trigger guard
		synchronized (this)
		{
			long now = System.currentTimeMillis();
			if (now - lastTimeSent < 100)
				return;
			lastTimeSent = now;
		}

		try
		{
			SearchInstance[] sis;
			synchronized (channels) {
				if (channels.size() == 0)
					return;
				sis = new SearchInstance[channels.size()];
				channels.values().toArray(sis);
			}

			send(sis);
		}
		catch (Throwable th)
		{
			// should never happen, be we are careful and verbose
			th.printStackTrace();
		}
	}

	private static boolean isPowerOfTwo (int x)
	{
	  return ((x > 0) && (x & (x - 1)) == 0);
	}

	private static final int MAX_FRAMES_AT_ONCE = 10;
	private static final int DELAY_BETWEEN_FRAMES_MS = 50;

	private void send(SearchInstance[] sis) throws InterruptedException
	{
		if (sis.length == 0)
			return;

		int count = 0;
		int frameSent = 0;
		for (SearchInstance si : sis)
		{

			// no more sync needed since we check/increase value only here
			int countValue = si.getUserValue().get();
			boolean skip = !isPowerOfTwo(countValue);

			if (countValue == MAX_COUNT_VALUE)
				si.getUserValue().set(MAX_FALLBACK_COUNT_VALUE);
			else
				si.getUserValue().incrementAndGet();

			// back-off
			if (skip)
				continue;

			count++;

			if (generateSearchRequestMessage(si, true, false))
				frameSent++;
			if (frameSent == MAX_FRAMES_AT_ONCE)
			{
				Thread.sleep(DELAY_BETWEEN_FRAMES_MS);
				frameSent = 0;
			}
		}

		if (count > 0)
			flushSendBuffer();
	}

	public void timerStopped() {
		// noop
	}
}
