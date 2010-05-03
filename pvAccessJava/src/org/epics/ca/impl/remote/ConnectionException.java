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

import java.net.InetSocketAddress;

/**
 * Connection exception.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ConnectionException extends Exception {

	static final long serialVersionUID = 2533297215333289327L;

	/**
	 * Failed connection address.
	 */
	private InetSocketAddress address;

	/**
	 * Protocol type (tcp, udp, ssl, etc.)
	 */
	private String type;
	
	/**
	 * @param address remote address.
	 * @param type protocol type (tcp, udp, ssl, etc.)
	 * @param message
	 * @param cause
	 */
	public ConnectionException(String message, InetSocketAddress address, String type, Throwable cause) {
		super(message, cause);
		this.address = address;
		this.type = type;
	}

    /**
     * Get connection addresss.
     * @return connection address.
     */
    public InetSocketAddress getAddress() {
        return address;
    }

	/**
	 * Get protocol type (tcp, udp, ssl, etc.).
	 * @return protocol type.
	 */
	public String getType() {
		return type;
	}
    
}
