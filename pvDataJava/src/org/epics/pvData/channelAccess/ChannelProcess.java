/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;


/**
 * ChannelProcess - request that a channel be processed..
 * @author mrk
 *
 */
public interface ChannelProcess {
    /**
     * Issue a process request.
     * This fails if the request can not be satisfied.
     * If it fails the channelProcessRequester.processDone is called before process returns.
     * @param lastRequest Is this the last request?
     */
    void process(boolean lastRequest);
    /**
     * Destroy the ChannelProcess.
     */
    void destroy();
}
