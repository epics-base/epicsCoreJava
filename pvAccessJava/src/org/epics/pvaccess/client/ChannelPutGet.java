/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.PVStructure;


/**
 * Channel access put/get request.
 * The put is performed first, followed optionally by a process request, and then by a get request.
 * @author mrk
 *
 */
public interface ChannelPutGet extends ChannelRequest {
    /**
     * Issue a put/get request. If process was requested when the ChannelPutGet was created this is a put, process, get.
     * This fails if the request can not be satisfied.
     * If it fails ChannelPutGetRequester.putDone is called before putGet returns.
     * @param pvPutStructure The PVStructure that holds the putData.
     * @param putBitSet putPVStructure bit-set (selects what fields to put).
     * @param lastRequest Is this the last request?
     */
    void putGet(PVStructure pvPutStructure, BitSet bitSet, boolean lastRequest);
    /**
     * Get the put PVStructure. The record will not be processed.
     * @param lastRequest Is this the last request?
     */
    void getPut(boolean lastRequest);
    /**
     * Get the get PVStructure. The record will not be processed.
     * @param lastRequest Is this the last request?
     */
    void getGet(boolean lastRequest);
}
