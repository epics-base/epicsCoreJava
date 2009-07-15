/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

import java.util.BitSet;

import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;

/**
 * Requester for ChannelPutGet.
 * @author mrk
 *
 */
public interface ChannelPutGetRequester extends Requester
{
    /**
     * The client and server have both completed the createChannelPutGet request.
     * @param channelPutGet The channelPutGet interface or null if the request failed.
     * @param pvPutStructure The PVStructure that holds the putData.
     * @param putBitSet The bitSet for that shows what putData has changed.
     * @param pvGetStructure The PVStructure that holds the getData.
     * @param getBitSet The bitSet for that shows what getData has changed.
     */
    void channelPutGetConnect(ChannelPutGet channelPutGet,
            PVStructure pvPutStructure,BitSet putBitSet,
            PVStructure pvGetStructure,BitSet getBitSet);
    /**
     * The putGet request is done. This is always called with no locks held.
     * @param success Was the request successful
     */
    void putGetDone(boolean success);
    /**
     * The getPut request is done. This is always called with no locks held.
     * @param success Was the request successful
     */
    void getPutDone(boolean success);
    /**
     * The getGet request is done. This is always called with no locks held.
     * @param success Was the request successful
     */
    void getGetDone(boolean success);
}
