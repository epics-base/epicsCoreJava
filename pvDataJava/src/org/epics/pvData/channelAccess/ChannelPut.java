/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;



/**
 * Interface for a channel access put request.
 * @author mrk
 *
 */
public interface ChannelPut  extends ChannelRequest {
    /**
     * Put data to a channel.
     * This fails if the request can not be satisfied.
     * If it fails ChannelPutRequester.putDone is called before put returns.
     * @param lastRequest Is this the last request?
     */
    void put(boolean lastRequest);
    /**
     * Get the current data.
     */
    void get();
}
