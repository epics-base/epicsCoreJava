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
import org.epics.pvdata.pv.Status.StatusType;

/**
 * CA get request.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelGetRequestImpl extends BaseRequestImpl implements ChannelGet {

    /**
	 * Response callback listener.
	 */
	protected final ChannelGetRequester callback;

	protected final PVStructure pvRequest;

	protected PVStructure data = null;
	protected BitSet bitSet = null;
	
	public ChannelGetRequestImpl(ChannelImpl channel, ChannelGetRequester callback,
            PVStructure pvRequest)
	{
		super(channel, callback);
		
		if (callback == null)
		{
			destroy(true);
			throw new IllegalArgumentException("null requester");
		}
		
		if (pvRequest == null)
		{
			destroy(true);
			throw new IllegalArgumentException("null pvRequest");
		}
		
		this.callback = callback;
		
		this.pvRequest = pvRequest;
		
		// TODO immediate get, i.e. get data with init message
		// TODO one-time get, i.e. immediate get + lastRequest 

		// subscribe
		try {
			resubscribeSubscription(channel.checkAndGetTransport());
		} catch (IllegalStateException ise) {
			callback.channelGetConnect(channelNotConnected, this, null, null);
			destroy(true);
		} catch (CAException caex) {
			callback.channelGetConnect(statusCreate.createStatus(StatusType.ERROR, "failed to sent message over network", caex), this, null, null);
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
	 * @see org.epics.pvaccess.client.impl.remote.channelAccess.BaseRequestImpl#destroyResponse(org.epics.pvaccess.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvdata.pv.Status)
	 */
	@Override
	void destroyResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		// data available
		if (QoS.GET.isSet(qos))
			normalResponse(transport, version, payloadBuffer,qos, status);
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
				callback.channelGetConnect(status, this, null, null);
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
			callback.channelGetConnect(status, this, data, bitSet);
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
	 * @see org.epics.pvaccess.client.impl.remote.channelAccess.BaseRequestImpl#normalResponse(org.epics.pvaccess.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvdata.pv.Status)
	 */
	@Override
	void normalResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		try
		{
			if (!status.isSuccess())
			{
				callback.getDone(status);
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
			
			callback.getDone(status);
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
	 * @see org.epics.pvaccess.client.ChannelGet#get(boolean)
	 */
	@Override
	public synchronized void get(boolean lastRequest) {
		if (destroyed) {
			callback.getDone(destroyedStatus);
			return;
		}

		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() | QoS.GET.getMaskValue() : QoS.DEFAULT.getMaskValue())) {
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
	
	/* Called on server restart...
	 * @see org.epics.pvaccess.core.SubscriptionRequest#resubscribeSubscription(org.epics.pvaccess.core.Transport)
	 */
	@Override
	public final void resubscribeSubscription(Transport transport) throws CAException {
		startRequest(QoS.INIT.getMaskValue());
		transport.enqueueSendRequest(this);
	}
	
}
