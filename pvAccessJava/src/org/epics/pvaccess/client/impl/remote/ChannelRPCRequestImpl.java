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

import org.epics.pvaccess.client.ChannelRPC;
import org.epics.pvaccess.client.ChannelRPCRequester;
import org.epics.pvaccess.impl.remote.QoS;
import org.epics.pvaccess.impl.remote.SerializationHelper;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;

/**
 * PVA RPC request.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelRPCRequestImpl extends BaseRequestImpl implements ChannelRPC {

    /**
	 * Response callback listener.
	 */
	protected final ChannelRPCRequester callback;

	protected PVStructure argumentData;

	public static ChannelRPCRequestImpl create(ChannelImpl channel,
			ChannelRPCRequester callback,
	        PVStructure pvRequest)
	{
		ChannelRPCRequestImpl thisInstance =
			new ChannelRPCRequestImpl(channel, callback, pvRequest);
		thisInstance.activate();
		return thisInstance;
	}

	protected ChannelRPCRequestImpl(ChannelImpl channel,
			ChannelRPCRequester callback,
	        PVStructure pvRequest)
	{
		super(channel, callback, pvRequest, true);

		this.callback = callback;
	}

	protected void activate()
	{
		super.activate();

		// subscribe
		try {
			resubscribeSubscription(channel.checkDestroyedAndGetTransport());
		} catch (IllegalStateException ise) {
			callback.channelRPCConnect(channelDestroyed, this);
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
			SerializationHelper.serializePVRequest(buffer, control, pvRequest);

		}
		else
		{
			lock();
			try {
				SerializationHelper.serializeStructureFull(buffer, control, argumentData);
                // release argumentData structure
				argumentData = null;
			} finally {
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
				callback.channelRPCConnect(status, this);
				return;
			}

			// notify
			callback.channelRPCConnect(status, this);
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
				callback.requestDone(status, this, null);
				return;
			}

			// deserialize data
			final PVStructure retVal = SerializationHelper.deserializeStructureFull(payloadBuffer, transport);
			callback.requestDone(status, this, retVal);
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
	 * @see org.epics.pvaccess.client.ChannelRPC#request(org.epics.pvdata.pv.PVStructure)
	 */
	public void request(PVStructure pvArgument) {
		if (destroyed) {
			callback.requestDone(destroyedStatus, this, null);
			return;
		}

		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() : QoS.DEFAULT.getMaskValue())) {
			callback.requestDone(otherRequestPendingStatus, this, null);
			return;
		}

		try {
			lock();
			this.argumentData = pvArgument;
			unlock();
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			stopRequest();
			callback.requestDone(channelNotConnected, this, null);
		}
	}

}
