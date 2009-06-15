/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;


/**
 * Interface returned by some factory that is not part of pvData.
 * @author mrk
 *
 */
public interface ChannelAccess {
    /**
     * Create a channel.
     * @param channelName The channel name, which is also the record name.
     * @return The channel or null if it could not be created.
     */
    Channel createChannel(String channelName, ChannelListener listener);
}
