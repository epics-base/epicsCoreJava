/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca.server;

import org.epics.ca.client.ChannelProvider;

/**
 * Interface implemented by code that can provide access to the record
 * to which a channel connects.
 * @author mrk
 *
 */
public interface ChannelServer extends ChannelProvider{
    /**
     * Register an interface for a channelProcessProvider.
     * @param channelProcessProvider The provider.
     * @return (false,true) if the provider was registered.
     */
    boolean registerChannelProcessProvider(ChannelProcessorProvider channelProcessProvider);
}
