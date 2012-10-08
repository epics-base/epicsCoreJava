package org.epics.pvaccess.server.rpc;

import org.epics.pvdata.pv.Status.StatusType;

/**
 * Exception to be thrown in case of an error while processing RPC request.
 * @author msekoranja
 */
public class RPCRequestException extends Exception {

	private static final long serialVersionUID = -5921492767290276785L;

	private final StatusType status;
	
	/**
	 * Constructor.
	 * NOTE: if an exception (of a problem) is available, use other constructor that supports exception chaining.
	 * @param status status type.
	 * @param message human readable exception message.
	 */
	public RPCRequestException(StatusType status, String message) {
		super(message);
		this.status = status;
	}

	/**
	 * Constructor that supports exception chaining.
	 * @param status status type.
	 * @param message human readable exception message.
	 * @param cause the cause of the problem.
	 */
	public RPCRequestException(StatusType status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	/**
	 * Get status type.
	 * @return status type.
	 */
	public StatusType getStatus() {
		return status;
	}

}
