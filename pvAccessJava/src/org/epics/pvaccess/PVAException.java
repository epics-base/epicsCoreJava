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
 * Base PVA exception.
 * @author msekoranja
 */
public class PVAException extends Exception {

	/**
	 * Serializartion version ID.
	 */
	private static final long serialVersionUID = 6793616450810302722L;

	/**
	 * Default constructor.
	 */
	protected PVAException() {
	}

	/**
	 * @param message exception message.
	 */
	public PVAException(String message) {
		super(message);
	}

	/**
	 * @param cause exception cause.
	 */
	public PVAException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message exception message.
	 * @param cause exception cause.
	 */
	public PVAException(String message, Throwable cause) {
		super(message, cause);
	}

}
