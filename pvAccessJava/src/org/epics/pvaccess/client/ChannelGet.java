/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

/**
 * Request to get data from a channel.
 * @author mrk
 *
 */
public interface ChannelGet extends ChannelRequest {
    /**
     * Get data from the channel.
     * Completion status is reported by calling ChannelGetRequester.getDone() callback.
     */
    void get();
}
