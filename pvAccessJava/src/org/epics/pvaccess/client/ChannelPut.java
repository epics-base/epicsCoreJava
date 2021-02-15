/*
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
     * Completion status is reported by calling ChannelPutRequester.putDone() callback.
     * @param pvPutStructure The PVStructure that holds the putData.
     * @param bitSet selects what fields to put.
     */
    void put(PVStructure pvPutStructure, BitSet bitSet);
    /**
     * Get the current data.
     */
    void get();
}
