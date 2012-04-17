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

package org.epics.pvaccess.client.impl.remote;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.epics.pvaccess.CAException;
import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.impl.remote.QoS;
import org.epics.pvaccess.impl.remote.SerializationHelper;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.misc.BitSetUtil;
import org.epics.pvdata.misc.BitSetUtilFactory;
import org.epics.pvdata.monitor.Monitor;
import org.epics.pvdata.monitor.MonitorElement;
import org.epics.pvdata.monitor.MonitorQueue;
import org.epics.pvdata.monitor.MonitorQueueFactory;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.Structure;

/**
 * CA monitor request.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelMonitorImpl extends BaseRequestImpl implements Monitor {

	/**
	 * PVField factory.
	 */
	private static final PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();

	/**
	 * Response callback listener.
	 */
	protected final MonitorRequester callback;

	protected final PVStructure pvRequest;
	
	protected AtomicBoolean started = new AtomicBoolean(false);


	private interface MonitorStrategy extends Monitor {
		void init(Structure structure);
		void response(Transport transport, ByteBuffer payloadBuffer);
	}
	
	private final MonitorStrategy monitorStrategy;

	
	public ChannelMonitorImpl(ChannelImpl channel,
			MonitorRequester callback,
	        PVStructure pvRequest)
	{
		super(channel, callback);
		
		this.callback = callback;
		
		if (callback == null)
			throw new IllegalArgumentException("null requester");

		if (pvRequest == null)
			throw new IllegalArgumentException("null pvRequest");

		this.pvRequest = pvRequest;

		
		int queueSize = 2;
		PVField pvField = pvRequest.getSubField("record.queueSize");
		if (pvField != null && (pvField instanceof PVString)) {
			PVString pvString = (PVString)pvField;
			String value = pvString.get();
			try {
				queueSize = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				callback.monitorConnect(
						PVFactory.getStatusCreate().createStatus(StatusType.ERROR, "queueSize type is not a valid integer", e),
						null, null);
				monitorStrategy = null;
				return;
			}
		}

        if (queueSize == -1)
        	monitorStrategy = new MonitorStrategyNotify();
        else if (queueSize == 0) // 0 means all (old v3 style), some sending optimization can be done (not to send bit-sets)
        	monitorStrategy = new MonitorStrategyEntire();
        else if (queueSize == 1)	
        	monitorStrategy = new MonitorStrategySingle();
        else 
        	monitorStrategy = new MonitorStrategyQueue(queueSize);
        	
        	
        // subscribe
		try {
			resubscribeSubscription(channel.checkAndGetTransport());
		} catch (IllegalStateException ise) {
			callback.monitorConnect(channelNotConnected, null, null);
		} catch (CAException e) {		
			callback.monitorConnect(statusCreate.createStatus(StatusType.ERROR, "failed to sent message over network", e), null, null);
		}
	}

	
	
	
	private final class MonitorStrategyNotify implements MonitorStrategy {
		private final MonitorElement monitorElement = MonitorQueueFactory.createMonitorElement(null);
		private volatile boolean gotMonitor = false;
		
		@Override
		public void init(Structure structure) {
			// noop
		}

		@Override
		public void response(Transport transport, ByteBuffer payloadBuffer) {
			gotMonitor = true;
			// no data, only notify
        	callback.monitorEvent(this);
		}

		@Override
		public MonitorElement poll() {
			return gotMonitor ? monitorElement : null;
		}

		@Override
		public void release(MonitorElement monitorElement) {
			gotMonitor = false;
		}

		@Override
		public Status start() {
			return okStatus;
		}

		@Override
		public Status stop() {
			return okStatus;
		}

		@Override
		public void destroy() {
			// noop
		}
	}
	
	private final class MonitorStrategyEntire implements MonitorStrategy {
		private MonitorElement monitorElement = null;
		private PVStructure monitorElementStructure = null;
        private BitSet monitorElementChangeBitSet = null;
        private BitSet monitorElementOverrunBitSet = null;
		private boolean gotMonitor = false;
		
		@Override
		public void init(Structure structure)
		{
			synchronized (this)
			{
	            PVStructure pvStructure = pvDataCreate.createPVStructure(null, structure);
				monitorElement = MonitorQueueFactory.createMonitorElement(pvStructure);

				monitorElementStructure = monitorElement.getPVStructure();
				monitorElementChangeBitSet = monitorElement.getChangedBitSet();
	        	monitorElementOverrunBitSet = monitorElement.getOverrunBitSet();
			}
		}
		
		@Override
		public void response(Transport transport, ByteBuffer payloadBuffer)
		{
			synchronized (this)
			{
				// simply deserialize and notify
				monitorElementChangeBitSet.deserialize(payloadBuffer, transport);
	        	monitorElementStructure.deserialize(payloadBuffer, transport, monitorElementChangeBitSet);
	        	monitorElementOverrunBitSet.deserialize(payloadBuffer, transport);
	        	gotMonitor = true;
				callback.monitorEvent(this);
			}
		}

		@Override
		public MonitorElement poll()
		{
            synchronized(this)
            {
            	if (!gotMonitor) return null;
                return monitorElement;
            }
		}

		@Override
		public void release(MonitorElement monitorElement)
		{
        	synchronized(this)
        	{
                gotMonitor = false;
            }
		}

		@Override
		public Status start()
		{
            synchronized(this)
            {
	    		gotMonitor = false;
				return okStatus;
            }
		}

		@Override
		public Status stop() {
			return okStatus;
		}

		@Override
		public void destroy() {
			// noop
		}
		
	}
	
	private final class MonitorStrategySingle implements MonitorStrategy {
		private MonitorElement monitorElement = null;
		private PVStructure monitorElementStructure = null;
        private BitSet monitorElementChangeBitSet = null;
        private BitSet monitorElementOverrunBitSet = null;
        private BitSet dataChangeBitSet = null;
        private BitSet dataOverrunBitSet = null;
		private boolean gotMonitor = false;
		private boolean needToCompress = false;
		
		@Override
		public void init(Structure structure)
		{
			synchronized (this)
			{
	            PVStructure pvStructure = pvDataCreate.createPVStructure(null, structure);
				monitorElement = MonitorQueueFactory.createMonitorElement(pvStructure);

				monitorElementStructure = monitorElement.getPVStructure();
				monitorElementChangeBitSet = monitorElement.getChangedBitSet();
	        	monitorElementOverrunBitSet = monitorElement.getOverrunBitSet();
	        	
	        	dataChangeBitSet = (BitSet)monitorElementChangeBitSet.clone();
	        	dataOverrunBitSet = (BitSet)monitorElementOverrunBitSet.clone();
			}
		}
		
		@Override
		public void response(Transport transport, ByteBuffer payloadBuffer)
		{
			synchronized (this)
			{
				if (!gotMonitor)
				{
					// simply deserialize and notify
					monitorElementChangeBitSet.deserialize(payloadBuffer, transport);
		        	monitorElementStructure.deserialize(payloadBuffer, transport, monitorElementChangeBitSet);
		        	monitorElementOverrunBitSet.deserialize(payloadBuffer, transport);
		        	gotMonitor = true;
					callback.monitorEvent(this);
				}
				else 
				{
					// deserialize first
					dataChangeBitSet.deserialize(payloadBuffer, transport);
		        	monitorElementStructure.deserialize(payloadBuffer, transport, dataChangeBitSet);
		        	dataOverrunBitSet.deserialize(payloadBuffer, transport);

		        	// OR local overrun
		        	// TODO should work only on uncompressed
					monitorElementOverrunBitSet.or_and(dataChangeBitSet, monitorElementChangeBitSet);

					// OR new changes
					monitorElementChangeBitSet.or(dataChangeBitSet);
					
					// OR remote overrun
					monitorElementOverrunBitSet.or(dataOverrunBitSet);
				}
			}
		}

		@Override
		public MonitorElement poll()
		{
            synchronized(this)
            {
            	if (!gotMonitor) return null;
            	
            	// compress if needed
            	if (needToCompress) {
	            	bitSetUtil.compress(monitorElementChangeBitSet, monitorElementStructure);
					bitSetUtil.compress(monitorElementOverrunBitSet, monitorElementStructure);
					needToCompress = false;
            	}
            	
                return monitorElement;
            }
		}

		@Override
		public void release(MonitorElement monitorElement)
		{
        	synchronized(this)
        	{
                gotMonitor = false;
            }
		}

		@Override
		public Status start()
		{
            synchronized(this)
            {
	    		gotMonitor = false;
	    		monitorElementChangeBitSet.clear();
	    		monitorElementOverrunBitSet.clear();
				return okStatus;
            }
		}

		@Override
		public Status stop() {
			return okStatus;
		}

		@Override
		public void destroy() {
			// noop
		}
		
	}
	
    private static final BitSetUtil bitSetUtil = BitSetUtilFactory.getCompressBitSet();
    private static final Convert convert = ConvertFactory.getConvert();

    // TODO fix sync
    private final class MonitorStrategyQueue implements MonitorStrategy {
		private final int queueSize;

		private MonitorElement monitorElement = null;
		private BitSet bitSet1 = null;
		private BitSet bitSet2 = null;
	    private boolean overrunInProgress = false;

	    private MonitorQueue monitorQueue = null;
	    
	    private final Object monitorSync = new Object();


		public MonitorStrategyQueue(int queueSize)
		{
			if (queueSize <= 1)
				throw new IllegalArgumentException("queueSize <= 1");
			
			this.queueSize = queueSize;
		}
		
		@Override
		public void init(Structure structure)
		{
			synchronized (monitorSync)
			{
	    		MonitorElement[] monitorElements = new MonitorElement[queueSize];
	            for(int i=0; i<queueSize; i++) {
	                PVStructure pvNew = pvDataCreate.createPVStructure(null, structure);
	                monitorElements[i] = MonitorQueueFactory.createMonitorElement(pvNew);
	            }
	            monitorQueue = MonitorQueueFactory.create(monitorElements);
			}
		}
		
		@Override
		public void response(Transport transport, ByteBuffer payloadBuffer)
		{
			boolean notify = false;
			
			synchronized (monitorSync)
			{
	            // if in overrun mode, check if some is free
	            if (overrunInProgress)
	            {
	            	MonitorElement newElement = monitorQueue.getFree();
	            	if (newElement != null)
	            	{
	            		// take new, put current in use
	    				final PVStructure pvStructure = monitorElement.getPVStructure();
			            convert.copy(pvStructure, newElement.getPVStructure());

			            bitSetUtil.compress(monitorElement.getChangedBitSet(), pvStructure);
			            bitSetUtil.compress(monitorElement.getOverrunBitSet(), pvStructure);
	            		monitorQueue.setUsed(monitorElement);

	            		monitorElement = newElement;
	            		notify = true;

	            		overrunInProgress = false;
	            	}
	            }
			}
			
			if (notify)
				callback.monitorEvent(this);

	        synchronized (monitorSync)
			{

	            // setup current fields
				final PVStructure pvStructure = monitorElement.getPVStructure();
	            final BitSet changedBitSet = monitorElement.getChangedBitSet();
	            final BitSet overrunBitSet = monitorElement.getOverrunBitSet();

	            // special treatment if in overrun state
	            if (overrunInProgress)
	            {
	            	// lazy init
	            	if (bitSet1 == null) bitSet1 = new BitSet(changedBitSet.size());
	            	if (bitSet2 == null) bitSet2 = new BitSet(overrunBitSet.size());
	            	
	            	bitSet1.deserialize(payloadBuffer, transport);
					pvStructure.deserialize(payloadBuffer, transport, bitSet1);
					bitSet2.deserialize(payloadBuffer, transport);

					// OR local overrun
					// TODO this does not work perfectly... uncompressed bitSets should be used!!!
					overrunBitSet.or_and(changedBitSet, bitSet1);

					// OR remove change
					changedBitSet.or(bitSet1);

					// OR remote overrun
					overrunBitSet.or(bitSet2);
	            }
	            else
	            {
	            	// deserialize changedBitSet and data, and overrun bit set
		            changedBitSet.deserialize(payloadBuffer, transport);
					pvStructure.deserialize(payloadBuffer, transport, changedBitSet);
					overrunBitSet.deserialize(payloadBuffer, transport);
	            }
	            
				// prepare next free (if any)
				MonitorElement newElement = monitorQueue.getFree();
	            if (newElement == null) {
	                overrunInProgress = true;
	                return;
	            }
	            
	            // if there was overrun in progress we manipulated bitSets... compress them
	            if (overrunInProgress) {
		            bitSetUtil.compress(changedBitSet, pvStructure);
		            bitSetUtil.compress(overrunBitSet, pvStructure);

		            overrunInProgress = false;
	            }
	            
	            convert.copy(pvStructure, newElement.getPVStructure());
     
	            monitorQueue.setUsed(monitorElement);

	            monitorElement = newElement;
			}
	        
        	callback.monitorEvent(this);
		}

		@Override
		public MonitorElement poll()
		{
            synchronized(monitorSync) {
            	final MonitorElement retVal = monitorQueue.getUsed();
            	if (retVal != null)
            		return retVal;
            	
	            // if in overrun mode and we have free, make it as last element
	            if (overrunInProgress)
	            {
	            	MonitorElement newElement = monitorQueue.getFree();
	            	if (newElement != null)
	            	{
	            		// take new, put current in use
	    				final PVStructure pvStructure = monitorElement.getPVStructure();
			            convert.copy(pvStructure, newElement.getPVStructure());

			            bitSetUtil.compress(monitorElement.getChangedBitSet(), pvStructure);
			            bitSetUtil.compress(monitorElement.getOverrunBitSet(), pvStructure);
	            		monitorQueue.setUsed(monitorElement);

	            		monitorElement = newElement;

	            		overrunInProgress = false;
	            		
	            		return monitorQueue.getUsed();
	            	}
	            	else
	            		return null;		// should never happen since queueSize >= 2, but (too) smart client can do this
	            }
	            else
	            	return null;
            }
		}

		@Override
		public void release(MonitorElement monitorElement)
		{
	        synchronized(monitorSync) {
	            monitorQueue.releaseUsed(monitorElement);
	        }
		}

		@Override
		public Status start()
		{
			synchronized (monitorSync) {
				overrunInProgress = false;
	            monitorQueue.clear();
	            monitorElement = monitorQueue.getFree();
			}
			return okStatus;
		}

		@Override
		public Status stop() {
			return okStatus;
		}

		@Override
		public void destroy() {
			// noop
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.TransportSender#send(java.nio.ByteBuffer, org.epics.pvaccess.impl.remote.TransportSendControl)
	 */
	@Override
	public void send(ByteBuffer buffer, TransportSendControl control) {
		final int pendingRequest = getPendingRequest();
		if (pendingRequest < 0)
		{
			super.send(buffer, control);
			return;
		}
		
		control.startMessage((byte)13, 2*Integer.SIZE/Byte.SIZE+1);
		buffer.putInt(channel.getServerChannelID());
		buffer.putInt(ioid);
		buffer.put((byte)pendingRequest);
		
		if (QoS.INIT.isSet(pendingRequest))
		{
			// pvRequest
			SerializationHelper.serializePVRequest(buffer, control, pvRequest);
		}

		stopRequest();
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.impl.remote.channelAccess.BaseRequestImpl#destroyResponse(org.epics.pvaccess.core.Transport, byte, java.nio.ByteBuffer, byte qos, org.epics.pvdata.pv.Status)
	 */
	@Override
	void destroyResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		// data available
		// TODO if (QoS.GET.isSet(qos))
		{
			normalResponse(transport, version, payloadBuffer, qos, status);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.impl.remote.channelAccess.BaseRequestImpl#initResponse(org.epics.pvaccess.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvdata.pv.Status)
	 */
	@Override
	void initResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		if (!status.isSuccess())
		{
			callback.monitorConnect(status, this, null);
			return;
		}
		
		// deserialize Structure...
		final Structure structure = (Structure)transport.cachedDeserialize(payloadBuffer);
		monitorStrategy.init(structure);

		// notify
		callback.monitorConnect(okStatus, this, structure);
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.impl.remote.channelAccess.BaseRequestImpl#normalResponse(org.epics.pvaccess.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvdata.pv.Status)
	 */
	@Override
	void normalResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		if (QoS.GET.isSet(qos))
		{
			// TODO not supported by IF yet...
		}
		else
		{
			monitorStrategy.response(transport, payloadBuffer);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.core.DataResponse#response(org.epics.pvaccess.core.Transport, byte, java.nio.ByteBuffer)
	 */
	public void response(Transport transport, byte version, ByteBuffer payloadBuffer) {
		boolean cancel = false;
		try
		{	
			transport.ensureData(1);
			final byte qos = payloadBuffer.get();

			if (QoS.INIT.isSet(qos))
			{
				final Status status = statusCreate.deserializeStatus(payloadBuffer, transport);
				initResponse(transport, version, payloadBuffer, qos, status);

				if (started.get())
					start();
			}
			else if (QoS.DESTROY.isSet(qos))
			{
				final Status status = statusCreate.deserializeStatus(payloadBuffer, transport);
				remotelyDestroyed = true;
				cancel = true;

				destroyResponse(transport, version, payloadBuffer, qos, status);
			}
			else
			{
				// status is always OK
				normalResponse(transport, version, payloadBuffer, qos, okStatus);
			}
		}
		finally
		{
			// always cancel request
			if (cancel)
				cancel();
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelMonitor#start()
	 */
	@Override
	public Status start() {
		if (destroyed)
			return destroyedStatus;
		
		monitorStrategy.start();
		
		try {
			// start == process + get
			startRequest(QoS.PROCESS.getMaskValue() | QoS.GET.getMaskValue());
			channel.checkAndGetTransport().enqueueSendRequest(this);
			started.set(true);
			return okStatus;
		} catch (IllegalStateException ise) {
			stopRequest();
			return channelNotConnected;
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelMonitor#stop()
	 */
	@Override
	public Status stop() {
		if (destroyed)
			return destroyedStatus;
		
		monitorStrategy.stop();
		
		try {
			// stop == process + no get
			startRequest(QoS.PROCESS.getMaskValue());
			channel.checkAndGetTransport().enqueueSendRequest(this);
			started.set(false);
			return okStatus;
		} catch (IllegalStateException ise) {
			stopRequest();
			return channelNotConnected;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvdata.monitor.Monitor#poll()
	 */
	@Override
	public MonitorElement poll() {
		return monitorStrategy.poll();
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.monitor.Monitor#release(org.epics.pvdata.monitor.MonitorElement)
	 */
	@Override
	public void release(MonitorElement monitorElement) {
		monitorStrategy.release(monitorElement);
	}

	/* Called on server restart...
	 * @see org.epics.pvaccess.core.SubscriptionRequest#resubscribeSubscription(org.epics.pvaccess.core.Transport)
	 */
	@Override
	public final void resubscribeSubscription(Transport transport) throws CAException {
		startRequest(QoS.INIT.getMaskValue());
		transport.enqueueSendRequest(this);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.core.SubscriptionRequest#updateSubscription()
	 */
	@Override
	public void updateSubscription() throws CAException {
		/*
		// get latest value
		try {
			startRequest(QoS.GET.getMaskValue());
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (Throwable th) {
			// TODO how to report
			requester.message(e.toString(), MessageType.error);
		}
		*/
		// TODO
	}
 
}
