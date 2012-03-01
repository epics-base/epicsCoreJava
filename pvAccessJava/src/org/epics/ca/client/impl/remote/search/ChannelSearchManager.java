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

package org.epics.ca.client.impl.remote.search;

import java.net.InetSocketAddress;

/**
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public interface ChannelSearchManager {
	
	/**
	 * Get number of registered channels.
	 * @return number of registered channels.
	 */
	public int registeredCount();

	/**
	 * Register channel.
	 * @param channel
	 */
	public void register(SearchInstance channel);


	/**
	 * Unregister channel.
	 * @param channel
	 */
	public void unregister(SearchInstance channel);
	
	/**
	 * Search response from server (channel found).
	 * @param cid	client channel ID.
	 * @param seqNo	search sequence number.
	 * @param minorRevision	server minor CA revision.
	 * @param serverAddress	server address.
	 */
	public void searchResponse(int cid, int seqNo, byte minorRevision, InetSocketAddress serverAddress);

	/**
	 * Beacon anomaly detected.
	 * Boost searching of all channels.
	 */
	public void newServerDetected();
	
	/**
	 * Cancel.
	 */
	public void cancel();

}
