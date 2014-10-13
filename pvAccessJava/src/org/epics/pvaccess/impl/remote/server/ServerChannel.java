package org.epics.pvaccess.impl.remote.server;

import org.epics.pvaccess.client.Channel;
import org.epics.pvdata.misc.Destroyable;

public interface ServerChannel {

	/**
	 * Get channel SID.
	 * @return channel SID.
	 */
	public int getSID();

	/**
	 * Destroy server channel.
	 * This method MUST BE called if overriden.
	 */
	public void destroy();

	/**
	 * Get served channel instance.
	 * @return the channel.
	 */
	public Channel getChannel();
	
	public Destroyable[] getRequests();

}