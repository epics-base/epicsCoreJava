/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca.server;

import org.epics.pvData.property.TimeStamp;
/**
 * Interface for requesting the channel be processed.
 * @author mrk
 *
 */
public interface ChannelProcessor {
    /**
     * Detach from being record processor.
     */
    void detach();
    /**
     * Request to process. ChannelProcessorRequester.becomeProcessor is called when the requester can call setActive and/or process.
     */
    void requestProcess();
    /**
     * Process the record.
     * A value of false means that the request failed.
     * If the request failed Requester .message is called.
     * @param leaveActive Should the record be left active after
     * process is complete? This is true if the requester wants to read
     * data from the record after processing.
     * @param timeStamp The timeStamp to be assigned to the record.
     * This can be null.
     */
    void process(boolean leaveActive,TimeStamp timeStamp);
    /**
     * Called if process requested that the record be left active.
     */
    void setInactive();
}
