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
 * Requester for ChannelPutGet.
 * @author mrk
 *
 */
public interface ChannelPutGetRequester extends Requester
{
    /**
     * The client and server have both completed the createChannelPutGet request.
     * @param status Completion status.
     * @param channelPutGet The channelPutGet interface or null if the request failed.
     * @param putStructure The put structure introspection data or <code>null</code> if the request failed.
     * @param getStructure The get structure introspection data or <code>null</code> if the request failed.
     */
    void channelPutGetConnect(Status status, ChannelPutGet channelPutGet,
    		Structure putStructure, Structure getStructure);
    /**
     * The putGet request is done. This is always called with no locks held.
     * @param status Completion status.
     * @param channelPutGet The channelPutGet interface.
     * @param getPVStructure The PVStructure that holds the getData or <code>null</code> if the request failed.
     * @param getBitSet getPVStructure changed bit-set or <code>null</code> if the request failed.
     */
    void putGetDone(Status status, ChannelPutGet channelPutGet, PVStructure getPVStructure, BitSet getBitSet);
    /**
     * The getPut request is done. This is always called with no locks held.
     * @param status Completion status.
     * @param channelPutGet The channelPutGet interface.
     * @param putPVStructure The PVStructure that holds the putData or <code>null</code> if the request failed.
     * @param putBitSet putPVStructure changed bit-set or <code>null</code> if the request failed.
     */
    void getPutDone(Status status, ChannelPutGet channelPutGet, PVStructure putPVStructure, BitSet putBitSet);
    /**
     * The getGet request is done. This is always called with no locks held.
     * @param status Completion status.
     * @param channelPutGet The channelPutGet interface.
     * @param getPVStructure The PVStructure that holds the getData or <code>null</code> if the request failed.
     * @param getBitSet getPVStructure changed bit-set or <code>null</code> if the request failed.
     */
    void getGetDone(Status status, ChannelPutGet channelPutGet, PVStructure getPVStructure, BitSet getBitSet);
}
