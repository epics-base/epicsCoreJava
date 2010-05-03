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

import org.epics.ca.client.GetFieldRequester;
import org.epics.ca.impl.remote.DataResponse;
import org.epics.ca.impl.remote.Transport;
import org.epics.ca.impl.remote.TransportSendControl;
import org.epics.ca.impl.remote.TransportSender;
import org.epics.pvData.factory.StatusFactory;
import org.epics.pvData.misc.SerializeHelper;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.StatusCreate;

/**
 * CA get field request.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ChannelGetFieldRequestImpl implements DataResponse, TransportSender {

    private static final StatusCreate statusCreate = StatusFactory.getStatusCreate();

    /**
	 * Channel.
	 */
	protected final ChannelImpl channel;

	/**
	 * Context.
	 */
	protected final ClientContextImpl context;

	/**
	 * I/O ID given by the context when registered.
	 */
	protected final int ioid;

	/**
	 * Response callback listener.
	 */
	protected final GetFieldRequester callback;
	
	/**
	 * Sub-field name.
	 */
	protected final String subField;
	
	/**
	 * Destroyed flag.
	 */
	protected volatile boolean destroyed = false;

	public ChannelGetFieldRequestImpl(ChannelImpl channel, GetFieldRequester callback,
            String subField)
	{
		if (callback == null)
			throw new IllegalArgumentException("null requester");

		this.channel = channel;
		this.context = (ClientContextImpl)channel.getContext();
		
		this.callback = callback;
		this.subField = subField;
		
		// register response request
		ioid = context.registerResponseRequest(this);
		channel.registerResponseRequest(this);

		// enqueue send request
		try {
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			callback.getDone(BaseRequestImpl.channelNotConnected, null);
		}
	}

	
	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.TransportSender#lock()
	 */
	@Override
	public void lock() {
		// noop
	}


	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.TransportSender#send(java.nio.ByteBuffer, org.epics.ca.impl.remote.TransportSendControl)
	 */
	@Override
	public void send(ByteBuffer buffer, TransportSendControl control) {
		control.startMessage((byte)17, 2*Integer.SIZE/Byte.SIZE);
		
		// SID
		buffer.putInt(channel.getServerChannelID());

		// IOID
		buffer.putInt(ioid);

		// field name (can be null)
		SerializeHelper.serializeString(subField, buffer, control);
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.TransportSender#unlock()
	 */
	@Override
	public void unlock() {
		// noop
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.core.ResponseRequest#getIOID()
	 */
	public int getIOID() {
		return ioid;
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.ResponseRequest#getRequester()
	 */
	@Override
	public Requester getRequester() {
		return callback;
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.core.DataResponse#response(org.epics.ca.core.Transport, byte, java.nio.ByteBuffer)
	 */
	public void response(Transport transport, byte version, ByteBuffer payloadBuffer) {
		
		try
		{	
			final Status status = statusCreate.deserializeStatus(payloadBuffer, transport);
			if (status.isSuccess())
			{
				// deserialize Field...
				final Field field = transport.getIntrospectionRegistry().deserialize(payloadBuffer, transport);
				callback.getDone(status, field);
			}
			else
			{
				callback.getDone(status, null);
			}
		}
		finally
		{
			// always cancel request
			cancel();
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.core.ResponseRequest#cancel()
	 */
	public void cancel() {
		destroy();
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.client.ChannelRequest#destroy()
	 */
	public void destroy() {
		
		synchronized (this) {
			if (destroyed)
				return;
			destroyed = true;
		}

		// unregister response request
		context.unregisterResponseRequest(this);
		channel.unregisterResponseRequest(this);
	}
	
	/* (non-Javadoc)
	 * @see org.epics.ca.core.ResponseRequest#timeout()
	 */
	public void timeout() {
		cancel();
		// TODO notify?
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.core.ResponseRequest#reportStatus(org.epics.pvData.pv.Status)
	 */
	public void reportStatus(Status status) {
		// destroy, since channel (parent) was destroyed
		if (status == ChannelImpl.channelDestroyed)
			destroy();
		// TODO notify?
	}
	
	
}
