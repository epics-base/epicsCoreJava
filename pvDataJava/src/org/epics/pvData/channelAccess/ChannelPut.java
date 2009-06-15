/**
 * Copyright - See the COPYRIGHT that is included with this disctibution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

import java.util.BitSet;


/**
 * Interface for a channel access put request.
 * @author mrk
 *
 */
public interface ChannelPut {
    /**
     * Get the BitSet which describes the fields that the requester has modified since the last request.
     * @return The BitSet.
     */
    BitSet getBitSet();
    /**
     * Put data to a channel.
     * This fails if the request can not be satisfied.
     * If it fails ChannelPutRequester.putDone is called before put returns.
     */
    void put();
    /**
     * Destroy the ChannelPut.
     */
    void destroy();
}
