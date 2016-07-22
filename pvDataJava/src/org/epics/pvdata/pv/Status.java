/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

/**
 * Status interface.
 * @author mse
 */
public interface Status extends Serializable {
	
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
	 * 
	 * @return status type, non-<code>null</code>.
	 */
	StatusType getType();
	
	/**
	 * Get error message describing an error. Required if error status.
	 *
	 * @return error message
	 */
	String getMessage();
	
	/**
	 * Get stack dump where error (exception) happened. Optional.
	 * 
	 * @return stack dump.
	 */
	String getStackDump();
	
	/**
	 * Convenient OK test. Same as <code>(getType() == StatusType.OK)</code>. 
	 * NOTE: this will return <code>false</code> on WARNING message although operation succeeded.
	 * To check if operation succeeded, use <code>isSuccess</code>.
     *
	 * @return OK status
	 * @see #isSuccess()
	 */
	boolean isOK();

	/**
	 * Check if operation succeeded.
	 * 
	 * @return operation success status
	 */
	boolean isSuccess();
}
