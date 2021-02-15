/*
 * Copyright (c) 2006 by Cosylab
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

package org.epics.pvaccess.server.impl.remote;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.impl.remote.server.ServerChannel;
import org.epics.pvaccess.plugins.SecurityPlugin.ChannelSecuritySession;
import org.epics.pvdata.misc.Destroyable;

/**
 * Server channel (client connection to local channel). This (default)
 * implementation grants all access rights.
 *
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ServerChannelImpl implements ServerChannel {

	/**
	 * Local channel.
	 */
	protected final Channel channel;

	/**
	 * Channel SID.
	 */
	protected final int sid;

	/**
	 * Channel CID.
	 */
	protected final int cid;

	/**
	 * Channel security session.
	 */
	protected final ChannelSecuritySession channelSecuritySession;

	/**
	 * Requests.
	 */
	protected final Map<Integer, Destroyable> requests = Collections
			.synchronizedMap(new HashMap<Integer, Destroyable>());

	/**
	 * Destroy state.
	 */
	protected boolean destroyed = false;

	/**
	 * Create server channel for given process variable.
	 *
	 * @param channel
	 *            local channel.
	 * @param cid
	 *            channel CID.
	 * @param sid
	 *            channel SID.
	 * @param css
	 *            channel security session.
	 */
	public ServerChannelImpl(Channel channel, int cid, int sid, ChannelSecuritySession css) {
		if (channel == null)
			throw new IllegalArgumentException("non null local channel required");

		this.cid = cid;
		this.sid = sid;
		this.channel = channel;
		this.channelSecuritySession = css;
	}

	/**
	 * Get local channel.
	 *
	 * @return local channel.
	 */
	public Channel getChannel() {
		return channel;
	}

	/**
	 * Get channel CID.
	 *
	 * @return channel CID.
	 */
	public int getCID() {
		return cid;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.epics.pvaccess.server.impl.remote.ServerChannel#getSID()
	 */
	public int getSID() {
		return sid;
	}

	public ChannelSecuritySession getChannelSecuritySession() {
		return channelSecuritySession;
	}

	/**
	 * Register request
	 *
	 * @param id
	 *            request ID.
	 * @param request
	 *            request to be registered.
	 */
	public void registerRequest(int id, Destroyable request) {
		if (request == null)
			throw new IllegalArgumentException("request == null");

		requests.put(id, request);
	}

	/**
	 * Unregister request.
	 *
	 * @param id
	 *            request ID.
	 */
	public void unregisterRequest(int id) {

		requests.remove(id);

	}

	/**
	 * Get request by its ID.
	 *
	 * @param id
	 *            request ID.
	 * @return request with given ID, <code>null</code> if there is no request with
	 *         such ID.
	 */
	public Destroyable getRequest(int id) {

		return requests.get(id);

	}

	public synchronized Destroyable[] getRequests() {

		Destroyable[] reqs = new Destroyable[requests.size()];
		requests.values().toArray(reqs);
		return reqs;

	}

	/**
	 * Destroy all registered requests.
	 */
	protected void destroyAllRequests() {
		Integer[] keys;

		// resource allocation optimization
		if (requests.size() == 0)
			return;

		keys = new Integer[requests.keySet().size()];
		requests.keySet().toArray(keys);
		for (Integer key : keys) {
			final Destroyable cr = requests.remove(key);
			cr.destroy();
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.epics.pvaccess.server.impl.remote.ServerChannel#destroy()
	 */
	public synchronized void destroy() {
		if (destroyed)
			return;
		destroyed = true;

		// destroy all requests
		destroyAllRequests();

		try {
			channelSecuritySession.close();
		} catch (Throwable th) {
			// guard from bad plug-on
			// TODO
			th.printStackTrace();
		}

		// TODO make impl that does shares channels (and does ref counting)!!!
		// try catch?
		channel.destroy();
	}

	/**
	 * Prints detailed information about the process variable to the standard output
	 * stream.
	 *
	 * @throws IllegalStateException
	 *             if the context has been destroyed.
	 */
	public void printInfo() throws IllegalStateException {
		printInfo(System.out);
	}

	/**
	 * Prints detailed information about the process variable to the specified
	 * output stream.
	 *
	 * @param out
	 *            the output stream.
	 * @throws IllegalStateException
	 *             if the context has been destroyed.
	 */
	public void printInfo(PrintStream out) {
		out.println("CLASS        : " + getClass().getName());
		out.println("CHANNEL      : " + channel);
	}

}
