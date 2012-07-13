package org.epics.pvaccess.server.rpc;

import org.epics.pvdata.pv.Status.StatusType;

public class RPCRequestException extends Exception {

	private static final long serialVersionUID = -5921492767290276785L;

	private final StatusType status;
	
	public RPCRequestException(StatusType status, String message) {
		super(message);
		this.status = status;
	}

	public RPCRequestException(StatusType status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	public StatusType getStatus() {
		return status;
	}

}
