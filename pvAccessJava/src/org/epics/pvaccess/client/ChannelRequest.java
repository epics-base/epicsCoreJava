/**
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
	 * Cancel any pending request.
	 * Completion will be reported via request's response callback:
	 * <ul>
	 *   <li>if cancel() request is issue after the request was already complete, success/failure completion will be reported.</li>
	 *   <li>if request was actually canceled, cancellation completion will be reported.</li>
	 * </ul>
	 */
	void cancel();
}
