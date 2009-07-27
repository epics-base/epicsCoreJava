/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess.v2;

import java.util.BitSet;

import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;

/**
 *  Requester for ChannelMonitor.
 * @author mrk
 *
 */
public interface ChannelMonitorRequester extends Requester{
    /**
     * The client and server have both completed the createMonitor request.
     * @param channelMonitor The channelMonitor interface or null if the request failed.
     */
    void channelMonitorConnect(ChannelMonitor channelMonitor);
    /**
     * A monitor event has occurrence.
     * @param pvStructure The PVStructure holding data.
     * @param changeBitSet The bitSet showing which fields changed.
     * @param overrunBitSet The bitSet showing which fields were changed more than once.
     */
    void monitorEvent(PVStructure pvStructure, BitSet changeBitSet, BitSet overrunBitSet);
    /**
     * unlisten 
     */
    void unlisten();
}
