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

package org.epics.pvaccess.server.impl.remote.handlers;

import org.epics.pvaccess.impl.remote.request.AbstractResponseHandler;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;


/**
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public abstract class AbstractServerResponseHandler extends AbstractResponseHandler {

	/**
	 * Context instance.
	 */
	protected final ServerContextImpl context;

	public AbstractServerResponseHandler(ServerContextImpl context, String description) {
		super(description, context.getDebugLevel() >= 3);
		this.context = context;
	}

}
