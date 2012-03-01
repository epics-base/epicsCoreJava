package org.epics.ca.impl.remote.codec;

public class ConnectionClosedException extends RuntimeException
{
	private static final long serialVersionUID = 1393611748151786339L;

	public ConnectionClosedException() {
		super();
	}

	public ConnectionClosedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectionClosedException(String message) {
		super(message);
	}

	public ConnectionClosedException(Throwable cause) {
		super(cause);
	}
}