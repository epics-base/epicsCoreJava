/*
 * Copyright (c) 2009 by Cosylab
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

package org.epics.pvaccess.impl.remote;

import java.nio.ByteBuffer;

import org.epics.pvaccess.client.Lockable;

/**
 * Interface defining transport sender (instance sending data over transport).
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public interface TransportSender extends Lockable {

	/**
	 * Called by transport.
	 * By this call transport gives callee ownership over the buffer.
	 * Calls on <code>TransportSendControl</code> instance must be made from
	 * calling thread. Moreover, ownership is valid only for the time of call
	 * of this method.
	 * NOTE: these limitations allows efficient implementation.
	 */
	void send(ByteBuffer buffer, TransportSendControl control);
}
