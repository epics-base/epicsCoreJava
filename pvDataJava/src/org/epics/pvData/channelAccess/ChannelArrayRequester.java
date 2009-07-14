/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

import org.epics.pvData.pv.*;
import org.epics.pvData.misc.*;

/**
 * The requester for a ChannelArray.
 * @author mrk
 *
 */
public interface ChannelArrayRequester extends Requester {
    /**
     * The client and server have both completed the createChannelArray request.
     * @param channelArray The channelArray interface or null if the request failed.
     * @param pvArray The PVArray that holds the data.
     */
    void channelArrayConnect(ChannelArray channelArray,PVArray pvArray);
    /**
     * The request is done. This is always called with no locks held.
     * @param success Was the request successful.
     */
    void putArrayDone(boolean success);
    /**
     * The request is done. This is always called with no locks held.
     * @param success Was the request successful.
     */
    void getArrayDone(boolean success);
}
