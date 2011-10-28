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
import org.epics.ca.client.ChannelProcess;
import org.epics.ca.client.ChannelProcessRequester;
import org.epics.ca.impl.remote.QoS;
import org.epics.ca.impl.remote.Transport;
import org.epics.ca.impl.remote.TransportSendControl;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.Status.StatusType;

/**
 * CA process request.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelProcessRequestImpl extends BaseRequestImpl implements ChannelProcess {

	/**
	 * Response callback listener.
	 */
	protected final ChannelProcessRequester callback;

	protected final PVStructure pvRequest;

	public ChannelProcessRequestImpl(ChannelImpl channel, ChannelProcessRequester callback, PVStructure pvRequest) {
		super(channel, callback);
		this.callback = callback;

		this.pvRequest = pvRequest;
		
		// TODO best-effort support

		// subscribe
		try {
			resubscribeSubscription(channel.checkAndGetTransport());
		} catch (IllegalStateException ise) {
			callback.channelProcessConnect(channelNotConnected, null);
		} catch (CAException e) {
			callback.channelProcessConnect(statusCreate.createStatus(StatusType.ERROR, "failed to sent message over network", e), null);
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
		
		control.startMessage((byte)16, 2*Integer.SIZE/Byte.SIZE+1);
		buffer.putInt(channel.getServerChannelID());
		buffer.putInt(ioid);
		buffer.put((byte)pendingRequest);
		
		if (QoS.INIT.isSet(pendingRequest))
		{
			// pvRequest
			channel.getTransport().getIntrospectionRegistry().serializePVRequest(buffer, control, pvRequest);
		}
		
		stopRequest();
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.impl.remote.channelAccess.BaseRequestImpl#destroyResponse(org.epics.ca.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvData.pv.Status)
	 */
	@Override
	void destroyResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		try {
			callback.processDone(status);
		} catch (Throwable th) {
			// guard CA code from exceptions
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			th.printStackTrace(printWriter);
			requester.message("Unexpected exception caught while calling a callback: " + writer, MessageType.fatalError);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.impl.remote.channelAccess.BaseRequestImpl#initResponse(org.epics.ca.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvData.pv.Status)
	 */
	@Override
	void initResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		try {
			callback.channelProcessConnect(status, this);
		} catch (Throwable th) {
			// guard CA code from exceptions
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			th.printStackTrace(printWriter);
			requester.message("Unexpected exception caught while calling a callback: " + writer, MessageType.fatalError);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.impl.remote.channelAccess.BaseRequestImpl#normalResponse(org.epics.ca.core.Transport, byte, java.nio.ByteBuffer, byte, org.epics.pvData.pv.Status)
	 */
	@Override
	void normalResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status) {
		try {
			callback.processDone(status);
		} catch (Throwable th) {
			// guard CA code from exceptions
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			th.printStackTrace(printWriter);
			requester.message("Unexpected exception caught while calling a callback: " + writer, MessageType.fatalError);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.core.SubscriptionRequest#resubscribeSubscription(org.epics.ca.core.Transport)
	 */
	@Override
	public final void resubscribeSubscription(Transport transport) throws CAException {
		startRequest(QoS.INIT.getMaskValue());
		transport.enqueueSendRequest(this);
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.ChannelProcess#process(boolean)
	 */
	@Override
	public void process(boolean lastRequest) {
		if (destroyed) {
			callback.processDone(destroyedStatus);
			return;
		}
		
		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() : QoS.DEFAULT.getMaskValue())) {
			callback.processDone(otherRequestPendingStatus);
			return;
		}
		
		try {
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			stopRequest();
			callback.processDone(channelNotConnected);
		}
	}
	
}
