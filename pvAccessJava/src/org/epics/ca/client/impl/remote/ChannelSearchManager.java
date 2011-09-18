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

package org.epics.ca.client.impl.remote;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.epics.ca.CAConstants;
import org.epics.ca.impl.remote.QoS;
import org.epics.ca.impl.remote.TransportSendControl;
import org.epics.ca.util.ArrayFIFO;
import org.epics.ca.util.IntHashMap;
import org.epics.pvData.misc.SerializeHelper;
import org.epics.pvData.misc.TimerFactory;
import org.epics.pvData.misc.Timer.TimerCallback;
import org.epics.pvData.misc.Timer.TimerNode;

/**
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelSearchManager {
	
	public interface SearchInstance {
		int getChannelID();
		String getChannelName();
		void unsetListOwnership();
		void addAndSetListOwnership(ArrayFIFO<SearchInstance> newOwner, int index);
		void removeAndUnsetListOwnership();
		int getOwnerIndex();
		boolean generateSearchRequestMessage(ByteBuffer buffer, TransportSendControl control);

		/**
		 * Search response from server (channel found).
		 * @param minorRevision	server minor CA revision.
		 * @param serverAddress	server address.
		 */
		 void searchResponse(byte minorRevision, InetSocketAddress serverAddress);
	}
	
	public static abstract class BaseSearchInstance implements SearchInstance {
		
		protected Object ownerLock = new Object();
		protected ArrayFIFO<SearchInstance> owner = null;
		protected int ownerIndex = -1;

		private final static int DATA_COUNT_POSITION = CAConstants.CA_MESSAGE_HEADER_SIZE + Integer.SIZE/Byte.SIZE + 1;
		private final static int PAYLOAD_POSITION = Short.SIZE/Byte.SIZE + 2;
		
		/**
		 * Send search message.
		 * @return success status.  
		 */
		public boolean generateSearchRequestMessage(ByteBuffer requestMessage, TransportSendControl control)
		{
			short dataCount = requestMessage.getShort(DATA_COUNT_POSITION);
			
			dataCount++;
			if (dataCount >= CAConstants.MAX_SEARCH_BATCH_COUNT)
				return false;
			
			final String name = getChannelName();
			// not nice... 
			final int addedPayloadSize = Integer.SIZE/Byte.SIZE + (1 + Integer.SIZE/Byte.SIZE + name.length()); 
			
			if (requestMessage.remaining() < addedPayloadSize)
				return false;
			
			requestMessage.putInt(getChannelID());
			SerializeHelper.serializeString(name, requestMessage, control);

			requestMessage.putInt(PAYLOAD_POSITION, requestMessage.position() - CAConstants.CA_MESSAGE_HEADER_SIZE);
			requestMessage.putShort(DATA_COUNT_POSITION, dataCount);
			
			return true;
		}
		
		public void unsetListOwnership() {
			synchronized (ownerLock) {
				owner = null;
			}
		}
		
		public void addAndSetListOwnership(ArrayFIFO<SearchInstance> newOwner, int index) {
			synchronized (newOwner) {
				synchronized (ownerLock) {
					newOwner.push(this);
					owner = newOwner;
					ownerIndex = index;
				}
			}
		}

		public void removeAndUnsetListOwnership() {
			if (owner == null)
				return;
			
			synchronized (owner) {
				synchronized (ownerLock) {
					if (owner != null) {
						owner.remove(this);
						owner = null;
					}
				}
			}
		}
		
		public final int getOwnerIndex() {
			synchronized (ownerLock) {
				return ownerIndex;
			}
		}
	}

	/**
	 * Max search tries per frame.
	 */
	private static final int MAX_FRAMES_PER_TRY = 64;

	private class SearchTimer implements TimerCallback
	{
		/**
		 * Number of search attempts in one frame.
		 */
		volatile int searchAttempts = 0; 

		/**
		 * Number of search responses in one frame.
		 */
		volatile int searchRespones = 0; 
		
		/**
		 * Number of frames per search try.
		 */
		double framesPerTry = 1;
		
		/**
		 * Number of frames until congestion threshold is reached.
		 */
	    double framesPerTryCongestThresh = Double.MAX_VALUE;

	    /**
	     * Start sequence number (first frame number within a search try). 
	     */
	    volatile int startSequenceNumber = 0;
	    
	    /**
	     * End sequence number (last frame number within a search try). 
	     */
	    volatile int endSequenceNumber = 0;
	    
	    /**
	     * This timer index.
	     */
	    final int timerIndex;
	    
	    /**
	     * Flag indicating whether boost is allowed. 
	     */
	    final boolean allowBoost;
	    
	    /**
	     * Flag indicating whether slow-down is allowed (for last timer). 
	     */
	    final boolean allowSlowdown;

	    /**
		 * Ordered (as inserted) list of channels with search request pending.
		 */
	    final ArrayFIFO<SearchInstance> requestPendingChannels = new ArrayFIFO<SearchInstance>();

		/**
		 * Ordered (as inserted) list of channels with search request pending.
		 */
	    final ArrayFIFO<SearchInstance> responsePendingChannels = new ArrayFIFO<SearchInstance>();

		/**
		 * Timer node.
		 * (sync on requestPendingChannels)
		 */
		TimerNode timerNode = null;

		/**
		 * Cancel this instance.
		 */
		volatile boolean canceled = false;
		
	    /**
	     * Time of last response check.
	     */
	    long timeAtResponseCheck = 0;

	    /**
		 * Constructor;
		 * @param timerIndex this timer instance index.
		 * @param allowBoost is boost allowed flag.
		 */
		public SearchTimer(int timerIndex, boolean allowBoost, boolean allowSlowdown) {
			this.timerIndex = timerIndex;
			this.allowBoost = allowBoost;
			this.allowSlowdown = allowSlowdown;
			this.timerNode = TimerFactory.createNode(this);
		}
		
		/**
		 * Shutdown this instance.
		 */
		public synchronized void shutdown()
		{
			if (canceled)
				return;
			canceled = true;

			synchronized (requestPendingChannels)
			{
				timerNode.cancel();
			}
			
			requestPendingChannels.clear();
			responsePendingChannels.clear();
		}
		
		/**
		 * Install channel.
		 * @param channel channel to be registered.
		 */
		public synchronized void installChannel(SearchInstance channel)
		{
			if (canceled)
				return;

			synchronized (requestPendingChannels)
			{
				boolean startImmediately = requestPendingChannels.isEmpty();
				channel.addAndSetListOwnership(requestPendingChannels, timerIndex);

				// start searching
				if (startImmediately) {
					timerNode.cancel();
					if (timeAtResponseCheck == 0)
						timeAtResponseCheck = System.currentTimeMillis();
					// start with some initial delay (to collect all installed requests)
					context.getTimer().scheduleAfterDelay(timerNode, 0.01);
				}
			}
		}

		/**
		 * Uninstall channel.
		 * @param channel channel to be unregistered.
		 *
		public void uninstallChannel(CAJChannel channel)
		{
			unregisterChannel(channel);
		}*/

		/**
		 * Move channels to other <code>SearchTimer</code>.
		 * @param destination where to move channels.
		 */
		public void moveChannels(SearchTimer destination)
		{
			// do not sync this, not necessary and might cause deadlock
			SearchInstance channel;
			while ((channel = responsePendingChannels.pop()) != null) {
				if (searchAttempts > 0)
					searchAttempts--;
				destination.installChannel(channel);
			}

			// bulk move
			synchronized (requestPendingChannels) {
				while (!requestPendingChannels.isEmpty())
					destination.installChannel(requestPendingChannels.pop());
			}
		}

		/* (non-Javadoc)
		 * @see org.epics.pvData.misc.Timer.TimerCallback#timerStopped()
		 */
		@Override
		public void timerStopped() {
			// noop
		}

		/* (non-Javadoc)
		 * @see org.epics.pvData.misc.Timer.TimerCallback#callback()
		 */
		@Override
		public void callback()
		{

			if (canceled)
				return;

			// if there was some success (no congestion)
			// boost search period (if necessary) for channels not recently searched
			if (allowBoost && searchRespones > 0)
			{
				synchronized (requestPendingChannels) {
					while (!requestPendingChannels.isEmpty()) {
						SearchInstance channel = requestPendingChannels.peek();
						// boost needed check
						//final int boostIndex = searchRespones >= searchAttempts * SUCCESS_RATE ? Math.min(Math.max(0, timerIndex - 1), beaconAnomalyTimerIndex) : beaconAnomalyTimerIndex;
						final int boostIndex = beaconAnomalyTimerIndex;
						if (channel.getOwnerIndex() > boostIndex)
						{
							requestPendingChannels.pop();
							channel.unsetListOwnership();
							boostSearching(channel, boostIndex);
						}
					}
				}
			}

			SearchInstance channel;
			
			// should we check results (installChannel trigger timer immediately)
			long now = System.currentTimeMillis();
			if (now - timeAtResponseCheck >= period())
			{
				timeAtResponseCheck = now;
				
				// notify about timeout (move it to other timer) 
				while ((channel = responsePendingChannels.pop()) != null) {
					if (allowSlowdown) {
						channel.unsetListOwnership();
						searchResponseTimeout(channel, timerIndex);
					}
					else {
						channel.addAndSetListOwnership(requestPendingChannels, timerIndex);
					}
				}
		
				// check search results
				if (searchAttempts > 0)
				{
		            // increase UDP frames per try if we have a good score
					if (searchRespones >= searchAttempts * SUCCESS_RATE)
					{
						// increase frames per try
		                // a congestion avoidance threshold similar to TCP is now used
						if (framesPerTry < MAX_FRAMES_PER_TRY)
						{
							if (framesPerTry < framesPerTryCongestThresh)
								framesPerTry = Math.min(2*framesPerTry, framesPerTryCongestThresh);
							else
								framesPerTry += 1.0/framesPerTry;
						}
					}
					else
					{
						// decrease frames per try, fallback
						framesPerTryCongestThresh = framesPerTry / 2.0;
						framesPerTry = 1;
					}
				
				}
			}
			
			startSequenceNumber = getSequenceNumber() + 1;
			
			searchAttempts = 0;
			searchRespones = 0;
			
			int framesSent = 0;
			int triesInFrame = 0;
			
			while (!canceled && (channel = requestPendingChannels.pop()) != null) {
				channel.unsetListOwnership();
				
				boolean requestSent = true;
				boolean allowNewFrame = (framesSent+1) < framesPerTry;
				boolean frameWasSent = generateSearchRequestMessage(channel, allowNewFrame);
				if (frameWasSent) {
					framesSent++;
					triesInFrame = 0;
					if (!allowNewFrame) {
						channel.addAndSetListOwnership(requestPendingChannels, timerIndex);
						requestSent = false;
					}
					else
						triesInFrame++;
				}
				else
					triesInFrame++;

				if (requestSent) {
					channel.addAndSetListOwnership(responsePendingChannels, timerIndex);
					if (searchAttempts < Integer.MAX_VALUE)
						searchAttempts++;
				}
				
				// limit
				if (triesInFrame == 0 && !allowNewFrame)
					break;
			}

		    // flush out the search request buffer
			if (triesInFrame > 0) {
				flushSendBuffer();
				framesSent++;
			}
			
			endSequenceNumber = getSequenceNumber();
			
			// reschedule
			synchronized (requestPendingChannels)
			{
				if (!canceled && !timerNode.isScheduled()) {
					boolean someWorkToDo = (!requestPendingChannels.isEmpty() || !responsePendingChannels.isEmpty());
					if (someWorkToDo)
						context.getTimer().scheduleAfterDelay(timerNode, period()/1000.0);
				}
			}

		}
		
		/**
		 * Search response received notification.
		 * @param responseSequenceNumber sequence number of search frame which contained search request.
		 * @param isSequenceNumberValid valid flag of <code>responseSequenceNumber</code>.
		 * @param responseTime time of search response.
		 */
		public void searchResponse(int responseSequenceNumber, boolean isSequenceNumberValid, long responseTime)
		{
			if (canceled)
				return;
		
			boolean validResponse = true;
			if (isSequenceNumberValid)
				validResponse = startSequenceNumber <= sequenceNumber && sequenceNumber <= endSequenceNumber;
			
			// update RTTE
			if (validResponse)
			{
				final long dt = responseTime - getTimeAtLastSend();
				updateRTTE(dt);
				
				if (searchRespones < Integer.MAX_VALUE)
				{
					searchRespones++;
					
					// all found, send new search requests immediately if necessary
					if (searchRespones == searchAttempts)
					{
						if (requestPendingChannels.size() > 0) 
						{
							timerNode.cancel();
							context.getTimer().scheduleAfterDelay(timerNode, 0.0);
						}
					}
				}
			}
		}
		
		
		/**
		 * Calculate search time period.
		 * @return search time period.
		 */
		public final long period()
		{
			return (long) ((1 << timerIndex) * getRTTE());
		}

	}
	
	

	/**
	 * Minimal RTT (ms).
	 */
	private static final long MIN_RTT = 32;

	/**
	 * Maximal RTT (ms).
	 */
	private static final long MAX_RTT = 2 * MIN_RTT;

	/**
	 * Rate to be considered as OK.
	 */
	private static final double SUCCESS_RATE = 0.9;

	/**
	 * Context.
	 */
	private ClientContextImpl context;

	/**
	 * Canceled flag.
	 */
	private volatile boolean canceled = false;

	/**
	 * Round-trip time (RTT) mean.
	 */
	private volatile double rttmean = MIN_RTT;

	/**
	 * Search timers.
	 * Each timer with a greater index has longer (doubled) search period.
	 */
	private SearchTimer[] timers;
	
	/**
	 * Index of a timer to be used when beacon anomaly is detected.
	 */
	private int beaconAnomalyTimerIndex;

	/**
	 * Search (datagram) sequence number.
	 */
	private volatile int sequenceNumber = 0;
	
	/**
	 * Max search period (in ms).
	 */
	private static final long MAX_SEARCH_PERIOD = 5 * 60000;  
	
	/**
	 * Max search period (in ms) - lower limit.
	 */
	private static final long MAX_SEARCH_PERIOD_LOWER_LIMIT = 60000;  

	/**
	 * Beacon anomaly search period (in ms).
	 */
	private static final long BEACON_ANOMALY_SEARCH_PERIOD = 5000;  
	
	/**
	 * Max number of timers.
	 */
	private static final int MAX_TIMERS = 18;  

	/**
	 * Send byte buffer (frame)
	 */
	private ByteBuffer sendBuffer;
	
    /**
     * Time of last frame send.
     */
    private volatile long timeAtLastSend;

    /**
     * Set of registered channels.
     */
    private IntHashMap channels = new IntHashMap();
    
    /**
	 * Constructor.
	 * @param context
	 */
	public ChannelSearchManager(ClientContextImpl context)
	{
		this.context = context;

		// create and initialize send buffer
		sendBuffer = ByteBuffer.allocate(CAConstants.MAX_UDP_SEND);
		initializeSendBuffer();

		// TODO should be configurable
		long maxPeriod = MAX_SEARCH_PERIOD;
		
		maxPeriod = Math.min(maxPeriod, MAX_SEARCH_PERIOD_LOWER_LIMIT);
		
		// calculate number of timers to reach maxPeriod (each timer period is doubled)
		double powerOfTwo = Math.log(maxPeriod / (double)MIN_RTT) / Math.log(2);
		int numberOfTimers = (int)(powerOfTwo + 1);
		numberOfTimers = Math.min(numberOfTimers, MAX_TIMERS);

		// calculate beacon anomaly timer index
		powerOfTwo = Math.log(BEACON_ANOMALY_SEARCH_PERIOD  / (double)MIN_RTT) / Math.log(2);
		beaconAnomalyTimerIndex = (int)(powerOfTwo + 1);
		beaconAnomalyTimerIndex = Math.min(beaconAnomalyTimerIndex, numberOfTimers - 1);
		
		// create timers
		timers = new SearchTimer[numberOfTimers];
		for (int i = 0; i < numberOfTimers; i++)
			timers[i] = new SearchTimer(i, i > beaconAnomalyTimerIndex, i != (numberOfTimers-1));
	}
	
	/**
	 * Cancel.
	 */
	public synchronized void cancel()
	{
		if (canceled)
			return;
		canceled = true;

		if (timers != null)
			for (int i = 0; i < timers.length; i++)
				timers[i].shutdown();
	}

	/**
	 * Initialize send buffer.
	 */
	private void initializeSendBuffer()
	{
		sequenceNumber++;

		// new buffer
		sendBuffer.clear();
		sendBuffer.put(CAConstants.CA_MAGIC);
		sendBuffer.put(CAConstants.CA_VERSION);
		sendBuffer.put((byte)0x80);	// data + big endian
		sendBuffer.put((byte)3);	// search
		sendBuffer.putInt(Integer.SIZE/Byte.SIZE + 1);		// "zero" payload
		sendBuffer.putInt(sequenceNumber);

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
		timeAtLastSend = System.currentTimeMillis();
		context.getSearchTransport().send(sendBuffer);
		initializeSendBuffer();
	}
	
	private static final TransportSendControl mockTransportSendControl = new TransportSendControl() {

		@Override
		public void endMessage() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void flush(boolean lastMessageCompleted) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setRecipient(InetSocketAddress sendTo) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void startMessage(byte command, int ensureCapacity) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void ensureBuffer(int size) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void flushSerializeBuffer() {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	/**
	 * Generate (put on send buffer) search request 
	 * @param channel 
	 * @param allowNewFrame flag indicating if new search request message is allowed to be put in new frame.
	 * @return <code>true</code> if new frame was sent.
	 */
	private synchronized boolean generateSearchRequestMessage(SearchInstance channel, boolean allowNewFrame)
	{
		boolean success = channel.generateSearchRequestMessage(sendBuffer, mockTransportSendControl);
		// buffer full, flush
		if (!success)
		{
			flushSendBuffer();
			if (allowNewFrame)
				channel.generateSearchRequestMessage(sendBuffer, mockTransportSendControl);
			return true;
		}
		
		return false;
	}

	/**
	 * Get number of registered channels.
	 * @return number of registered channels.
	 */
	public int registeredChannelCount() {
		synchronized (channels) {
			return channels.size();
		}
	}

	/**
	 * Register channel.
	 * @param channel
	 */
	public void registerChannel(SearchInstance channel)
	{
		if (canceled)
			return;

		synchronized (channels)
		{
			// ovverides if already registered
			channels.put(channel.getChannelID(), channel);
			timers[0].installChannel(channel);
		}
	}


	/**
	 * Unregister channel.
	 * @param channel
	 */
	public void unregisterChannel(SearchInstance channel)
	{
		synchronized (channels)
		{
			channels.remove(channel.getChannelID());
			channel.removeAndUnsetListOwnership();
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
			if (si != null)
				si.removeAndUnsetListOwnership();
		}
		
		if (si == null) {
			// minor hack to enable duplicate reports
			si = context.getChannel(cid);
			if (si != null)
				si.searchResponse(minorRevision, serverAddress);
			return;
		}
		
		// report success
		final int timerIndex = si.getOwnerIndex();
		timers[timerIndex].searchResponse(seqNo, seqNo != 0, System.currentTimeMillis());

		// then notify SearchInstance
		si.searchResponse(minorRevision, serverAddress);
	}
	
	/**
	 * Notify about search failure (response timeout).
	 * @param channel channel whose search failed.
	 * @param timerIndex index of timer which tries to search.
	 */
	private void searchResponseTimeout(SearchInstance channel, int timerIndex)
	{
		int newTimerIndex = Math.min(++timerIndex, timers.length - 1);
		timers[newTimerIndex].installChannel(channel);
	}

	/**
	 * Beacon anomaly detected.
	 * Boost searching of all channels.
	 */
	public void beaconAnomalyNotify()
	{
//System.out.println("[*] beaconAnomaly");
		
		for (int i = beaconAnomalyTimerIndex + 1; i < timers.length; i++)
			timers[i].moveChannels(timers[beaconAnomalyTimerIndex]);
	}
	
	/**
	 * Boost searching of a channel.
	 * @param channel channel to boost searching.
	 * @param timerIndex to what timer-index to boost
	 */
	private void boostSearching(SearchInstance channel, int timerIndex)
	{
		timers[timerIndex].installChannel(channel);
	}
	
	/**
	 * Update (recalculate) round-trip estimate.
	 * @param rtt new sample of round-trip value.
	 */
	private final void updateRTTE(long rtt)
	{
		final double error = rtt - rttmean;
		rttmean += error / 4.0;
//System.out.println("rtt:" + rtt + ", rttmean:" + rttmean);
	}
	
	/**
	 * Get round-trip estimate (in ms).
	 * @return round-trip estimate (in ms).
	 */
	private final double getRTTE() {
		return Math.min(Math.max(rttmean, MIN_RTT), MAX_RTT);
	}
	
	/**
	 * Get search (UDP) frame sequence number.
	 * @return search (UDP) frame sequence number.
	 */
	private final int getSequenceNumber()
	{
		return sequenceNumber;
	}
	
	/**
	 * Get time at last send (when sendBuffer was flushed).
	 * @return time at last send.
	 */
	private final long getTimeAtLastSend()
	{
		return timeAtLastSend;
	}
}
