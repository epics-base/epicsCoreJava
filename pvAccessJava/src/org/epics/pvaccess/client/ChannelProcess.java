/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;


/**
 * ChannelProcess - request that a channel be processed..
 * @author mrk
 *
 */
public interface ChannelProcess extends ChannelRequest {
    /**
     * Issue a process request.
     * Completion status is reported by calling ChannelProcessRequester.processDone() callback.
     */
    void process();
}
