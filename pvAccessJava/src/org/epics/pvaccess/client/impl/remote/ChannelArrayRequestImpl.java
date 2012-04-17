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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;

import org.epics.pvaccess.CAException;
import org.epics.pvaccess.client.ChannelArray;
import org.epics.pvaccess.client.ChannelArrayRequester;
import org.epics.pvaccess.impl.remote.QoS;
import org.epics.pvaccess.impl.remote.SerializationHelper;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;

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

	protected PVArray data;
	
	protected int offset = 0;
	protected int count = 0;
	
	protected int length = -1;
	protected int capacity = -1;

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
		
		control.startMessage((byte)14, 2*Integer.SIZE/Byte.SIZE+1);
		buffer.putInt(channel.getServerChannelID());
		buffer.putInt(ioid);
		buffer.put((byte)pendingRequest);
		
		if (QoS.INIT.isSet(pendingRequest))
		{
			// pvRequest
			SerializationHelper.serializePVRequest(buffer, control, pvRequest);
		}
		else if (QoS.GET.isSet(pendingRequest))
		{
			lock();
			try {
				SerializeHelper.writeSize(offset, buffer, control);
				SerializeHelper.writeSize(count, buffer, control);
			} finally {
				unlock();
			}
		}
		else if (QoS.GET_PUT.isSet(pendingRequest))
		{
			lock();
			try {
				SerializeHelper.writeSize(length, buffer, control);
				SerializeHelper.writeSize(capacity, buffer, control);
			} finally {
				unlock();
			}
		}
		// put
		else
		{
			lock();
			try {
				SerializeHelper.writeSize(offset, buffer, control);
				data.serialize(buffer, control, 0, count);	// put from 0 offset; TODO count out-of-bounds check?!
			} finally {
				unlock();
			}
		}
		
		stopRequest();
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.impl.remote.channelAccess.BaseRequestImpl#destroyResponse(org.epics.pvaccess.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvdata.pv.Status)
	 */
	@Override
	void destroyResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		// data available (get with destroy)
		if (QoS.GET.isSet(qos))
			normalResponse(transport, version, payloadBuffer, qos, status);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.impl.remote.channelAccess.BaseRequestImpl#initResponse(org.epics.pvaccess.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvdata.pv.Status)
	 */
	@Override
	void initResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		try
		{
			if (!status.isSuccess())
			{
				callback.channelArrayConnect(status, null, null);
				return;
			}
			
			final Field field = transport.cachedDeserialize(payloadBuffer);

			lock();
			try {
				// deserialize Field and create PVArray
				data = (PVArray)pvDataCreate.createPVField(null, field);
			} finally {
				unlock();
			}
		
			// notify
			callback.channelArrayConnect(status, this, data);
			
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
	 * @see org.epics.pvaccess.client.impl.remote.channelAccess.BaseRequestImpl#normalResponse(org.epics.pvaccess.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvaccess.CAStatus)
	 */
	@Override
	void normalResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		try
		{
			if (QoS.GET.isSet(qos))
			{
				if (!status.isSuccess())
				{
					callback.getArrayDone(status);
					return;
				}
					
				lock();
				try {
					data.deserialize(payloadBuffer, transport);
				} finally {
					unlock();
				}
	
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
	 * @see org.epics.pvaccess.client.ChannelArray#getArray(boolean, int, int)
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
			lock();
			this.offset = offset;
			this.count = count;
			unlock();

			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			stopRequest();
			callback.getArrayDone(channelNotConnected);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelArray#putArray(boolean, int, int)
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
			lock();
			this.offset = offset;
			this.count = count;
			unlock();
			
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			stopRequest();
			callback.putArrayDone(channelNotConnected);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelArray#setLength(boolean, int, int)
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
			lock();
			this.length = length;
			this.capacity = capacity;
			unlock();
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			stopRequest();
			callback.setLengthDone(channelNotConnected);
		}
	}

	/* Called on server restart...
	 * @see org.epics.pvaccess.core.SubscriptionRequest#resubscribeSubscription(org.epics.pvaccess.core.Transport)
	 */
	@Override
	public final void resubscribeSubscription(Transport transport) throws CAException {
		startRequest(QoS.INIT.getMaskValue());
		transport.enqueueSendRequest(this);
	}
	
}
