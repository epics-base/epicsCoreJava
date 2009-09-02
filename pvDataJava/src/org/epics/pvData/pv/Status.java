/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

/**
 * Status interface.
 * @author mse
 */
public interface Status {
	
	/**
	 * Status type enum.
	 */
	public enum StatusType { 
		/** Operation completed successfully. */
		OK, 
		/** Operation completed successfully, but there is a warning message. */
		WARNING, 
		/** Operation failed due to an error. */
		ERROR, 
		/** Operation failed due to an unexpected error. */
		FATAL
	};
	
	/**
	 * Get status type. 
	 * @return status type, non-<code>null</code>.
	 */
	StatusType getType();
	
	/**
	 * Get error message describing an error. Required if error status.
	 * @return error message.
	 */
	String getMessage();
	
	/**
	 * Get stack dump where error (exception) happened. Optional. 
	 * @return stack dump.
	 */
	String getStackDump();
}
