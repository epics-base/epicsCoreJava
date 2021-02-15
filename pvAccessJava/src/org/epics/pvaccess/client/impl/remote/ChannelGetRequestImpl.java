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

import org.epics.pvaccess.client.ChannelGet;
import org.epics.pvaccess.client.ChannelGetRequester;
import org.epics.pvaccess.impl.remote.QoS;
import org.epics.pvaccess.impl.remote.SerializationHelper;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;

/**
 * PVA get request.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelGetRequestImpl extends BaseRequestImpl implements ChannelGet {

    /**
	 * Response callback listener.
	 */
	protected final ChannelGetRequester callback;

	protected PVStructure data = null;
	protected BitSet bitSet = null;

	public static ChannelGetRequestImpl create(ChannelImpl channel,
		ChannelGetRequester callback,
	    PVStructure pvRequest)
	{
		ChannelGetRequestImpl thisInstance =
			new ChannelGetRequestImpl(channel, callback, pvRequest);
		thisInstance.activate();
		return thisInstance;
	}

	protected ChannelGetRequestImpl(
			ChannelImpl channel,
			ChannelGetRequester callback,
            PVStructure pvRequest)
	{
		super(channel, callback, pvRequest, false);

		this.callback = callback;

		// TODO immediate get, i.e. get data with init message
		// TODO one-time get, i.e. immediate get + lastRequest
	}

	protected void activate()
	{
		super.activate();

		// subscribe
		try {
			resubscribeSubscription(channel.checkDestroyedAndGetTransport());
		} catch (IllegalStateException ise) {
			callback.channelGetConnect(channelDestroyed, this, null);
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

		control.startMessage((byte)10, 2*Integer.SIZE/Byte.SIZE+1);
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
	 * @see org.epics.pvaccess.client.impl.remote.channelAccess.BaseRequestImpl#initResponse(org.epics.pvaccess.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvdata.pv.Status)
	 */
	@Override
	void initResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		try
		{
			if (!status.isSuccess())
			{
				callback.channelGetConnect(status, this, null);
				return;
			}

			lock();
			try {
				// create data and its bitSet
				data = SerializationHelper.deserializeStructureAndCreatePVStructure(payloadBuffer, transport, data);
				bitSet = createBitSetFor(data, bitSet);
			} finally {
				unlock();
			}

			// notify
			callback.channelGetConnect(status, this, data.getStructure());
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
			if (!status.isSuccess())
			{
				callback.getDone(status, this, data, bitSet);
				return;
			}

			lock();
			try {
				// deserialize bitSet and data
				bitSet.deserialize(payloadBuffer, transport);
				data.deserialize(payloadBuffer, transport, bitSet);
			} finally {
				unlock();
			}

			callback.getDone(status, this, data, bitSet);
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
	 * @see org.epics.pvaccess.client.ChannelGet#get()
	 */
	public synchronized void get() {
		if (destroyed) {
			callback.getDone(destroyedStatus, this, null, null);
			return;
		}

		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() | QoS.GET.getMaskValue() : QoS.DEFAULT.getMaskValue())) {
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

}
