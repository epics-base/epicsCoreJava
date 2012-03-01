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

package org.epics.ca.impl.remote.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

import org.epics.ca.CAConstants;
import org.epics.ca.impl.remote.Context;
import org.epics.ca.impl.remote.ProtocolType;
import org.epics.ca.impl.remote.Transport;
import org.epics.ca.impl.remote.codec.AbstractCodec;
import org.epics.ca.impl.remote.codec.impl.BlockingSocketAbstractCodec;
import org.epics.ca.impl.remote.request.ResponseHandler;
import org.epics.pvData.pv.Field;


/**
 * TCP transport implementation.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public abstract class BlockingTCPTransport extends BlockingSocketAbstractCodec implements Transport {

	/**
	 * Context instance.
	 */
	protected final Context context;

	/**
	 * Priority.
	 * NOTE: Priority cannot just be changed, since it is registered in transport registry with given priority.
	 */
	protected final short priority;
	// TODO to be implemeneted 
	
	/**
	 * CAS response handler.
	 */
	protected final ResponseHandler responseHandler;

	
	
	/**
	 * Cached byte-order flag. To be used only in send thread.
	 */
	//private int byteOrderFlag = 0x80;		// TODO
	
	/**
	 * Remote side transport revision (minor).
	 */
	protected byte remoteTransportRevision;		// TODO sync

	/**
	 * TCP transport constructor.
	 * @param context context where transport lives in.
	 * @param channel used socket channel.
	 * @param responseHandler response handler used to process CA headers.
	 * @param receiveBufferSize receive buffer size.
	 * @param priority transport priority.
	 */
	public BlockingTCPTransport(Context context, 
					   SocketChannel channel,
					   ResponseHandler responseHandler,
					   int receiveBufferSize,
					   short priority) throws SocketException {
		super(channel, 
				ByteBuffer.allocate(Math.max(CAConstants.MAX_TCP_RECV + AbstractCodec.MAX_ENSURE_DATA_SIZE, receiveBufferSize)),
				ByteBuffer.allocate(Math.max(CAConstants.MAX_TCP_RECV + AbstractCodec.MAX_ENSURE_DATA_SIZE, receiveBufferSize)),
				context.getLogger());
		this.context = context;
		this.responseHandler = responseHandler;
		this.remoteTransportRevision = 0;
		this.priority = priority;

		// add to registry
		context.getTransportRegistry().put(this);
	}
	

	@Override
	protected void internalDestroy() {
		super.internalDestroy();
		
		// remove from registry
		context.getTransportRegistry().remove(this);
	
		// clean resources
		internalClose();
	}
	
	// TODO
	/**
	 * Called to any resources just before closing transport
	 * @param forced	flag indicating if forced (e.g. forced disconnect) is required
	 */
	protected void internalClose()
	{
		// noop
	}
	
	
	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.Transport#getType()
	 */
	@Override
	public String getType() {
		return ProtocolType.TCP.name();
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.Transport#getRemoteAddress()
	 */
	@Override
	public InetSocketAddress getRemoteAddress() {
		return socketAddress;
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.Transport#getContext()
	 */
	@Override
	public Context getContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.Transport#getMinorRevision()
	 */
	@Override
	public byte getMinorRevision() {
		return CAConstants.CA_PROTOCOL_REVISION;
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.Transport#getReceiveBufferSize()
	 */
	@Override
	public int getReceiveBufferSize() {
		return socketBuffer.capacity();
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.Transport#getSocketReceiveBufferSize()
	 */
	@Override
	public int getSocketReceiveBufferSize() {
		try {
			return channel.socket().getReceiveBufferSize();
		} catch (SocketException e) {
			// error
			return -1;
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.Transport#getPriority()
	 */
	@Override
	public short getPriority() {
		// TODO Auto-generated method stub
		return priority;
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.Transport#setRemoteMinorRevision(byte)
	 */
	@Override
	public void setRemoteMinorRevision(byte minor) {
		// TODO Auto-generated method stub
		//this.remoteTransportRevision = minor;
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.Transport#setRemoteTransportReceiveBufferSize(int)
	 */
	@Override
	public void setRemoteTransportReceiveBufferSize(int receiveBufferSize) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.Transport#setRemoteTransportSocketReceiveBufferSize(int)
	 */
	@Override
	public void setRemoteTransportSocketReceiveBufferSize(
			int socketReceiveBufferSize) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.Transport#changedTransport()
	 */
	@Override
	public void changedTransport() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.codec.AbstractCodec#processControlMessage()
	 */
	@Override
	public void processControlMessage() {

		// TODO
		/*
		// marker request sent
		if (command == 0)
		{
			if (markerToSend.getAndSet(payloadSize) == 0)
				; // TODO send back response
		}
		// marker received back
		else if (command == 1)
		{
			int difference = (int)totalBytesSent - payloadSize + CAConstants.CA_MESSAGE_HEADER_SIZE;
			// overrun check
			if (difference < 0)
				difference += Integer.MAX_VALUE;
			remoteBufferFreeSpace = remoteTransportReceiveBufferSize + remoteTransportSocketReceiveBufferSize - difference; 
			// TODO if this is calculated wrong, this can be critical !!!
		}
		// set byte order
		else */if (command == 2)
		{
			// check 7-th bit
			setByteOrder(flags < 0 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.codec.AbstractCodec#processApplicationMessage()
	 */
	@Override
	public void processApplicationMessage() throws IOException {
		responseHandler.handleResponse(socketAddress, this, version, command, payloadSize, socketBuffer);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.DeserializableControl#cachedDeserialize(java.nio.ByteBuffer)
	 */
	@Override
	public Field cachedDeserialize(ByteBuffer buffer) {
		return getIntrospectionRegistry().deserialize(buffer, this);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.SerializableControl#cachedSerialize(org.epics.pvData.pv.Field, java.nio.ByteBuffer)
	 */
	@Override
	public void cachedSerialize(Field field, ByteBuffer buffer) {
		getIntrospectionRegistry().serialize(field, buffer, this);
	}
}
