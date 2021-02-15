/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Requester;
import org.epics.pvdata.pv.Status;

/**
 * Requester for channelGet.
 * @author mrk
 *
 */
public interface ChannelRPCRequester extends Requester {
    /**
     * The client and server have both completed the createChannelGet request.
     * @param status Completion status.
     * @param channelRPC The channelRPC interface or <code>null</code> if the request failed.
     */
    void channelRPCConnect(Status status, ChannelRPC channelRPC);
    /**
     * The request is done. This is always called with no locks held.
     * @param status Completion status.
     * @param channelRPC The channelRPC interface.
     * @param pvResponse The response data for the RPC request or <code>null</code> if the request failed.
     */
    void requestDone(Status status, ChannelRPC channelRPC, PVStructure pvResponse);
}
