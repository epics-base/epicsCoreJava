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

import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.client.impl.remote.ChannelImpl;
import org.epics.pvaccess.client.impl.remote.ClientContextImpl;
import org.epics.pvaccess.impl.remote.Transport;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.StatusCreate;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * PVA create channel response.
 *
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $id$
 */
public class CreateChannelHandler extends AbstractClientResponseHandler {

    /**
     * Status factory.
     */
    private static final StatusCreate statusCreate = PVFactory.getStatusCreate();

    public CreateChannelHandler(ClientContextImpl context) {
        super(context, "Create channel");
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.impl.remote.AbstractResponseHandler#handleResponse(java.net.InetSocketAddress, org.epics.pvaccess.core.Transport, byte, byte, int, java.nio.ByteBuffer)
     */
    @Override
    public void handleResponse(InetSocketAddress responseFrom, Transport transport, byte version, byte command, int payloadSize, ByteBuffer payloadBuffer) {
        super.handleResponse(responseFrom, transport, version, command, payloadSize, payloadBuffer);

        transport.ensureData(2 * Integer.SIZE / Byte.SIZE);
        final int cid = payloadBuffer.getInt();
        final int sid = payloadBuffer.getInt();
        final Status status = statusCreate.deserializeStatus(payloadBuffer, transport);

        ChannelImpl channel = context.getChannel(cid);
        if (channel != null) {
            // failed check
            if (!status.isSuccess()) {

                String logMessage = "Failed to create channel '" + channel.getChannelName() + "': " + status.getMessage();
                String stackDump = status.getStackDump();
                if (stackDump != null && stackDump.trim().length() != 0)
                    logMessage += "\n" + stackDump;
                context.getLogger().fine(logMessage);

                channel.createChannelFailed();
                return;
            }

            //final short acl = payloadBuffer.getShort();

            channel.connectionCompleted(sid);
        }

    }

}
