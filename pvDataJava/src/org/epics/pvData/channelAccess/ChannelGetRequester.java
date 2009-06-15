/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

import org.epics.pvData.pv.Requester;

/**
 * Requester for a Get.get request.
 * @author mrk
 *
 */
public interface ChannelGetRequester extends Requester {
    /**
     * The client and server have both completed the createChannelGet request.
     * @param channelGet The channelGet interface or null if the request failed.
     * @param isProcessor is the client the record processor?
     */
    void channelGetConnect(ChannelGet channelGet,boolean isProcessor);
    /**
     * The request is done. This is always called with no locks held.
     * @param success Was to request successful.
     */
    void getDone(boolean success);
}
