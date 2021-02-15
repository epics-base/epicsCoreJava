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

import org.epics.pvaccess.client.ChannelPut;
import org.epics.pvaccess.client.ChannelPutRequester;
import org.epics.pvaccess.impl.remote.QoS;
import org.epics.pvaccess.impl.remote.SerializationHelper;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;

/**
 * PVA put request.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelPutRequestImpl extends BaseRequestImpl implements ChannelPut {

	/**
	 * Response callback listener.
	 */
	protected final ChannelPutRequester callback;

	// get container
	protected PVStructure data = null;
	protected BitSet bitSet = null;

	// put reference store
	protected PVStructure pvPutStructure = null;
	protected BitSet putBitSet = null;

	public static ChannelPutRequestImpl create(ChannelImpl channel,
			ChannelPutRequester callback,
            PVStructure pvRequest)
	{
		ChannelPutRequestImpl thisInstance =
			new ChannelPutRequestImpl(channel, callback, pvRequest);
		thisInstance.activate();
		return thisInstance;
	}

	protected ChannelPutRequestImpl(ChannelImpl channel,
			ChannelPutRequester callback,
            PVStructure pvRequest)
	{
		super(channel, callback, pvRequest, false);

		this.callback = callback;

		// TODO low-overhead put
		// TODO best-effort put
	}


	protected void activate()
	{
		super.activate();

		// subscribe
		try {
			resubscribeSubscription(channel.checkDestroyedAndGetTransport());
		} catch (IllegalStateException ise) {
			callback.channelPutConnect(channelDestroyed, this, null);
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

		control.startMessage((byte)11, 2*Integer.SIZE/Byte.SIZE+1);
		buffer.putInt(channel.getServerChannelID());
		buffer.putInt(ioid);
		buffer.put((byte)pendingRequest);

		if (QoS.INIT.isSet(pendingRequest))
		{
			// pvRequest
			SerializationHelper.serializePVRequest(buffer, control, pvRequest);
		}
		else if (!QoS.GET.isSet(pendingRequest))
		{
			lock();
			try {
				// put
				// serialize only what has been changed
				putBitSet.serialize(buffer, control);
				pvPutStructure.serialize(buffer, control, putBitSet);
			}  finally {
				// release references
				putBitSet = null;
				pvPutStructure = null;

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
				callback.channelPutConnect(status, this, null);
				return;
			}

			lock();
			try {
				// create data (for get) and its bitSet
				data = SerializationHelper.deserializeStructureAndCreatePVStructure(payloadBuffer, transport, data);
				bitSet = createBitSetFor(data, bitSet);
			} finally {
				unlock();
			}

			// notify
			callback.channelPutConnect(status, this, data.getStructure());
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
					callback.getDone(status, this, null, null);
					return;
				}

				lock();
				try {
					bitSet.deserialize(payloadBuffer, transport);
					data.deserialize(payloadBuffer, transport, bitSet);
				} finally {
					unlock();
				}

				callback.getDone(status, this, data, bitSet);
			}
			else
			{
				callback.putDone(status, this);
			}
		}
		catch (Throwable th)
		{
			// guard PVA code from exceptions
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			th.printStackTrace(printWriter);
			requester.message("Unexpected exception caught while calling a callback: " + writer, MessageType.fatalError);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelPut#get()
	 */
	public void get() {
		if (destroyed) {
			callback.getDone(destroyedStatus, this, null, null);
			return;
		}

		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() | QoS.GET.getMaskValue() : QoS.GET.getMaskValue())) {
			callback.getDone(otherRequestPendingStatus, this, null, null);
			return;
		}

		try {
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			stopRequest();
			callback.getDone(channelNotConnected, this, null, null);
		}
	}

	public void put(PVStructure pvPutStructure, BitSet bitSet) {
		if (destroyed) {
			callback.putDone(destroyedStatus, this);
			return;
		}

		// TODO do we need to check for null or just let NPE happens

		if (!data.getStructure().equals(pvPutStructure.getStructure()))
		{
			callback.putDone(invalidPutStructureStatus, this);
			return;
		}

		if (bitSet.size() < data.getNumberFields())
		{
			callback.putDone(invalidBitSetLengthStatus, this);
			return;
		}

		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() : QoS.DEFAULT.getMaskValue())) {
			callback.putDone(otherRequestPendingStatus, this);
			return;
		}

		try {
			lock();
			this.pvPutStructure = pvPutStructure;
			this.putBitSet = bitSet;
			unlock();
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			stopRequest();
			callback.putDone(channelNotConnected, this);
		}
	}

}
