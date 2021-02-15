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

/**
 * PVA get request.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelArrayRequestImpl extends BaseRequestImpl implements ChannelArray {

	/**
	 * Response callback listener.
	 */
	protected final ChannelArrayRequester callback;

	// data container (for get)
	protected PVArray data;

	// reference store (for put)
	protected PVArray putData;

	protected int offset = 0;
	protected int count = 0;
	protected int stride = 0;

	protected int length = 0;

	public static ChannelArrayRequestImpl create(ChannelImpl channel,
			ChannelArrayRequester callback,
			PVStructure pvRequest)
	{
		ChannelArrayRequestImpl thisInstance =
			new ChannelArrayRequestImpl(channel, callback, pvRequest);
		thisInstance.activate();
		return thisInstance;
	}

	protected ChannelArrayRequestImpl(ChannelImpl channel,
			ChannelArrayRequester callback,
			PVStructure pvRequest)
	{
		super(channel, callback, pvRequest, false);

		this.callback = callback;
	}

	protected void activate()
	{
		super.activate();

		// subscribe
		try {
			resubscribeSubscription(channel.checkDestroyedAndGetTransport());
		} catch (IllegalStateException ise) {
			callback.channelArrayConnect(channelDestroyed, this, null);
			destroy(true);
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
		// get
		else if (QoS.GET.isSet(pendingRequest))
		{
			lock();
			try {
				SerializeHelper.writeSize(offset, buffer, control);
				SerializeHelper.writeSize(count, buffer, control);
				SerializeHelper.writeSize(stride, buffer, control);
			} finally {
				unlock();
			}
		}
		// setLength
		else if (QoS.GET_PUT.isSet(pendingRequest))
		{
			lock();
			try {
				SerializeHelper.writeSize(length, buffer, control);
			} finally {
				unlock();
			}
		}
		// getLength
		else if (QoS.PROCESS.isSet(pendingRequest))
		{
			// no data
		}
		// put
		else
		{
			lock();
			try {
				SerializeHelper.writeSize(offset, buffer, control);
				SerializeHelper.writeSize(stride, buffer, control);
				// TODO what about count sanity check?
				putData.serialize(buffer, control, 0, count != 0 ? count : putData.getLength());	// put from 0 offset
			} finally {
				// release reference
				putData = null;

				unlock();
			}
		}

		stopRequest();
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
				callback.channelArrayConnect(status, this, null);
				return;
			}

			final Field field = transport.cachedDeserialize(payloadBuffer);

			lock();
			try {
				// deserialize Field and create PVArray
				data = (PVArray)pvDataCreate.createPVField(field);
			} finally {
				unlock();
			}

			// notify
			callback.channelArrayConnect(status, this, data.getArray());

		}
		catch (Throwable th)
		{
			// guard PVA code from exceptions
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
					callback.getArrayDone(status, this, null);
					return;
				}

				lock();
				try {
					data.deserialize(payloadBuffer, transport);
				} finally {
					unlock();
				}

				callback.getArrayDone(okStatus, this, data);
			}
			else if (QoS.GET_PUT.isSet(qos))
			{
				callback.setLengthDone(status, this);
			}
			else if (QoS.PROCESS.isSet(qos))
			{
				int length = SerializeHelper.readSize(payloadBuffer, transport);
				callback.getLengthDone(status, this, length);
			}
			else
			{
				callback.putArrayDone(status, this);
			}
		}
		catch (Throwable th)
		{
			// guard PVA code from exceptions
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			th.printStackTrace(printWriter);
			requester.message("Unexpected exception caught: " + writer, MessageType.fatalError);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelArray#getArray(int, int)
	 */
	public void getArray(int offset, int count, int stride) {

		if (offset < 0)
			throw new IllegalArgumentException("offset < 0");
		if (count < 0)
			throw new IllegalArgumentException("count < 0");
		if (stride <= 0)
			throw new IllegalArgumentException("stride <= 0");

		if (destroyed) {
			callback.getArrayDone(destroyedStatus, this, null);
			return;
		}

		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() | QoS.GET.getMaskValue() : QoS.GET.getMaskValue())) {
			callback.getArrayDone(otherRequestPendingStatus, this, null);
			return;
		}

		try {
			lock();
			this.offset = offset;
			this.count = count;
			this.stride = stride;
			unlock();

			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			stopRequest();
			callback.getArrayDone(channelNotConnected, this, null);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelArray#putArray(PVArray, int, int, int)
	 */
	public void putArray(PVArray putArray, int offset, int count, int stride) {

		if (offset < 0)
			throw new IllegalArgumentException("offset < 0");
		if (count < 0)
			throw new IllegalArgumentException("count < 0");
		if (stride <= 0)
			throw new IllegalArgumentException("stride <= 0");

		if (destroyed) {
			callback.putArrayDone(destroyedStatus, this);
			return;
		}

		if (!putArray.getArray().equals(data.getArray()))
		{
			callback.putArrayDone(invalidPutArrayStatus, this);
			return;
		}

		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() : QoS.DEFAULT.getMaskValue())) {
			callback.putArrayDone(otherRequestPendingStatus, this);
			return;
		}

		try {
			lock();
			this.putData = putArray;
			this.offset = offset;
			this.count = count;
			this.stride = stride;
			unlock();

			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			stopRequest();
			callback.putArrayDone(channelNotConnected, this);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelArray#setLength(int)
	 */
	public void setLength(int length) {

		if (length < 0)
			throw new IllegalArgumentException("length < 0");

		if (destroyed) {
			callback.putArrayDone(destroyedStatus, this);
			return;
		}

		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() | QoS.GET_PUT.getMaskValue() : QoS.GET_PUT.getMaskValue())) {
			callback.setLengthDone(otherRequestPendingStatus, this);
			return;
		}

		try {
			lock();
			this.length = length;
			unlock();
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			stopRequest();
			callback.setLengthDone(channelNotConnected, this);
		}
	}

	public void getLength() {
		if (destroyed) {
			callback.getLengthDone(destroyedStatus, this, 0);
			return;
		}

		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() | QoS.PROCESS.getMaskValue() : QoS.PROCESS.getMaskValue())) {
			callback.getLengthDone(otherRequestPendingStatus, this, 0);
			return;
		}

		try {
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			stopRequest();
			callback.getLengthDone(channelNotConnected, this, 0);
		}
	}
}
