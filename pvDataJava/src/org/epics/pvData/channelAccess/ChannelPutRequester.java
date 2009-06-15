/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

import org.epics.pvData.pv.Requester;

/**
 * Requester for ChannelPut.
 * @author mrk
 *
 */
public interface ChannelPutRequester extends Requester {
    /**
     * The client and server have both processed the createChannelPut request.
     * @param channelPut The channelPut interface or null if the request failed.
     * @param isProcessor is the client the record processor?
     */
    void channelPutConnect(ChannelPut channelPut,boolean isProcessor);
    /**
     * The request is done. This is always called with no locks held.
     * @param success Was the request successful.
     */
    void putDone(boolean success);
}
