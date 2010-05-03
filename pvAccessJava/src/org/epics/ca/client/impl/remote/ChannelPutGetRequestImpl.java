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
import org.epics.ca.client.ChannelPutGet;
import org.epics.ca.client.ChannelPutGetRequester;
import org.epics.ca.impl.remote.IntrospectionRegistry;
import org.epics.ca.impl.remote.QoS;
import org.epics.ca.impl.remote.Transport;
import org.epics.ca.impl.remote.TransportSendControl;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.Status.StatusType;

/**
 * CA putGet request.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelPutGetRequestImpl extends BaseRequestImpl implements ChannelPutGet {

    /**
	 * Response callback listener.
	 */
	protected final ChannelPutGetRequester callback;

	protected final PVStructure pvRequest;
	
	protected volatile PVStructure putData = null;
	protected volatile PVStructure getData = null;
	
	public ChannelPutGetRequestImpl(ChannelImpl channel,
			ChannelPutGetRequester callback,
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
			callback.channelPutGetConnect(channelNotConnected, null, null, null);
		} catch (CAException e) {		
			callback.channelPutGetConnect(statusCreate.createStatus(StatusType.ERROR, "failed to sent message over network", e), null, null, null);
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
		
		control.startMessage((byte)12, 2*Integer.SIZE/Byte.SIZE+1);
		buffer.putInt(channel.getServerChannelID());
		buffer.putInt(ioid);
		if (pendingRequest != QoS.INIT.getMaskValue())
			buffer.put((byte)pendingRequest);
		
		if (QoS.INIT.isSet(pendingRequest))
		{
			// qos
			final int qos = QoS.INIT.getMaskValue();
			buffer.put((byte)qos);

			// pvRequest
			channel.getTransport().getIntrospectionRegistry().serializePVRequest(buffer, control, pvRequest);
		}
		else if (QoS.GET.isSet(pendingRequest) || QoS.GET_PUT.isSet(pendingRequest)) {
			// noop
		}
		else
		{
			putData.serialize(buffer, control);
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
		{
			normalResponse(transport, version, payloadBuffer, qos, status);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.impl.remote.channelAccess.BaseRequestImpl#initResponse(org.epics.ca.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvData.pv.Status)
	 */
	@Override
	void initResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		if (!status.isSuccess())
		{
			callback.channelPutGetConnect(status, this, null, null);
			return;
		}
		
		final IntrospectionRegistry registry = transport.getIntrospectionRegistry();
		putData = registry.deserializeStructureAndCreatePVStructure(payloadBuffer, transport);
		getData = registry.deserializeStructureAndCreatePVStructure(payloadBuffer, transport);

		// notify
		callback.channelPutGetConnect(okStatus, this, putData, getData);
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.impl.remote.channelAccess.BaseRequestImpl#normalResponse(org.epics.ca.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvData.pv.Status)
	 */
	@Override
	void normalResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		
		if (QoS.GET.isSet(qos))
		{
			if (!status.isSuccess())
			{
				callback.getGetDone(status);
				return;
			}
			
			// deserialize get data
			getData.deserialize(payloadBuffer, transport);
			callback.getGetDone(status);
		}
		else if (QoS.GET_PUT.isSet(qos))
		{
			if (!status.isSuccess())
			{
				callback.getPutDone(status);
				return;
			}
			
			// deserialize put data
			putData.deserialize(payloadBuffer, transport);
			callback.getPutDone(status);
		}
		else 
		{
			if (!status.isSuccess())
			{
				callback.putGetDone(status);
				return;
			}
			
			// deserialize data
			getData.deserialize(payloadBuffer, transport);
			callback.putGetDone(status);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.ChannelPutGet#putGet(boolean)
	 */
	@Override
	public void putGet(boolean lastRequest) {
		if (destroyed) {
			callback.putGetDone(destroyedStatus);
			return;
		}
		
		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() : QoS.DEFAULT.getMaskValue())) {
			callback.putGetDone(otherRequestPendingStatus);
			return;
		}
		
		try {
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			callback.putGetDone(channelNotConnected);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.ChannelPutGet#getGet()
	 */
	@Override
	public void getGet() {
		if (destroyed) {
			callback.getGetDone(destroyedStatus);
			return;
		}
		
		if (!startRequest(QoS.GET.getMaskValue())) {
			callback.getGetDone(otherRequestPendingStatus);
			return;
		}
		
		try {
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			callback.getGetDone(channelNotConnected);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.ChannelPutGet#getPut()
	 */
	@Override
	public void getPut() {
		if (destroyed) {
			callback.getPutDone(destroyedStatus);
			return;
		}
		
		if (!startRequest(QoS.GET_PUT.getMaskValue())) {
			callback.getPutDone(otherRequestPendingStatus);
			return;
		}
		
		try {
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			callback.getPutDone(channelNotConnected);
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
