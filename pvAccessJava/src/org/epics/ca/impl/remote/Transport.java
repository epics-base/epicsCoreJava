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

package org.epics.ca.impl.remote;

import java.net.InetSocketAddress;

import org.epics.pvData.pv.DeserializableControl;



/**
 * Interface defining transport (connection).
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public interface Transport extends DeserializableControl {

	/**
	 * Get remote address.
	 * @return remote address.
	 */
	public InetSocketAddress getRemoteAddress();

	/**
	 * Get protocol type (tcp, udp, ssl, etc.).
	 * @return protocol type.
	 */
	public String getType();
	
	/**
	 * Get context transport is living in.
	 * @return context transport is living in.
	 */
	public Context getContext();
	
	/**
	 * Transport protocol major revision.
	 * @return protocol major revision.
	 */
	public byte getMajorRevision();

	/**
	 * Transport protocol minor revision.
	 * @return protocol minor revision.
	 */
	public byte getMinorRevision();
	
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
	 * Set remote transport protocol minor revision.
	 * @param minor protocol minor revision.
	 */
	public void setRemoteMinorRevision(byte minor);
	
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
	 * Notification transport that is still alive.
	 */
	public void aliveNotification();

	/**
	 * Notification that transport has changed.
	 */
	public void changedTransport();

	/**
	 * Get introspection registry for transport.
	 * @return <code>IntrospectionRegistry</code> instance.
	 */
	public IntrospectionRegistry getIntrospectionRegistry();
	
	/**
	 * Close transport.
	 * @param force flag indicating force-full (e.g. remote disconnect) close.
	 */
	public void close(boolean force);

	/**
	 * Check connection status.
	 * @return <code>true</code> if connected.
	 */
	public boolean isClosed();

	/**
	 * Get transport verification status.
	 * @return verification flag.
	 */
	public boolean isVerified();
	
	/**
	 * Notify transport that it is has been verified.
	 */
	public void verified();

	/**
	 * Enqueue send request.
	 * @param sender
	 */
	void enqueueSendRequest(TransportSender sender);

}
