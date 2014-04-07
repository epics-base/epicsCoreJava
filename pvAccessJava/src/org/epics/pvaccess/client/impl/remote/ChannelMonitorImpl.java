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

import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.impl.remote.QoS;
import org.epics.pvaccess.impl.remote.SerializationHelper;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.misc.BitSetUtil;
import org.epics.pvdata.misc.BitSetUtilFactory;
import org.epics.pvdata.monitor.Monitor;
import org.epics.pvdata.monitor.MonitorElement;
import org.epics.pvdata.monitor.MonitorQueue;
import org.epics.pvdata.monitor.MonitorQueueFactory;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.Structure;

/**
 * PVA monitor request.
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

	protected AtomicBoolean started = new AtomicBoolean(false);


	private interface ElementQueue extends Monitor {
		void init(Structure structure);
		void response(Transport transport, ByteBuffer payloadBuffer);
	}
	
	private final ElementQueue elementQueue;

	public static ChannelMonitorImpl create(ChannelImpl channel,
			MonitorRequester callback,
	        PVStructure pvRequest)
	{
		ChannelMonitorImpl thisInstance = 
			new ChannelMonitorImpl(channel, callback, pvRequest);
		thisInstance.activate();
		return thisInstance;
	}
	
	protected ChannelMonitorImpl(ChannelImpl channel,
			MonitorRequester callback,
	        PVStructure pvRequest)
	{
		super(channel, callback, pvRequest, false);
		
		this.callback = callback;
		
		int queueSize = 2;
		PVField pvField = pvRequest.getSubField("record._options");
		if(pvField!=null) {
		    PVStructure pvOptions = (PVStructure)pvField;
		    pvField = pvOptions.getSubField("queueSize");
		    if(pvField!=null) {
		        PVString pvString = (PVString)pvField;
		        String value = pvString.get();
	            try {
	                queueSize = Integer.parseInt(value);
	            } catch (NumberFormatException e) {
	                callback.monitorConnect(
	                        PVFactory.getStatusCreate().createStatus(StatusType.ERROR, "queueSize type is not a valid integer", e),
	                        this, null);
	                elementQueue = null;
	                destroy(true);
	                return;
	            }
		    }
		}
		
        if(queueSize<2) queueSize = 2;
        elementQueue = new MultipleElementQueue(queueSize);
	}


	protected void activate()
	{
		super.activate();
		
        // subscribe
		try {
			resubscribeSubscription(channel.checkDestroyedAndGetTransport());
		} catch (IllegalStateException ise) {
			callback.monitorConnect(channelDestroyed, this, null);
			destroy(true);
		}
	}
		
    private static final BitSetUtil bitSetUtil = BitSetUtilFactory.getCompressBitSet();

    private final class MultipleElementQueue implements ElementQueue {
		private final int queueSize;
		private boolean queueIsFull = false;
	    private MonitorQueue monitorQueue = null;
	    private BitSet changedBitSet = null;
	    private BitSet overrunBitSet = null;
	    private MonitorElement latestMonitorElement = null;
	    

		public MultipleElementQueue(int queueSize)
		{
			this.queueSize = queueSize;
		}
		
		@Override
		public void init(Structure structure)
		{
			synchronized (this)
			{
			    int nfields = 0;
		        MonitorElement[] monitorElements = new MonitorElement[queueSize];
		        for(int i=0; i<queueSize; i++) {
		                PVStructure pvNew = pvDataCreate.createPVStructure(structure);
		                if(nfields==0) nfields = pvNew.getNumberFields();
		                monitorElements[i] = MonitorQueueFactory.createMonitorElement(pvNew);
		            }
		            monitorQueue = MonitorQueueFactory.create(monitorElements);
		            changedBitSet = new BitSet(nfields);
		            overrunBitSet = new BitSet(nfields);
			}
		}
		
		@Override
		public void response(Transport transport, ByteBuffer payloadBuffer)
		{
			synchronized (this)
			{
	            if (queueIsFull) {
	                MonitorElement monitorElement = latestMonitorElement;
                    PVStructure pvStructure = monitorElement.getPVStructure();
                    changedBitSet.deserialize(payloadBuffer, transport);
                    pvStructure.deserialize(
                         payloadBuffer,
                         transport,
                         changedBitSet);
                    overrunBitSet.deserialize(payloadBuffer, transport);
                    monitorElement.getChangedBitSet().or(changedBitSet);
                    monitorElement.getOverrunBitSet().or(changedBitSet);
                    
                    changedBitSet.clear();
                    overrunBitSet.clear();
                    return;

	            }
	            MonitorElement monitorElement = monitorQueue.getFree();
                if(monitorElement==null) {
                    throw new IllegalStateException("RealQueue::dataChanged() logic error");
                }
                if(monitorQueue.getNumberFree()==0){
                    queueIsFull = true;
                    latestMonitorElement = monitorElement;
                }
                PVStructure pvStructure = monitorElement.getPVStructure();
                changedBitSet.deserialize(payloadBuffer, transport);
                pvStructure.deserialize(
                     payloadBuffer,
                     transport,
                     changedBitSet);
                overrunBitSet.deserialize(payloadBuffer, transport);
                bitSetUtil.compress(changedBitSet,pvStructure);
                bitSetUtil.compress(overrunBitSet,pvStructure);
                monitorElement.getChangedBitSet().clear();
                monitorElement.getChangedBitSet().or(changedBitSet);
                monitorElement.getOverrunBitSet().clear();
                monitorElement.getOverrunBitSet().or(overrunBitSet);
                changedBitSet.clear();
                overrunBitSet.clear();
                monitorQueue.setUsed(monitorElement);

			}
        	callback.monitorEvent(this);
		}

		@Override
		public MonitorElement poll()
		{
            synchronized(this) {
            	return monitorQueue.getUsed();
            }
		}

		@Override
		public void release(MonitorElement currentElement)
		{
	        synchronized(this) {
	            if(queueIsFull) {
                    MonitorElement monitorElement = latestMonitorElement;
                    PVStructure pvStructure = monitorElement.getPVStructure();
                    bitSetUtil.compress(monitorElement.getChangedBitSet(),pvStructure);
                    bitSetUtil.compress(monitorElement.getOverrunBitSet(),pvStructure);
                    queueIsFull = false;
                    latestMonitorElement = null;
                }
                monitorQueue.releaseUsed(currentElement);
	        }
		}

		@Override
		public Status start()
		{
			synchronized (this) {
				queueIsFull = false;
	            monitorQueue.clear();
	            changedBitSet.clear();
	            overrunBitSet.clear();
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
		elementQueue.init(structure);

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
			elementQueue.response(transport, payloadBuffer);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.core.DataResponse#response(org.epics.pvaccess.core.Transport, byte, java.nio.ByteBuffer)
	 */
	public void response(Transport transport, byte version, ByteBuffer payloadBuffer) {
		boolean destroy = false;
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
				destroy = true;

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
			if (destroy)
				destroy();
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelMonitor#start()
	 */
	@Override
	public Status start() {
		if (destroyed)
			return destroyedStatus;
		// TODO not initialized (aka created) check?!!
		
		elementQueue.start();
		
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
		// TODO not initialized (aka created) check?!!
		
		elementQueue.stop();
		
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
		return elementQueue.poll();
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.monitor.Monitor#release(org.epics.pvdata.monitor.MonitorElement)
	 */
	@Override
	public void release(MonitorElement monitorElement) {
		elementQueue.release(monitorElement);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.core.SubscriptionRequest#updateSubscription()
	 *
	@Override
	public void updateSubscription() {
		// get latest value
		try {
			startRequest(QoS.GET.getMaskValue());
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (Throwable th) {
			// TODO how to report
			requester.message(e.toString(), MessageType.error);
		}
	}
	*/
 
}
