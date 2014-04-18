/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.PVStructure;



/**
 * Interface for a channel access put request.
 * @author mrk
 *
 */
public interface ChannelPut extends ChannelRequest {
    /**
     * Put data to a channel.
     * This fails if the request can not be satisfied.
     * If it fails ChannelPutRequester.putDone is called before put returns.
     * @param pvPutStructure The PVStructure that holds the putData.
     * @param putBitSet putPVStructure bit-set (selects what fields to put).
     * @param lastRequest Is this the last request?
     */
    void put(PVStructure pvPutStructure, BitSet bitSet, boolean lastRequest);
    /**
     * Get the current data.
     * @param lastRequest Is this the last request?
     */
    void get(boolean lastRequest);
}
