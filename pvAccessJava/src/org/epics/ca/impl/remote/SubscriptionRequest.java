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

import org.epics.ca.CAException;


/**
 * A request that expects an response multiple responses.
 * Responses identified by its I/O ID. 
 * This interface needs to be extended (to provide method called on response).
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public interface SubscriptionRequest extends ResponseRequest {
	
	/**
	 * Update (e.g. after some time of unresponsiveness) - report current value.
	 */
	public void updateSubscription() throws CAException;
	
	/**
	 * Rescubscribe (e.g. when server was restarted)
	 * @param transport new transport to be used.
	 */
	public void resubscribeSubscription(Transport transport) throws CAException;
}
