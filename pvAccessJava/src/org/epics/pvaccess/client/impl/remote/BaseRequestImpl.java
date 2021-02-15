/*
 * Copyright (c) 2009 by Cosylab
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

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.impl.remote.QoS;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.TransportSender;
import org.epics.pvaccess.impl.remote.request.DataResponse;
import org.epics.pvaccess.impl.remote.request.SubscriptionRequest;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Requester;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.StatusCreate;

/**
 * Base channel request.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public abstract class BaseRequestImpl implements DataResponse, SubscriptionRequest, TransportSender {

    protected static final StatusCreate statusCreate = PVFactory.getStatusCreate();
    protected static final Status okStatus = statusCreate.getStatusOK();
    protected static final Status destroyedStatus = statusCreate.createStatus(StatusType.ERROR, "request destroyed", null);
    protected static final Status channelNotConnected = statusCreate.createStatus(StatusType.ERROR, "channel not connected", null);
    protected static final Status channelDestroyed = statusCreate.createStatus(StatusType.ERROR, "channel destroyed", null);
    protected static final Status otherRequestPendingStatus = statusCreate.createStatus(StatusType.ERROR, "other request pending", null);
    protected static final Status invalidPutStructureStatus = statusCreate.createStatus(StatusType.ERROR, "incompatible put structure", null);
    protected static final Status invalidPutArrayStatus = statusCreate.createStatus(StatusType.ERROR, "incompatible put array", null);
    protected static final Status invalidBitSetLengthStatus = statusCreate.createStatus(StatusType.ERROR, "invalid bit-set length", null);
    protected static final PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();

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
	protected final Requester requester;

	/**
	 * pvRequest structure.
	 */
	protected final PVStructure pvRequest;

	/**
	 * Last request flag.
	 */
	protected volatile boolean lastRequest = false;

	/**
	 * Destroyed flag.
	 */
	protected volatile boolean destroyed = false;

	/**
	 * Remote instance destroyed.
	 */
	protected volatile boolean remotelyDestroyed = false;

	/**
	 * Initialized flag.
	 */
	protected volatile boolean subscribed = false;

	protected int pendingRequest = NULL_REQUEST;
	/* negative... */
	protected static final int NULL_REQUEST = -1;
	protected static final int PURE_DESTROY_REQUEST = -2;
	protected static final int PURE_CANCEL_REQUEST = -2;

	protected final ReentrantLock lock = new ReentrantLock();

	public BaseRequestImpl(ChannelImpl channel, Requester requester,
				PVStructure pvRequest, boolean allowNullPVRequest)
	{
		if (requester == null)
			throw new IllegalArgumentException("requester == null");

		if (pvRequest == null && !allowNullPVRequest)
			throw new IllegalArgumentException("pvRequest == null");

		this.channel = channel;
		this.context = (ClientContextImpl)channel.getContext();

		this.requester = requester;
		this.pvRequest = pvRequest;

		// register response request
		// NOTE: this reference given in constructor,
		// however it is not used until registerToChannel is called
		this.ioid = context.registerResponseRequest(this);
	}

	protected void activate()
	{
		channel.registerResponseRequest(this);
	}

	public final boolean startRequest(int qos) {
		synchronized (this) {
			// we allow pure destroy and cancel...
			if (pendingRequest != NULL_REQUEST && qos != PURE_DESTROY_REQUEST && qos != PURE_CANCEL_REQUEST)
				return false;

			pendingRequest = qos;
			return true;
		}

		/*
		if (qos == PURE_DESTROY_REQUEST)
		{
			pendingRequest.set(PURE_DESTROY_REQUEST);
			return true;
		}
		else if (qos == PURE_CANCEL_REQUEST)
		{
			pendingRequest.set(PURE_CANCEL_REQUEST);
			return true;
		}
		else
			return pendingRequest.compareAndSet(NULL_REQUEST, qos);
		 */
	}

	public final void stopRequest() {
		synchronized (this) {
			pendingRequest = NULL_REQUEST;
		}
		// pendingRequest.set(NULL_REQUEST);
	}

	public final int getPendingRequest() {
		synchronized (this) {
			return pendingRequest;
		}
		// return pendingRequest.get();
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.ResponseRequest#getRequester()
	 */
	public Requester getRequester() {
		return requester;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.core.ResponseRequest#getIOID()
	 */
	public int getIOID() {
		return ioid;
	}

	abstract void initResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status);
	abstract void normalResponse(Transport transport, byte version, ByteBuffer payloadBuffer, byte qos, Status status);

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.core.DataResponse#response(org.epics.pvaccess.core.Transport, byte, java.nio.ByteBuffer)
	 */
	public void response(Transport transport, byte version, ByteBuffer payloadBuffer) {
		boolean destroy = false;
		try
		{
			transport.ensureData(1);
			final byte qos = payloadBuffer.get();
			final Status status = statusCreate.deserializeStatus(payloadBuffer, transport);

			if (QoS.INIT.isSet(qos))
			{
				initResponse(transport, version, payloadBuffer, qos, status);
			}
			else
			{
				if (QoS.DESTROY.isSet(qos))
				{
					remotelyDestroyed = true;
					destroy = true;
				}

				normalResponse(transport, version, payloadBuffer, qos, status);
			}
		}
		finally
		{
			if (destroy)
				destroy();
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.core.ResponseRequest#cancel()
	 */
	public void cancel() {

		if (destroyed)
			return;

		/*
		boolean canceledBeforeRequestSent = false;
		synchronized (this) {
			if (pendingRequest > 0)
			{
				canceledBeforeRequestSent = true;
				stopRequest();
			}
			else
				startRequest(PURE_CANCEL_REQUEST);
		}

		if (canceledBeforeRequestSent)
		{
			reportCancellation();
			return;
		}
		*/
		startRequest(PURE_CANCEL_REQUEST);
		try {
			channel.checkAndGetTransport().enqueueSendRequest(this);
		} catch (IllegalStateException ise) {
			// noop, we are just not connected
		}
	}

	/**
	 * Actual destroy implementation.
	 * @param createRequestFailed set to true if create request failed.
	 */
	protected void destroy(boolean createRequestFailed) {

		synchronized (this) {
			if (destroyed)
				return;
			destroyed = true;
		}
		// if (destroyed.getAndSet(true)) return;

		// unregister response request
		context.unregisterResponseRequest(this);
		channel.unregisterResponseRequest(this);

		// destroy remote instance
		if (!createRequestFailed && !remotelyDestroyed)
		{
			startRequest(PURE_DESTROY_REQUEST);
			try {
				channel.checkAndGetTransport().enqueueSendRequest(this);
			} catch (IllegalStateException ise) {
				// noop, we are just not connected
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.client.ChannelRequest#destroy()
	 */
	public void destroy() {
		destroy(false);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.core.ResponseRequest#timeout()
	 */
	public void timeout() {
		cancel();
		// TODO notify?
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.core.ResponseRequest#reportStatus(org.epics.pvdata.pv.Status)
	 */
	public void reportStatus(Status status) {
		// destroy, since channel (parent) was destroyed
		if (status == ChannelImpl.channelDestroyed)
			destroy();
		else if (status == ChannelImpl.channelDisconnected)
		{
			subscribed = false;
			stopRequest();
		}
		// TODO notify?
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.TransportSender#lock()
	 */
	public void lock() {
		lock.lock();
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.TransportSender#send(java.nio.ByteBuffer, org.epics.pvaccess.impl.remote.TransportSendControl)
	 */
	public void send(ByteBuffer buffer, TransportSendControl control) {
		final int qos = getPendingRequest();
		if (qos == -1)
			return;
		else if (qos == PURE_DESTROY_REQUEST)
		{
			control.startMessage((byte)15, 2*Integer.SIZE/Byte.SIZE);
			buffer.putInt(channel.getServerChannelID());
			buffer.putInt(ioid);
		}
		else if (qos == PURE_CANCEL_REQUEST)
		{
			control.startMessage((byte)21, 2*Integer.SIZE/Byte.SIZE);
			buffer.putInt(channel.getServerChannelID());
			buffer.putInt(ioid);
		}
		stopRequest();
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.impl.remote.TransportSender#unlock()
	 */
	public void unlock() {
		lock.unlock();
	}

	public static final BitSet createBitSetFor(PVStructure pvStructure, BitSet existingBitSet)
	{
		final int pvStructureSize = pvStructure.getNumberFields();
		if (existingBitSet != null && existingBitSet.size() >= pvStructureSize)
		{
			// clear existing BitSet
			// also necessary if larger BitSet is reused
			existingBitSet.clear();
			return existingBitSet;
		}
		else
			return new BitSet(pvStructureSize);
	}

	public static final PVField reuseOrCreatePVField(Field field, PVField existingPVField)
	{
		if (existingPVField != null && field.equals(existingPVField.getField()))
			return existingPVField;
		else
			return pvDataCreate.createPVField(field);
	}

	/* Called on server restart...
	 * @see org.epics.pvaccess.core.SubscriptionRequest#resubscribeSubscription(org.epics.pvaccess.core.Transport)
	 */
	public void resubscribeSubscription(Transport transport) {
		// NOTE: transport is null if channel was never connected
		if (transport != null && !subscribed && startRequest(QoS.INIT.getMaskValue()))
		{
			subscribed = true;
			transport.enqueueSendRequest(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.core.SubscriptionRequest#updateSubscription()
	 */
	public void updateSubscription() {
		// default is noop
	}

	public void lastRequest() {
		lastRequest = true;
	}

	public Channel getChannel() {
		return channel;
	}
}
