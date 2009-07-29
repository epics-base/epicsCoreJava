/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;


/**
 * Interface implemented by code that can provide access to the record
 * to which a channel connects.
 * @author mrk
 *
 */
public interface ChannelProvider {
    /**
     * Terminate.
     */
    void destroy();
    /**
     * Get the provider name.
     * @return The name.
     */
    String getProviderName();
    /**
     * Find a channel.
     * @param channelName The channel name.
     * @param channelFindRequester The requester.
     * @return An interface for the find.
     */
    ChannelFind channelFind(String channelName,ChannelFindRequester channelFindRequester);
    /**
     * Create a channel.
     * @param channelName The name of the channel.
     * @param channelRequester The requester.
     */
    void createChannel(String channelName,ChannelRequester channelRequester);
}
