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
 * Requester for ChannelPut.
 * @author mrk
 *
 */
public interface ChannelPutRequester extends Requester {
    /**
     * The client and server have both processed the createChannelPut request.
     * @param status Completion status.
     * @param channelPut The channelPut interface or null if the request failed.
     * @param pvStructure The PVStructure that holds the data.
     * @param bitSet The bitSet for that shows what data has changed.
     */
    void channelPutConnect(Status status,ChannelPut channelPut,PVStructure pvStructure,BitSet bitSet);
    /**
     * The request is done. This is always called with no locks held.
     * @param status Completion status.
     */
    void putDone(Status status);
    /**
     * The get request is done. This is always called with no locks held.
     * @param status Completion status.
     */
    void getDone(Status status);
}
