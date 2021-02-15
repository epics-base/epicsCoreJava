/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

import org.epics.pvdata.pv.Array;
import org.epics.pvdata.pv.PVArray;
import org.epics.pvdata.pv.Requester;
import org.epics.pvdata.pv.Status;

/**
 * The requester for a ChannelArray.
 * @author mrk
 *
 */
public interface ChannelArrayRequester extends Requester {
    /**
     * The client and server have both completed the createChannelArray request.
     * <code>array</code> introspection is always be a non-fixed array instance,
     * even if it's connected to a fixed array.
     * @param status Completion status.
     * @param channelArray The channelArray interface or <code>null</code> if the request failed.
     * @param array The Array introspection interface or <code>null</code> if the request failed.
     */
    void channelArrayConnect(Status status, ChannelArray channelArray, Array array);
    /**
     * The request is done. This is always called with no locks held.
     * @param status Completion status.
     * @param channelArray The channelArray interface.
     */
    void putArrayDone(Status status, ChannelArray channelArray);
    /**
     * The request is done. This is always called with no locks held.
     * @param status Completion status.
     * @param channelArray The channelArray interface.
     * @param pvArray The PVArray that holds the data or <code>null</code> if the request failed.
     */
    void getArrayDone(Status status, ChannelArray channelArray, PVArray pvArray);
    /**
     * The request is done. This is always called with no locks held.
     * @param status Completion status.
     * @param channelArray The channelArray interface.
     * @param length The length of the array, 0 if the request failed.
     */
    void getLengthDone(Status status, ChannelArray channelArray, int length);
    /**
     * The request is done. This is always called with no locks held.
     * @param status Completion status.
     * @param channelArray The channelArray interface.
     */
    void setLengthDone(Status status, ChannelArray channelArray);
}
