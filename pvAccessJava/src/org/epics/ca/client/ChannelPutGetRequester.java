/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca.client;

import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.Status;

/**
 * Requester for ChannelPutGet.
 * @author mrk
 *
 */
public interface ChannelPutGetRequester extends Requester
{
    /**
     * The client and server have both completed the createChannelPutGet request.
     * @param status Completion status.
     * @param channelPutGet The channelPutGet interface or null if the request failed.
     * @param pvPutStructure The PVStructure that holds the putData.
     * @param pvGetStructure The PVStructure that holds the getData.
     */
    void channelPutGetConnect(Status status,ChannelPutGet channelPutGet,
            PVStructure pvPutStructure,PVStructure pvGetStructure);
    /**
     * The putGet request is done. This is always called with no locks held.
     * @param status Completion status.
     */
    void putGetDone(Status status);
    /**
     * The getPut request is done. This is always called with no locks held.
     * @param status Completion status.
     */
    void getPutDone(Status status);
    /**
     * The getGet request is done. This is always called with no locks held.
     * @param status Completion status.
     */
    void getGetDone(Status status);
}
