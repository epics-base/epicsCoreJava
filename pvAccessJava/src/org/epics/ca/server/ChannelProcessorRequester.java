/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca.server;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.Status;

/**
 * An interface implemented by a channel process requester.
 * @author mrk
 *
 */
public interface ChannelProcessorRequester extends Requester{
    /**
     * Called as a result of calling ChannelProcessor.requestProcess.
     */
    void becomeProcessor();
    /**
     * A queueProcessRequest failed.
     * @param reason The reason why the request failed.
     */
    void canNotProcess(String reason);
    /**
     * The result of the process request.
     * This is called with the record still active and locked.
     * The requester can read data from the record.
     * @param status Completion status.
     */
    void recordProcessResult(Status status);
    /**
     * Called to signify process completion.
     * This is called with the record unlocked.
     */
    void recordProcessComplete();
    /**
     * The ChannelProcessorRequester has lost the right to request processing..
     * The requester does not have to call channelProcessor.detach.
     */
    void lostRightToProcess();
}
