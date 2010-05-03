/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca.client;

import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.Status;

/**
 * Requester for channelGet.
 * @author mrk
 *
 */
public interface ChannelGetRequester extends Requester {
    /**
     * The client and server have both completed the createChannelGet request.
     * @param status Completion status.
     * @param channelGet The channelGet interface or null if the request failed.
     * @param pvStructure The PVStructure that holds the data.
     * @param bitSet The bitSet for that shows what data has changed.
     */
    void channelGetConnect(Status status,ChannelGet channelGet,PVStructure pvStructure,BitSet bitSet);
    /**
     * The request is done. This is always called with no locks held.
     * @param status Completion status.
     */
    void getDone(Status status);
}
