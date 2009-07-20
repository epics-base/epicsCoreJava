/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

import org.epics.pvData.pv.Requester;
/**
 * Listener for connect state changes.
 * @author mrk
 *
 */
public interface ChannelRequester extends Requester {
    /**
     * A channel has been created. This may be called multiple times if there are multiple providers.
     * @param channel The channel.
     */
    void channelCreated(Channel channel);
    /**
     * Channel not created. This may be called multiple times if there are multiple providers.
     */
    void channelNotCreated();
    /**
     * A channel connection state change has occurred.
     * @param c The channel.
     * @param isConnected (false,true) if new state (is not, is) connected.
     */
    void channelStateChange(Channel c,boolean isConnected);
    /**
     * The channel will not honor any further requests.
     * @param c The channel.
     */
    void destroy(Channel c);
}
