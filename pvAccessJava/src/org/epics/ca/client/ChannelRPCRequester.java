/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca.client;

import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.Status;

/**
 * Requester for channelGet.
 * @author mrk
 *
 */
public interface ChannelRPCRequester extends Requester {
    /**
     * The client and server have both completed the createChannelGet request.
     * @param status Completion status.
     * @param channelRPC The channelRPC interface or null if the request failed.
     */
    void channelRPCConnect(Status status,ChannelRPC channelRPC);
    /**
     * The request is done. This is always called with no locks held.
     * @param status Completion status.
     * @param pvResponse The response data for the RPC request.
     */
    void requestDone(Status status,PVStructure pvResponse);
}
