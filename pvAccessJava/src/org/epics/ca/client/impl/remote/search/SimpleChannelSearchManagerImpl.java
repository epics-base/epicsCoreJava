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

package org.epics.ca.client.impl.remote.search;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.epics.ca.CAConstants;
import org.epics.ca.client.impl.remote.ClientContextImpl;
import org.epics.ca.impl.remote.QoS;
import org.epics.ca.impl.remote.TransportSendControl;
import org.epics.ca.util.IntHashMap;
import org.epics.pvData.misc.SerializeHelper;
import org.epics.pvData.misc.Timer.TimerCallback;
import org.epics.pvData.misc.Timer.TimerNode;
import org.epics.pvData.misc.TimerFactory;
import org.epics.pvData.pv.Field;

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
    private final IntHashMap channels = new IntHashMap();
    
    private final ArrayList<SearchInstance> immediateSearch = new ArrayList<SearchInstance>(128);
    
    private final TimerNode timerNode;
    private long lastTimeSent = 0;
    
    // 225ms +/- 25ms random
    private static final double ATOMIC_PERIOD = 0.225;
    private static final int PERIOD_JITTER_MS = 25;
    
    /**
	 * Constructor.
	 * @param context
	 */
	public SimpleChannelSearchManagerImpl(ClientContextImpl context)
	{
		this.context = context;

		// create and initialize send buffer
		sendBuffer = ByteBuffer.allocate(CAConstants.MAX_UDP_SEND);
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
			catch (Throwable th)
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

	/**
	 * Initialize send buffer.
	 */
	private void initializeSendBuffer()
	{
		// new buffer
		sendBuffer.clear();
		sendBuffer.put(CAConstants.CA_MAGIC);
		sendBuffer.put(CAConstants.CA_VERSION);
		sendBuffer.put((byte)0x80);	// data + big endian
		sendBuffer.put((byte)3);	// search
		sendBuffer.putInt(Integer.SIZE/Byte.SIZE + 1);		// "zero" payload
		sendBuffer.putInt(sequenceNumber.incrementAndGet());

		/*
		final boolean REQUIRE_REPLY = false;
		sendBuffer.put(REQUIRE_REPLY ? (byte)QoS.REPLY_REQUIRED.getMaskValue() : (byte)QoS.DEFAULT.getMaskValue());
		*/
		
		sendBuffer.put((byte)QoS.DEFAULT.getMaskValue());
		sendBuffer.putShort((short)0);	// count
	}
	
	/**
	 * Flush send buffer.
	 */
	private synchronized void flushSendBuffer()
	{
		context.getSearchTransport().send(sendBuffer);
		initializeSendBuffer();
	}
	
	private static final TransportSendControl mockTransportSendControl = new TransportSendControl() {

		@Override
		public void endMessage() {
		}

		@Override
		public void flush(boolean lastMessageCompleted) {
		}

		@Override
		public void setRecipient(InetSocketAddress sendTo) {
		}

		@Override
		public void startMessage(byte command, int ensureCapacity) {
		}

		@Override
		public void ensureBuffer(int size) {
		}

		@Override
		public void alignBuffer(int alignment) {
			throw new UnsupportedOperationException("alignBuffer not supported");
		}

		@Override
		public void flushSerializeBuffer() {
		}
		
		@Override
		public void cachedSerialize(Field field, ByteBuffer buffer) {
			// no cache
			field.serialize(buffer, this);
		}
		
	};
	
	private final static int DATA_COUNT_POSITION = CAConstants.CA_MESSAGE_HEADER_SIZE + Integer.SIZE/Byte.SIZE + 1;
    private final static int PAYLOAD_POSITION = Short.SIZE/Byte.SIZE + 2;

    /**
	 * Send search message.
	 * @return success status.  
	 */
	private static boolean generateSearchRequestMessage(SearchInstance si, ByteBuffer requestMessage, TransportSendControl control)
	{
	    short dataCount = requestMessage.getShort(DATA_COUNT_POSITION);
		
		dataCount++;
		if (dataCount >= CAConstants.MAX_SEARCH_BATCH_COUNT)
			return false;
		
		final String name = si.getChannelName();
		// not nice... 
		final int addedPayloadSize = Integer.SIZE/Byte.SIZE + (1 + Integer.SIZE/Byte.SIZE + name.length()); 
		
		if (requestMessage.remaining() < addedPayloadSize)
			return false;
		
		requestMessage.putInt(si.getChannelID());
		SerializeHelper.serializeString(name, requestMessage, control);

		requestMessage.putInt(PAYLOAD_POSITION, requestMessage.position() - CAConstants.CA_MESSAGE_HEADER_SIZE);
		requestMessage.putShort(DATA_COUNT_POSITION, dataCount);

		return true;
	}

	/**
	 * Generate (put on send buffer) search request 
	 * @param channel 
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

	/**
	 * Register channel.
	 * @param channel
	 */
	public void register(SearchInstance channel)
	{
		if (canceled)
			return;

		synchronized (channels)
		{
			// overrided if already registered
			channels.put(channel.getChannelID(), channel);
			channel.getUserValue().set(1);
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
	 * @param channel
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
	 * @param cid	client channel ID.
	 * @param seqNo	search sequence number.
	 * @param minorRevision	server minor CA revision.
	 * @param serverAddress	server address.
	 */
	public void searchResponse(int cid, int seqNo, byte minorRevision, InetSocketAddress serverAddress)
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
				si.searchResponse(minorRevision, serverAddress);
			return;
		}
		
		// then notify SearchInstance
		si.searchResponse(minorRevision, serverAddress);
	}
	
	/**
	 * Beacon anomaly detected.
	 * Boost searching of all channels.
	 */
	public void newServerDetected()
	{
		boost();
		callback();
	}

	private final static int BOOST_VALUE = 1;
	// must be power of two (so that search is done)
	private final static int MAX_COUNT_VALUE = 1 << 7;
	private final static int MAX_FALLBACK_COUNT_VALUE = (1 << 6) + 1;
	
	private void boost()
	{
		synchronized (channels) {
			if (channels.size() == 0)
				return;

			// TODO no copy when iterator is supported
			SearchInstance[] sis = new SearchInstance[channels.size()];
			channels.toArray(sis);
			
			
			for (SearchInstance si : sis)
				si.getUserValue().set(BOOST_VALUE);
		}
	}
	
	@Override
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
				channels.toArray(sis);
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
	
	@Override
	public void timerStopped() {
		// noop
	}
}
