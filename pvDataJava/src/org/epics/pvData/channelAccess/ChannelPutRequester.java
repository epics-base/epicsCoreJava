/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

import org.epics.pvData.pv.PVStructure;
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
     * @param pvStructure The PVStructure that holds the data.
     */
    void channelPutConnect(ChannelPut channelPut,PVStructure pvStructure);
    /**
     * The request is done. This is always called with no locks held.
     * @param success Was the request successful.
     */
    void putDone(boolean success);
}
