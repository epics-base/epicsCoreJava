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

package org.epics.pvaccess.client.impl.remote.search;

import java.net.InetSocketAddress;

import org.epics.pvaccess.impl.remote.utils.GUID;

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
	 * @param channel channel to register.
	 */
	public void register(SearchInstance channel);

	/**
	 * Register channel, with maximum possible period search.
	 * @param channel channel to register.
	 * @param penalize register with penalty (do not search immediately).
	 */
	public void register(SearchInstance channel, boolean penalize);

	/**
	 * Unregister channel.
	 * @param channel channel to unregister.
	 */
	public void unregister(SearchInstance channel);
	
	/**
	 * Search response from server (channel found).
	 * @param guid server GUID.
	 * @param cid	client channel ID.
	 * @param seqNo	search sequence number.
	 * @param minorRevision	server minor PVA revision.
	 * @param serverAddress	server address.
	 */
	public void searchResponse(GUID guid, int cid, int seqNo, byte minorRevision, InetSocketAddress serverAddress);

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
