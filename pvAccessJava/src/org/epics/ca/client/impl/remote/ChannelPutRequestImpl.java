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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;

import org.epics.ca.CAException;
import org.epics.ca.client.ChannelPut;
import org.epics.ca.client.ChannelPutRequester;
import org.epics.ca.impl.remote.QoS;
import org.epics.ca.impl.remote.Transport;
import org.epics.ca.impl.remote.TransportSendControl;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.Status.StatusType;

/**
 * CA put request.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelPutRequestImpl extends BaseRequestImpl implements ChannelPut {

	/**
	 * Response callback listener.
	 */
	protected final ChannelPutRequester callback;

	protected final PVStructure pvRequest;
	
	protected PVStructure data = null;
	protected BitSet bitSet = null;
	
	public ChannelPutRequestImpl(ChannelImpl channel, ChannelPutRequester callback,
            PVStructure pvRequest)
	{
		super(channel, callback);
		
		if (callback == null)
			throw new IllegalArgumentException("null requester");

		if (pvRequest == null)
			throw new IllegalArgumentException("null pvRequest");
		
		this.callback = callback;
		
		this.pvRequest = pvRequest;
		
		// TODO low-overhead put
		// TODO best-effort put

		// subscribe
		try {
			resubscribeSubscription(channel.checkAndGetTransport());
		} catch (IllegalStateException ise) {
			callback.channelPutConnect(channelNotConnected, null, null, null);
		} catch (CAException e) {
			callback.channelPutConnect(statusCreate.createStatus(StatusType.ERROR, "failed to sent message over network", e), null, null, null);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.TransportSender#send(java.nio.ByteBuffer, org.epics.ca.impl.remote.TransportSendControl)
	 */
	@Override
	public void send(ByteBuffer buffer, TransportSendControl control) {
		final int pendingRequest = getPendingRequest();
		if (pendingRequest < 0)
		{
			super.send(buffer, control);
			return;
		}
		
		control.startMessage((byte)11, 2*Integer.SIZE/Byte.SIZE+1);
		buffer.putInt(channel.getServerChannelID());
		buffer.putInt(ioid);
		buffer.put((byte)pendingRequest);
		
		if (QoS.INIT.isSet(pendingRequest))
		{
			// pvRequest
			channel.getTransport().getIntrospectionRegistry().serializePVRequest(buffer, control, pvRequest);
		}
		else if (!QoS.GET.isSet(pendingRequest))
		{
			lock();
			try {
				// put
				// serialize only what has been changed
				bitSet.serialize(buffer, control);
				data.serialize(buffer, control, bitSet);
			}  finally {
				unlock();
			}
		}
		
		stopRequest();
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.impl.remote.channelAccess.BaseRequestImpl#destroyResponse(org.epics.ca.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvData.pv.Status)
	 */
	@Override
	void destroyResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		try {
			callback.putDone(status);
		} catch (Throwable th) {
			// guard CA code from exceptions
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			th.printStackTrace(printWriter);
			requester.message("Unexpected exception caught while calling a callback: " + writer, MessageType.fatalError);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.impl.remote.channelAccess.BaseRequestImpl#initResponse(org.epics.ca.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvData.pv.Status)
	 */
	@Override
	void initResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		try
		{
			if (!status.isSuccess())
			{
				callback.channelPutConnect(status, this, null, null);
				return;
			}
			
			lock();
			try {
				// create data and its bitSet
				data = transport.getIntrospectionRegistry().deserializeStructureAndCreatePVStructure(payloadBuffer, transport);
				bitSet = new BitSet(data.getNumberFields());
			} finally {
				unlock();
			}
			
			// notify
			callback.channelPutConnect(status, this, data, bitSet);
		}
		catch (Throwable th)
		{
			// guard CA code from exceptions
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			th.printStackTrace(printWriter);
			requester.message("Unexpected exception caught: " + writer, MessageType.fatalError);
		} 
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.impl.remote.channelAccess.BaseRequestImpl#normalResponse(org.epics.ca.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvData.pv.Status)
	 */
	@Override
	void normalResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		try
		{
			if (QoS.GET.isSet(qos))
			{
				if (!status.isSuccess())
				{
					callback.getDone(status);
					return;
				}
				
				lock();
				try {
					data.deserialize(payloadBuffer, transport);
				} finally {
					unlock();
				}		

				callback.getDone(status);
			}
			else
			{
				callback.putDone(status);
			}
		}
		catch (Throwable th)
		{
			// guard CA code from exceptions
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			th.printStackTrace(printWriter);
			requester.message("Unexpected exception caught while calling a callback: " + writer, MessageType.fatalError);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.ChannelPut#get()
	 */
	@Override
	public void get() {
		if (destroyed) {
			callback.getDone(destroyedStatus);
			return;
		}
		
		if (!startRequest(QoS.GET.getMaskValue())) {
			callback.getDone(otherRequestPendingStatus);
			return;
		}
		
		try {
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			stopRequest();
			callback.getDone(channelNotConnected);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.ChannelPut#put(boolean)
	 */
	@Override
	public void put(boolean lastRequest) {
		if (destroyed) {
			callback.putDone(destroyedStatus);
			return;
		}
		
		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() : QoS.DEFAULT.getMaskValue())) {
			callback.putDone(otherRequestPendingStatus);
			return;
		}
		
		try {
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			stopRequest();
			callback.putDone(channelNotConnected);
		}
	}
	
	/* Called on server restart...
	 * @see org.epics.ca.core.SubscriptionRequest#resubscribeSubscription(org.epics.ca.core.Transport)
	 */
	@Override
	public void resubscribeSubscription(Transport transport) throws CAException {
		startRequest(QoS.INIT.getMaskValue());
		transport.enqueueSendRequest(this);
	}

}
