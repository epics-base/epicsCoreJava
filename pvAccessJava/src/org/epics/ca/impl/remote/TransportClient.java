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

/**
 * Client (user) of the transport.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public interface TransportClient {
	
	/**
	 * Notification of unresponsive transport (e.g. no heartbeat detected) .
	 */
	public void transportUnresponsive();

	/**
	 * Notification of responsive transport (e.g. heartbeat detected again),
	 * called to discard <code>transportUnresponsive</code> notification.
	 * @param transport	responsive transport.
	 */
	public void transportResponsive(Transport transport);

	/**
	 * Notification of network change (server restarted).
	 */
	public void transportChanged();

	/**
	 * Notification of forcefully closed transport.
	 */
	public void transportClosed();

}
