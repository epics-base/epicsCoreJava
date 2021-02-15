/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

import org.epics.pvdata.misc.Destroyable;

/**
 * Base interface for all channel requests.
 * @author mse
 */
public interface ChannelRequest extends Lockable, Destroyable {

	/**
	 * Get a channel instance this request belongs to.
	 * @return the channel instance.
	 */
	Channel getChannel();

	/**
	 * Cancel any pending request.
	 * Completion will be reported via request's response callback:
	 * <ul>
	 *   <li>if cancel() request is issued after the request was already complete, request success/failure completion will be reported and cancel() request ignored.</li>
	 *   <li>if the request was actually canceled, cancellation completion is reported.</li>
	 * </ul>
	 */
	void cancel();

	/**
	 * Announce next request as last request.
	 * When last request will be completed (regardless of completion status) the remote and local instance will be destroyed.
	 */
	void lastRequest();
}
