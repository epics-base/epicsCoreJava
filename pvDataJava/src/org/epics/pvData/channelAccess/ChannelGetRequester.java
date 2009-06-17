/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;

/**
 * Requester for channelGet.
 * @author mrk
 *
 */
public interface ChannelGetRequester extends Requester {
    /**
     * The client and server have both completed the createChannelGet request.
     * @param channelGet The channelGet interface or null if the request failed.
     * @param pvStructure The PVStructure that holds the data.
     */
    void channelGetConnect(ChannelGet channelGet,PVStructure pvStructure);
    /**
     * The request is done. This is always called with no locks held.
     * @param success Was to request successful.
     */
    void getDone(boolean success);
}
