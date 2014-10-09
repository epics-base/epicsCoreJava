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

package org.epics.pvaccess.client.impl.remote.handlers;

import org.epics.pvaccess.client.impl.remote.ClientContextImpl;
import org.epics.pvaccess.impl.remote.request.AbstractResponseHandler;


/**
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public abstract class AbstractClientResponseHandler extends AbstractResponseHandler {

	/**
	 * Context instance.
	 */
	protected final ClientContextImpl context;

	/**
	 * @param context
	 * @param description
	 */
	public AbstractClientResponseHandler(ClientContextImpl context, String description) {
		super(description, context.getDebugLevel() >= 3);
		this.context = context;
	}

}
