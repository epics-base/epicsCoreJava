/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

import java.util.Set;

import org.epics.pvdata.pv.Status;


/**
 * @author mrk
 *
 */
public interface ChannelListRequester {
    /**
     * @param status Completion status.
     * @param channelFind The ChannelFind instance.
     * @param channelName A set of channel names hosted by the provider, <code>null</code> on failure.
     * @param hasDynamic true if the provider supports creation of dynamic channels (on the fly)
     * 			and they cannot be listed in channelNames, otherwise false.
     */
    void channelListResult(Status status, ChannelFind channelFind, Set<String> channelName, boolean hasDynamic);
}
