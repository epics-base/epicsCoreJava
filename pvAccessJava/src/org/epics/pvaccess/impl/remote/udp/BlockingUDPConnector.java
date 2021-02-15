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

package org.epics.pvaccess.impl.remote.udp;

import org.epics.pvaccess.impl.remote.*;
import org.epics.pvaccess.impl.remote.request.ResponseHandler;

import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.channels.DatagramChannel;


/**
 * UDP broadcast connector.
 * This is not a real connector since it binds.
 *
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class BlockingUDPConnector implements Connector {

    /**
     * Context instance.
     */
    private final Context context;

    /**
     * Send address.
     */
    private final InetSocketAddress[] sendAddresses;

    /**
     * Reuse socket flag.
     */
    private final boolean reuseSocket;

    /**
     * Broadcast flag.
     */
    private final boolean broadcast;

    public BlockingUDPConnector(Context context, boolean reuseSocket, InetSocketAddress[] sendAddresses, boolean broadcast) {
        this.context = context;
        this.reuseSocket = reuseSocket;
        this.sendAddresses = sendAddresses;
        this.broadcast = broadcast;
    }

    /**
     * NOTE: transport client is ignored for broadcast (UDP).
     *
     * @see org.epics.pvaccess.impl.remote.Connector#connect(org.epics.pvaccess.impl.remote.TransportClient, org.epics.pvaccess.impl.remote.request.ResponseHandler, java.net.InetSocketAddress, byte, short)
     */
    public Transport connect(TransportClient client, ResponseHandler responseHandler, InetSocketAddress bindAddress, byte transportRevision, short priority)
            throws ConnectionException {
        context.getLogger().finer("Creating datagram socket to " + bindAddress + ".");

        DatagramChannel socket = null;
        MulticastSocket multicastSocket = null;
        try {
            multicastSocket = new MulticastSocket(bindAddress);

            // set broadcast mode
            if (broadcast)
                multicastSocket.setBroadcast(true);

            // TODO tune buffer sizes?! Win32 defaults are 8k, which is OK
            //socket.socket().setReceiveBufferSize();
            //socket.socket().setSendBufferSize();

            // try already set to reuse in creation of MulticastSocket
//            if (reuseSocket)
//                multicastSocket.setReuseAddress(true);

            // Already bound in creation of MulticastSocket
//            socket.bind(bindAddress);

            // create transport
            return new BlockingUDPTransport(context, responseHandler, multicastSocket,
                    bindAddress, sendAddresses, transportRevision);
        } catch (Throwable th) {
            // close socket, if open
            try {
                if (multicastSocket != null)
                    multicastSocket.close();
            } catch (Throwable t) { /* noop */ }

            throw new ConnectionException("Failed to bind to '" + bindAddress + "'.", bindAddress, ProtocolType.udp.name(), th);
        }

    }


}
