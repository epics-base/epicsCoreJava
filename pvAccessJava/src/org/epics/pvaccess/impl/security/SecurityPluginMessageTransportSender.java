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
package org.epics.pvaccess.impl.security;

import java.nio.ByteBuffer;

import org.epics.pvaccess.impl.remote.SerializationHelper;
import org.epics.pvaccess.impl.remote.TransportSendControl;
import org.epics.pvaccess.impl.remote.TransportSender;
import org.epics.pvdata.pv.PVField;

public class SecurityPluginMessageTransportSender implements TransportSender
{
	private final PVField data;

	public SecurityPluginMessageTransportSender(PVField data)
	{
		this.data = data;
	}

	public void lock() {
		// noop
	}

	public void unlock() {
		// noop
	}

	public void send(ByteBuffer buffer, TransportSendControl control) {

		control.startMessage((byte)5, 0);

		SerializationHelper.serializeFull(buffer, control, data);

		// send immediately
		control.flush(true);

	}

}
