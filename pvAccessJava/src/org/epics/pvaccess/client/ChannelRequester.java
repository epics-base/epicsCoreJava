/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

import org.epics.pvaccess.client.Channel.ConnectionState;
import org.epics.pvdata.pv.Requester;
import org.epics.pvdata.pv.Status;
/**
 * Listener for connect state changes.
 * @author mrk
 *
 */
public interface ChannelRequester extends Requester {
    /**
     * A channel has been created. This may be called multiple times if there are multiple providers.
     * @param status Completion status.
     * @param channel The channel or <code>null</code> if the request failed.
     */
    void channelCreated(Status status, Channel channel);
    /**
     * A channel connection state change has occurred.
     * @param channel The channel.
     * @param connectionState The new connection state.
     */
    void channelStateChange(Channel channel, ConnectionState connectionState);
}
