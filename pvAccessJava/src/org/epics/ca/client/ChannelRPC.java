/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca.client;


/**
 * Requester for channelGet.
 * @author mrk
 *
 */
public interface ChannelRPC extends ChannelRequest {
    /**
     * Issue an RPC request to the channel.
     * This fails if the request can not be satisfied.
     * @param lastRequest Is this the last request?
     */
    void request(boolean lastRequest);
}
