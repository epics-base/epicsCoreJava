/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess.v2;


/**
 * Interface returned by some factory that is not part of pvData.
 * @author mrk
 *
 */
public interface ChannelAccess {
    /**
     * Create a channel.
     * If successful channelRequester.channelCreated is called.
     * @param channelName The channel name, which is also the record name.
     * @param channelRequester The requester.
     * @param timeOut timeout value.
     */
    void createChannel(String channelName, ChannelRequester channelRequester, double timeOut);
}
