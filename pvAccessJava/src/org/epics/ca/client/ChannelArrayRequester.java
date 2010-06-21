/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca.client;

import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.Status;

/**
 * The requester for a ChannelArray.
 * @author mrk
 *
 */
public interface ChannelArrayRequester extends Requester {
    /**
     * The client and server have both completed the createChannelArray request.
     * @param status Completion status.
     * @param channelArray The channelArray interface or null if the request failed.
     * @param pvArray The PVArray that holds the data.
     */
    void channelArrayConnect(Status status,ChannelArray channelArray,PVArray pvArray);
    /**
     * The request is done. This is always called with no locks held.
     * @param status Completion status.
     */
    void putArrayDone(Status status);
    /**
     * The request is done. This is always called with no locks held.
     * @param status Completion status.
     */
    void getArrayDone(Status status);
    /**
     * The request is done. This is always called with no locks held.
     * @param status Completion status.
     */
    void setLengthDone(Status status);
}
