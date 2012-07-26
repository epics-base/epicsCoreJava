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

package org.epics.pvaccess.impl.remote;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.nio.channels.Channel;

import org.epics.pvdata.pv.DeserializableControl;



/**
 * Interface defining transport (connection).
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public interface Transport extends DeserializableControl, Channel {

	/** 
	 * Acquires transport.
	 * @param client client (channel) acquiring the transport
	 * @return <code>true</code> if transport was granted, <code>false</code> otherwise.
	 */
	public boolean acquire(TransportClient client);
	
	/** 
	 * Releases transport.
	 * @param client client (channel) releasing the transport
	 */
	public void release(TransportClient client);

	/**
	 * Get protocol type (tcp, udp, ssl, etc.).
	 * @return protocol type.
	 */
	public String getType();
	
	/**
	 * Get remote address.
	 * @return remote address.
	 */
	public InetSocketAddress getRemoteAddress();

	/**
	 * Get context transport is living in.
	 * @return context transport is living in.
	 */
	public Context getContext();

	/**
	 * Transport protocol revision.
	 * @return protocol revision.
	 */
	public byte getRevision();
	
	/**
	 * Get receive buffer size.
	 * @return receive buffer size.
	 */
	public int getReceiveBufferSize();
	
	/**
	 * Get socket receive buffer size.
	 * @return socket receive buffer size.
	 */
	public int getSocketReceiveBufferSize();

	/**
	 * Transport priority.
	 * @return protocol priority.
	 */
	public short getPriority();

	/**
	 * Set remote transport protocol revision.
	 * @param revision protocol revision.
	 */
	public void setRemoteRevision(byte revision);
	
	/**
	 * Set remote transport receive buffer size.
	 * @param receiveBufferSize receive buffer size.
	 */
	public void setRemoteTransportReceiveBufferSize(int receiveBufferSize);

	/**
	 * Set remote transport socket receive buffer size.
	 * @param socketReceiveBufferSize remote socket receive buffer size.
	 */
	public void setRemoteTransportSocketReceiveBufferSize(int socketReceiveBufferSize);

	/**
	 * Set byte order.
	 * @param byteOrder byte order to set.
	 */
	public void setByteOrder(ByteOrder byteOrder);
	
	/**
	 * Notification that transport has changed (server restarted).
	 */
	public void changedTransport();

	/**
	 * Enqueue send request.
	 * @param sender
	 */
	void enqueueSendRequest(TransportSender sender);

	/**
	 * Waits (if needed) until transport is verified, i.e. verified() method is being called.
	 * @param timeoutMs timeout to wait for verification, infinite if 0.
	 */
	boolean verify(long timeoutMs);
	
	/**
	 * Acknowledge that transport was verified.
	 */
	void verified();
	
	/**
	 * Alive notification.
	 * This method needs to be called (by newly received data or beacon)
	 * at least once in this period, if not echo will be issued
	 * and if there is not response to it, transport will be considered as unresponsive.
	 */
	void aliveNotification();
	
}
