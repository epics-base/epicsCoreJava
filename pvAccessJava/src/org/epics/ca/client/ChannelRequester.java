/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca.client;

import org.epics.ca.client.Channel.ConnectionState;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.Status;
/**
 * Listener for connect state changes.
 * @author mrk
 *
 */
public interface ChannelRequester extends Requester {
    /**
     * A channel has been created. This may be called multiple times if there are multiple providers.
     * @param status Completion status.
     * @param channel The channel.
     */
    void channelCreated(Status status,Channel channel);
    /**
     * A channel connection state change has occurred.
     * @param c The channel.
     * @param connectionState The new connection state.
     */
    void channelStateChange(Channel c, ConnectionState connectionState);
}
