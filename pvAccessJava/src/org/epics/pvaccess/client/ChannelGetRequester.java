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
 * Requester for channelGet.
 * @author mrk
 *
 */
public interface ChannelGetRequester extends Requester {
    /**
     * The client and server have both completed the createChannelGet request.
     * @param status Completion status.
     * @param channelGet The channelGet interface or <code>null</code> if the request failed.
     * @param structure The introspection interface of requested get structure or <code>null</code> if the request failed.
     */
    void channelGetConnect(Status status, ChannelGet channelGet, Structure structure);
    /**
     * The request is done. This is always called with no locks held.
     * @param status Completion status.
     * @param channelGet The channelGet interface.
     * @param pvStructure The PVStructure that holds the data or <code>null</code> if the request failed.
     * @param bitSet The bitSet for that shows what data has changed or <code>null</code> if the request failed.
     */
    void getDone(Status status, ChannelGet channelGet, PVStructure pvStructure, BitSet bitSet);
}
