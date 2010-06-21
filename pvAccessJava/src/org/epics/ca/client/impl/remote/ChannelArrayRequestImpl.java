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

import java.nio.ByteBuffer;

import org.epics.ca.CAException;
import org.epics.ca.client.ChannelArray;
import org.epics.ca.client.ChannelArrayRequester;
import org.epics.ca.impl.remote.QoS;
import org.epics.ca.impl.remote.Transport;
import org.epics.ca.impl.remote.TransportSendControl;
import org.epics.pvData.misc.SerializeHelper;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.Status.StatusType;

/**
 * CA get request.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelArrayRequestImpl extends BaseRequestImpl implements ChannelArray {

	/**
	 * Response callback listener.
	 */
	protected final ChannelArrayRequester callback;

	protected final PVStructure pvRequest;

	protected volatile PVArray data;
	
	protected volatile int offset = 0;
	protected volatile int count = 0;
	
	protected volatile int length = -1;
	protected volatile int capacity = -1;

	public ChannelArrayRequestImpl(ChannelImpl channel,
			ChannelArrayRequester callback,
			PVStructure pvRequest)
	{
		super(channel, callback);
		
		if (callback == null)
			throw new IllegalArgumentException("null requester");
		
		if (pvRequest == null)
			throw new IllegalArgumentException("null pvRequest");

		this.callback = callback;
		this.pvRequest = pvRequest;

		// subscribe
		try {
			resubscribeSubscription(channel.checkAndGetTransport());
		} catch (IllegalStateException ise) {
			callback.channelArrayConnect(channelNotConnected, null, null);
		} catch (CAException e) {
			callback.channelArrayConnect(statusCreate.createStatus(StatusType.ERROR, "failed to sent message over network", e), null, null);
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
		
		control.startMessage((byte)14, 2*Integer.SIZE/Byte.SIZE+1);
		buffer.putInt(channel.getServerChannelID());
		buffer.putInt(ioid);
		buffer.put((byte)pendingRequest);
		
		if (QoS.INIT.isSet(pendingRequest))
		{
			// pvRequest
			channel.getTransport().getIntrospectionRegistry().serializePVRequest(buffer, control, pvRequest);
		}
		else if (QoS.GET.isSet(pendingRequest))
		{
			SerializeHelper.writeSize(offset, buffer, control);
			SerializeHelper.writeSize(count, buffer, control);
		}
		else if (QoS.GET_PUT.isSet(pendingRequest))
		{
			SerializeHelper.writeSize(length, buffer, control);
			SerializeHelper.writeSize(capacity, buffer, control);
		}
		// put
		else
		{
			SerializeHelper.writeSize(offset, buffer, control);
			data.serialize(buffer, control, 0, count);	// put from 0 offset; TODO count out-of-bounds check?!
		}
		
		stopRequest();
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.impl.remote.channelAccess.BaseRequestImpl#destroyResponse(org.epics.ca.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvData.pv.Status)
	 */
	@Override
	void destroyResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		// data available (get with destroy)
		if (QoS.GET.isSet(qos))
			normalResponse(transport, version, payloadBuffer, qos, status);
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.impl.remote.channelAccess.BaseRequestImpl#initResponse(org.epics.ca.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvData.pv.Status)
	 */
	@Override
	void initResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		if (!status.isSuccess())
		{
			callback.channelArrayConnect(status, null, null);
			return;
		}
		
		// deserialize Field and create PVArray
		final Field field = transport.getIntrospectionRegistry().deserialize(payloadBuffer, transport);
		data = (PVArray)pvDataCreate.createPVField(null, field);

		// notify
		callback.channelArrayConnect(status, this, data);
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.impl.remote.channelAccess.BaseRequestImpl#normalResponse(org.epics.ca.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.ca.CAStatus)
	 */
	@Override
	void normalResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		if (QoS.GET.isSet(qos))
		{
			if (!status.isSuccess())
			{
				callback.getArrayDone(status);
				return;
			}
			
			data.deserialize(payloadBuffer, transport);
			callback.getArrayDone(okStatus);
		}
		else if (QoS.GET_PUT.isSet(qos))
		{
			callback.setLengthDone(status);
		}
		else
		{
			callback.putArrayDone(status);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.ChannelArray#getArray(boolean, int, int)
	 */
	@Override
	public void getArray(boolean lastRequest, int offset, int count) {
		if (destroyed) {
			callback.getArrayDone(destroyedStatus);
			return;
		}

		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() | QoS.GET.getMaskValue() : QoS.GET.getMaskValue())) {
			callback.getArrayDone(otherRequestPendingStatus);
			return;
		}
		
		try {
			this.offset = offset;
			this.count = count;
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			callback.getArrayDone(channelNotConnected);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.ChannelArray#putArray(boolean, int, int)
	 */
	@Override
	public void putArray(boolean lastRequest, int offset, int count) {
		if (destroyed) {
			callback.putArrayDone(destroyedStatus);
			return;
		}

		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() : QoS.DEFAULT.getMaskValue())) {
			callback.putArrayDone(otherRequestPendingStatus);
			return;
		}
		
		try {
			this.offset = offset;
			this.count = count;
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			callback.putArrayDone(channelNotConnected);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.epics.ca.client.ChannelArray#setLength(boolean, int, int)
	 */
	@Override
	public void setLength(boolean lastRequest, int length, int capacity) {
		if (destroyed) {
			callback.putArrayDone(destroyedStatus);
			return;
		}

		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() | QoS.GET_PUT.getMaskValue() : QoS.GET_PUT.getMaskValue())) {
			callback.setLengthDone(otherRequestPendingStatus);
			return;
		}
		
		try {
			this.length = length;
			this.capacity = capacity;
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			callback.setLengthDone(channelNotConnected);
		}
	}

	/* Called on server restart...
	 * @see org.epics.ca.core.SubscriptionRequest#resubscribeSubscription(org.epics.ca.core.Transport)
	 */
	@Override
	public final void resubscribeSubscription(Transport transport) throws CAException {
		startRequest(QoS.INIT.getMaskValue());
		transport.enqueueSendRequest(this);
	}
	
}
