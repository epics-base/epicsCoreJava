/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

/**
 * Requester for a putGet request.
 * @author mrk
 *
 */
public interface ChannelPutGetRequester {
    /**
     * The client and server have both completed the createChannelPutGet request.
     * @param channelPutGet The channelPutGet interface or null if the request failed.
     * @param isProcessor is the client the record processor?
     */
    void channelPutGetConnect(ChannelPutGet channelPutGet,boolean isProcessor);
    /**
     * The request is done. This is always called with no locks held.
     * @param success Was the request successful
     */
    void putGetDone(boolean success);
}
