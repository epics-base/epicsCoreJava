/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess.v2;

/**
 * Base interface for all channel requests.
 * @author mse
 */
public interface ChannelRequest {
    /**
     * Destroy the request.
     */
    void destroy();
}
