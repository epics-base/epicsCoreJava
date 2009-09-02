/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

import org.epics.pvData.pv.Status.StatusType;

/**
 * Interface for creating status.
 * @author mse
 */
public interface StatusCreate {
	
	/**
	 * Get OK status. Static instance should be returned.
	 * @return OK <code>Status</code> instance.
	 */
	Status getStatusOK();
	
	/**
	 * Create status.
	 * @param type status type, non-<code>null</code>.
	 * @param message message describing an error, non-<code>null</code>.
	 * 		  NOTE: Do NOT use <code>throwable.getMessage()</code> as message, since it will be supplied with the <code>cause</code>.
	 * @param cause exception that caused an error. Optional.
	 * @return status instance.
	 */
	Status createStatus(StatusType type, String message, Throwable cause);
}
