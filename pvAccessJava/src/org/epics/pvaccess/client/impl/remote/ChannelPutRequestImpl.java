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
 * CA put request.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelPutRequestImpl extends BaseRequestImpl implements ChannelPut {

	/**
	 * Response callback listener.
	 */
	protected final ChannelPutRequester callback;

	protected PVStructure data = null;
	protected BitSet bitSet = null;
	
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
			callback.channelPutConnect(channelDestroyed, this, null, null);
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
				bitSet.serialize(buffer, control);
				data.serialize(buffer, control, bitSet);
			}  finally {
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
		try {
			callback.putDone(status);
		} catch (Throwable th) {
			// guard CA code from exceptions
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			th.printStackTrace(printWriter);
			requester.message("Unexpected exception caught while calling a callback: " + writer, MessageType.fatalError);
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
				callback.channelPutConnect(status, this, null, null);
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
			callback.channelPutConnect(status, this, data, bitSet);
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
			if (QoS.GET.isSet(qos))
			{
				if (!status.isSuccess())
				{
					callback.getDone(status);
					return;
				}
				
				lock();
				try {
					data.deserialize(payloadBuffer, transport);
				} finally {
					unlock();
				}		

				callback.getDone(status);
			}
			else
			{
				callback.putDone(status);
			}
		}
		catch (Throwable th)
		{
			// guard CA code from exceptions
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			th.printStackTrace(printWriter);
			requester.message("Unexpected exception caught while calling a callback: " + writer, MessageType.fatalError);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelPut#get()
	 */
	@Override
	public void get() {
		if (destroyed) {
			callback.getDone(destroyedStatus);
			return;
		}
		
		if (!startRequest(QoS.GET.getMaskValue())) {
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

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelPut#put(boolean)
	 */
	@Override
	public void put(boolean lastRequest) {
		if (destroyed) {
			callback.putDone(destroyedStatus);
			return;
		}
		
		if (!startRequest(lastRequest ? QoS.DESTROY.getMaskValue() : QoS.DEFAULT.getMaskValue())) {
			callback.putDone(otherRequestPendingStatus);
			return;
		}
		
		try {
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			stopRequest();
			callback.putDone(channelNotConnected);
		}
	}
	
}
