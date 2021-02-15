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

package org.epics.pvaccess.impl.remote.server;



/**
 * Interface defining a transport that hosts channels.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public interface ChannelHostingTransport {

	/**
	 * Preallocate new channel SID.
	 * @return new channel server id (SID).
	 */
	public int preallocateChannelSID();

	/**
	 * De-preallocate new channel SID.
	 * @param sid preallocated channel SID.
	 */
	public void dePreAllocateChannelSID(int sid);

	/**
	 * Register a new channel.
	 * @param sid preallocated channel SID.
	 * @param channel channel to register.
	 */
	public void registerChannel(int sid, ServerChannel channel);

	/**
	 * Unregister a new channel (and deallocates its handle).
	 * @param sid SID
	 */
	public void unregisterChannel(int sid);

	/**
	 * Get channel by its SID.
	 * @param sid channel SID
	 * @return channel with given SID, <code>null</code> otherwise
	 */
	public ServerChannel getChannel(int sid);

	/**
	 * Get channel count.
	 * @return channel count.
	 */
	public int getChannelCount();

	/**
	 * Get an array of all hosted channels.
	 * @return an array of all hosted channels.
	 */
	public ServerChannel[] getChannels();
}
