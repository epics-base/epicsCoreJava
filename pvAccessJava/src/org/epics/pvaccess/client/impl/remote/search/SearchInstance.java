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
import java.util.concurrent.atomic.AtomicInteger;

import org.epics.pvaccess.impl.remote.utils.GUID;

public interface SearchInstance {
	int getChannelID();
	String getChannelName();
	
	AtomicInteger getUserValue();

	/**
	 * Search response from server (channel found).
	 * @param guid server GUID
	 * @param minorRevision	server minor PVA revision.
	 * @param serverAddress	server address.
	 */
	// TODO make InetSocketAddress an URI or similar
	 void searchResponse(GUID guid, byte minorRevision, InetSocketAddress serverAddress);
}
