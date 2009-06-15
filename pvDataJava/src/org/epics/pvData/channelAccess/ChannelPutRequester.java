/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

import org.epics.pvData.pv.Requester;

/**
 * Requester for ChannelPut requests.
 * @author mrk
 *
 */
public interface ChannelPutRequester extends Requester {
    /**
     * The request is done. This is always called with no locks held.
     * @param success Was to request successful.
     */
    void putDone(boolean success);
}
