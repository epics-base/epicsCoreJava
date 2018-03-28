package org.epics.pvaccess.impl.remote.codec;

public class InvalidDataStreamException extends RuntimeException
{
	private static final long serialVersionUID = 4197285014395498270L;

	public InvalidDataStreamException() {
		super();
	}

	public InvalidDataStreamException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidDataStreamException(String message) {
		super(message);
	}

	public InvalidDataStreamException(Throwable cause) {
		super(cause);
	}
}