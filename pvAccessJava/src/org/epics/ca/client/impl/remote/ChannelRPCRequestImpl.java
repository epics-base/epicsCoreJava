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
import org.epics.ca.client.ChannelRPC;
import org.epics.ca.client.ChannelRPCRequester;
import org.epics.ca.impl.remote.IntrospectionRegistry;
import org.epics.ca.impl.remote.QoS;
import org.epics.ca.impl.remote.Transport;
import org.epics.ca.impl.remote.TransportSendControl;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.Status.StatusType;

/**
 * CA RPC request.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelRPCRequestImpl extends BaseRequestImpl implements ChannelRPC {

    /**
	 * Response callback listener.
	 */
	protected final ChannelRPCRequester callback;

	protected final PVStructure pvRequest;
	
	protected PVStructure argumentData = null;
	protected BitSet argumentBitSet = null;
	
	public ChannelRPCRequestImpl(ChannelImpl channel,
			ChannelRPCRequester callback,
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
			callback.channelRPCConnect(channelNotConnected, null, null, null);
		} catch (CAException e) {		
			callback.channelRPCConnect(statusCreate.createStatus(StatusType.ERROR, "failed to sent message over network", e), null, null, null);
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
		
		control.startMessage((byte)20, 2*Integer.SIZE/Byte.SIZE+1);
		buffer.putInt(channel.getServerChannelID());
		buffer.putInt(ioid);
		if (pendingRequest != QoS.INIT.getMaskValue())
			buffer.put((byte)pendingRequest);
		
		if (QoS.INIT.isSet(pendingRequest))
		{
			// qos
			buffer.put((byte)QoS.INIT.getMaskValue());

			// pvRequest
			channel.getTransport().getIntrospectionRegistry().serializePVRequest(buffer, control, pvRequest);
		    
		}
		else
		{
			lock();
			try {
				argumentBitSet.serialize(buffer, control);
				argumentData.serialize(buffer, control, argumentBitSet);
			} finally {
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
		// data available
		// TODO we need a flag here...
		normalResponse(transport, version, payloadBuffer, qos, status);
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
				callback.channelRPCConnect(status, this, null, null);
				return;
			}
			
			final IntrospectionRegistry registry = transport.getIntrospectionRegistry();
			lock();
			try {
				argumentData = registry.deserializeStructureAndCreatePVStructure(payloadBuffer, transport);
				argumentBitSet = new BitSet(argumentData.getNumberFields());
			} finally {
				unlock();
			}
			
			// notify
			callback.channelRPCConnect(status, this, argumentData, argumentBitSet);
		}
		catch (Throwable th)
		{
			// guard CA code from exceptions
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			th.printStackTrace(printWriter);
			requester.message("Unexpected exception caught: " + printWriter, MessageType.fatalError);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.impl.remote.channelAccess.BaseRequestImpl#normalResponse(org.epics.ca.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvData.pv.Status)
	 */
	@Override
	void normalResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		try
		{
			if (!status.isSuccess())
			{
				callback.requestDone(status, null);
				return;
			}
			
			// deserialize data
			final PVStructure retVal = transport.getIntrospectionRegistry().deserializeStructure(payloadBuffer, transport);
			callback.requestDone(status, retVal);
		}
		catch (Throwable th)
		{
			// guard CA code from exceptions
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			th.printStackTrace(printWriter);
			requester.message("Unexpected exception caught: " + printWriter, MessageType.fatalError);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.ChannelRPC#request(boolean)
	 */
	@Override
	public void request(boolean lastRequest) {
		if (destroyed) {
			callback.requestDone(destroyedStatus, null);
			return;
		}
		
		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() : QoS.DEFAULT.getMaskValue())) {
			callback.requestDone(otherRequestPendingStatus, null);
			return;
		}
		
		try {
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			stopRequest();
			callback.requestDone(channelNotConnected, null);
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
