/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

/**
 * Request to get data from a channel.
 * @author mrk
 *
 */
public interface ChannelGet extends ChannelRequest {
    /**
     * Get data from the channel.
     * This fails if the request can not be satisfied.
     * If it fails ChannelGetRequester.getDone is called before get returns.
     * @param lastRequest Is this the last request?
     */
    void get(boolean lastRequest);
}
