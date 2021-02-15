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

import org.epics.pvaccess.client.ChannelPutGet;
import org.epics.pvaccess.client.ChannelPutGetRequester;
import org.epics.pvaccess.impl.remote.QoS;
import org.epics.pvaccess.impl.remote.SerializationHelper;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;

/**
 * PVA putGet request.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelPutGetRequestImpl extends BaseRequestImpl implements ChannelPutGet {

    /**
	 * Response callback listener.
	 */
	protected final ChannelPutGetRequester callback;

	// TODO !!! give access to it, or lazy initialize
	// put data container
	protected PVStructure putData = null;
	protected BitSet putDataBitSet = null;

	// get data container
	protected PVStructure getData = null;
	protected BitSet getDataBitSet = null;

	// putGet reference store
	protected PVStructure putPutData = null;
	protected BitSet putPutDataBitSet = null;

	public static ChannelPutGetRequestImpl create(ChannelImpl channel,
			ChannelPutGetRequester callback,
	        PVStructure pvRequest)
	{
		ChannelPutGetRequestImpl thisInstance =
			new ChannelPutGetRequestImpl(channel, callback, pvRequest);
		thisInstance.activate();
		return thisInstance;
	}

	protected ChannelPutGetRequestImpl(ChannelImpl channel,
			ChannelPutGetRequester callback,
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
			callback.channelPutGetConnect(channelDestroyed, this, null, null);
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

		control.startMessage((byte)12, 2*Integer.SIZE/Byte.SIZE+1);
		buffer.putInt(channel.getServerChannelID());
		buffer.putInt(ioid);
		if (pendingRequest != QoS.INIT.getMaskValue())
			buffer.put((byte)pendingRequest);

		if (QoS.INIT.isSet(pendingRequest))
		{
			// qos
			buffer.put((byte)QoS.INIT.getMaskValue());

			// pvRequest
			SerializationHelper.serializePVRequest(buffer, control, pvRequest);
		}
		else if (QoS.GET.isSet(pendingRequest) || QoS.GET_PUT.isSet(pendingRequest)) {
			// noop
		}
		else
		{
			lock();
			try {
				putPutDataBitSet.serialize(buffer, control);
				putPutData.serialize(buffer, control, putPutDataBitSet);
			} finally {
				// release references
				putPutData = null;
				putPutDataBitSet = null;

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
				callback.channelPutGetConnect(status, this, null, null);
				return;
			}

			lock();
			try {
				putData = SerializationHelper.deserializeStructureAndCreatePVStructure(payloadBuffer, transport, putData);
				putDataBitSet = createBitSetFor(putData, putDataBitSet);
				getData = SerializationHelper.deserializeStructureAndCreatePVStructure(payloadBuffer, transport, getData);
				getDataBitSet = createBitSetFor(putData, getDataBitSet);
			} finally {
				unlock();
			}

			// notify
			callback.channelPutGetConnect(status, this, putData.getStructure(), getData.getStructure());
		} catch (Throwable th) {
			// guard PVA code from exceptions
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			th.printStackTrace(printWriter);
			requester.message("Unexpected exception caught: " + writer, MessageType.fatalError);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.impl.remote.channelAccess.BaseRequestImpl#normalResponse(org.epics.pvaccess.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvdata.pv.Status)
	 */
	@Override
	void normalResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		try
		{

			if (QoS.GET.isSet(qos))
			{
				if (!status.isSuccess())
				{
					callback.getGetDone(status, this, null, null);
					return;
				}

				lock();
				try {
					// deserialize get data
					getDataBitSet.deserialize(payloadBuffer, transport);
					getData.deserialize(payloadBuffer, transport, getDataBitSet);
				} finally {
					unlock();
				}

				callback.getGetDone(status, this, getData, getDataBitSet);
			}
			else if (QoS.GET_PUT.isSet(qos))
			{
				if (!status.isSuccess())
				{
					callback.getPutDone(status, this, null, null);
					return;
				}

				lock();
				try {
					// deserialize put data
					putDataBitSet.deserialize(payloadBuffer, transport);
					putData.deserialize(payloadBuffer, transport, putDataBitSet);
				} finally {
					unlock();
				}

				callback.getPutDone(status, this, putData, putDataBitSet);
			}
			else
			{
				if (!status.isSuccess())
				{
					callback.putGetDone(status, this, null, null);
					return;
				}

				lock();
				try {
					// deserialize data
					getDataBitSet.deserialize(payloadBuffer, transport);
					getData.deserialize(payloadBuffer, transport, getDataBitSet);
				} finally {
					unlock();
				}

				callback.putGetDone(status, this, getData, getDataBitSet);
			}
		} catch (Throwable th) {
			// guard PVA code from exceptions
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			th.printStackTrace(printWriter);
			requester.message("Unexpected exception caught: " + writer, MessageType.fatalError);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelPutGet#putGet(PVStructure, BitSet)
	 */

	public void putGet(PVStructure pvPutStructure, BitSet bitSet) {
		if (destroyed) {
			callback.putGetDone(destroyedStatus, this, null, null);
			return;
		}

		if (!putData.getStructure().equals(pvPutStructure.getStructure()))
		{
			callback.putGetDone(invalidPutStructureStatus, this, null, null);
			return;
		}

		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() : QoS.DEFAULT.getMaskValue())) {
			callback.putGetDone(otherRequestPendingStatus, this, null, null);
			return;
		}

		try {
			lock();
			putPutData = pvPutStructure;
			putPutDataBitSet = bitSet;
			unlock();
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			stopRequest();
			callback.putGetDone(channelNotConnected, this, null, null);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelPutGet#getGet()
	 */
	public void getGet() {
		if (destroyed) {
			callback.getGetDone(destroyedStatus, this, null, null);
			return;
		}

		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() | QoS.GET.getMaskValue() : QoS.GET.getMaskValue())) {
			callback.getGetDone(otherRequestPendingStatus, this, null, null);
			return;
		}

		try {
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			stopRequest();
			callback.getGetDone(channelNotConnected, this, null, null);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelPutGet#getPut()
	 */
	public void getPut() {
		if (destroyed) {
			callback.getPutDone(destroyedStatus, this, null, null);
			return;
		}

		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() | QoS.GET_PUT.getMaskValue() : QoS.GET_PUT.getMaskValue())) {
			callback.getPutDone(otherRequestPendingStatus, this, null, null);
			return;
		}

		try {
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			stopRequest();
			callback.getPutDone(channelNotConnected, this, null, null);
		}
	}

}
