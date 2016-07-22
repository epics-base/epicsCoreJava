/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;



/**
 * Base interface for requesters.
 * @author mrk
 *
 */
public interface Requester {
    /**
     * Get the name of the requester.
     * 
     * @return The name.
     */
    String getRequesterName();

    /**
     * Report a message.
     *
     * @param message the message
     * @param messageType the message type
     */
    void message(String message, MessageType messageType);
}
