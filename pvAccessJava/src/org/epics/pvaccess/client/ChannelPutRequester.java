/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Requester;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;

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
     * @param structure The introspection interface of requested put/get structure or <code>null</code> if the request failed.
     */
    void channelPutConnect(Status status, ChannelPut channelPut, Structure structure);
    /**
     * The request is done. This is always called with no locks held.
     * @param status Completion status.
     * @param channelPut The channelPut interface.
     */
    void putDone(Status status, ChannelPut channelPut);
    /**
     * The get request is done. This is always called with no locks held.
     * @param status Completion status.
     * @param channelPut The channelPut interface.
     * @param pvStructure The PVStructure that holds the data or <code>null</code> if the request failed.
     * @param bitSet The bitSet for that shows what data has changed or <code>null</code> if the request failed.
     */
    void getDone(Status status, ChannelPut channelPut, PVStructure pvStructure, BitSet bitSet);
}
