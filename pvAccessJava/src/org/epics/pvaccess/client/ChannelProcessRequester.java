/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

import org.epics.pvdata.pv.Requester;
import org.epics.pvdata.pv.Status;

/**
 * Requester for channelProcess.
 * @author mrk
 *
 */
public interface ChannelProcessRequester extends Requester {
    /**
     * The client and server have both completed the createChannelProcess request.
     * @param status Completion status.
     * @param channelProcess The channelProcess interface or <code>null</code> if the client could not become
     * the record processor.
     */
    void channelProcessConnect(Status status, ChannelProcess channelProcess);
    /**
     * The process request is done. This is always called with no locks held.
     * @param status Completion status.
     * @param channelProcess The channelProcess interface.
     */
    void processDone(Status status, ChannelProcess channelProcess);
}
