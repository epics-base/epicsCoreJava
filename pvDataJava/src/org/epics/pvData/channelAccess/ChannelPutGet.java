/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

import java.util.BitSet;


/**
 * Channel access put/get request.
 * The put is performed first, followed optionally by a process request, and then by a get request.
 * @author mrk
 *
 */
public interface ChannelPutGet extends ChannelRequest {
    /**
     * Get the BitSet which describes the output fields that the requester has modified since the last request.
     * @return The BitSet.
     */
    BitSet getPutBitSet();
    /**
     * Get the BitSet which describes which input fields have been modified since the last request.
     * @return The BitSet.
     */
    BitSet getGetBitSet();
    /**
     * Issue a put/get request. If process was requested when the ChannelPutGet was created this is a put, process, get.
     * This fails if the request can not be satisfied.
     * If it fails ChannelPutGetRequester.putDone is called before putGet returns.
     * @param lastRequest Is this the last request?
     */
    void putGet(boolean lastRequest);
    /**
     * Get the put PVStructure. The record will not be processed.
     */
    void getPut();
    /**
     * Get the get PVStructure. The record will not be processed.
     */
    void getGet();
}
