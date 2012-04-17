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

package org.epics.pvaccess;

/**
 * Base CA exception.
 * @author msekoranja
 */
public class CAException extends Exception {

	/**
	 * Serializartion version ID.
	 */
	private static final long serialVersionUID = 6793616450810302722L;

	/**
	 * Default constructor.
	 */
	protected CAException() {
	}

	/**
	 * @param message
	 */
	public CAException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public CAException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CAException(String message, Throwable cause) {
		super(message, cause);
	}

}
