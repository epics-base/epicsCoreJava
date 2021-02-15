/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

import org.epics.pvdata.pv.PVStructure;


/**
 * Requester for channelGet.
 * @author mrk
 *
 */
public interface ChannelRPC extends ChannelRequest {
    /**
     * Issue an RPC request to the channel.
     * Completion status is reported by calling ChannelRPCRequester.requestDone() callback.
     * @param pvArgument The argument structure for an RPC request.
     */
    void request(PVStructure pvArgument);
}
