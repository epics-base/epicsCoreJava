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
import org.epics.pvaccess.client.ChannelPutGet;
import org.epics.pvaccess.client.ChannelPutGetRequester;
import org.epics.pvaccess.impl.remote.QoS;
import org.epics.pvaccess.impl.remote.SerializationHelper;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;

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
	
	protected PVStructure putData = null;
	protected PVStructure getData = null;
	
	public ChannelPutGetRequestImpl(ChannelImpl channel,
			ChannelPutGetRequester callback,
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

		// subscribe
		try {
			resubscribeSubscription(channel.checkAndGetTransport());
		} catch (IllegalStateException ise) {
			callback.channelPutGetConnect(channelNotConnected, null, null, null);
			destroy(true);
		} catch (CAException e) {		
			callback.channelPutGetConnect(statusCreate.createStatus(StatusType.ERROR, "failed to sent message over network", e), null, null, null);
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
				putData.serialize(buffer, control);
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
		// data available
		// TODO we need a flag here...
		{
			normalResponse(transport, version, payloadBuffer, qos, status);
		}
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
				getData = SerializationHelper.deserializeStructureAndCreatePVStructure(payloadBuffer, transport, getData);
			} finally {
				unlock();
			}
	
			// notify
			callback.channelPutGetConnect(status, this, putData, getData);
		} catch (Throwable th) {
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
			
			if (QoS.GET.isSet(qos))
			{
				if (!status.isSuccess())
				{
					callback.getGetDone(status);
					return;
				}
				
				lock();
				try {
					// deserialize get data
					getData.deserialize(payloadBuffer, transport);
				} finally {
					unlock();
				}
				
				callback.getGetDone(status);
			}
			else if (QoS.GET_PUT.isSet(qos))
			{
				if (!status.isSuccess())
				{
					callback.getPutDone(status);
					return;
				}
				
				lock();
				try {
					// deserialize put data
					putData.deserialize(payloadBuffer, transport);
				} finally {
					unlock();
				}
				
				callback.getPutDone(status);
			}
			else 
			{
				if (!status.isSuccess())
				{
					callback.putGetDone(status);
					return;
				}
				
				lock();
				try {
					// deserialize data
					getData.deserialize(payloadBuffer, transport);
				} finally {
					unlock();
				}
				
				callback.putGetDone(status);
			}
		} catch (Throwable th) {
			// guard CA code from exceptions
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			th.printStackTrace(printWriter);
			requester.message("Unexpected exception caught: " + writer, MessageType.fatalError);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelPutGet#putGet(boolean)
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
			stopRequest();
			callback.putGetDone(channelNotConnected);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelPutGet#getGet()
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
			stopRequest();
			callback.getGetDone(channelNotConnected);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelPutGet#getPut()
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
			stopRequest();
			callback.getPutDone(channelNotConnected);
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
