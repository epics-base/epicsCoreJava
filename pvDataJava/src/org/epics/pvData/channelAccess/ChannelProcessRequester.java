/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

import org.epics.pvData.pv.Requester;

/**
 * Callback for a channel process request.
 * @author mrk
 *
 */
public interface ChannelProcessRequester extends Requester {
    /**
     * The process request is done. This is always called with no locks held.
     * @param success was the record actually processed?
     */
    void processDone(boolean success);
}
