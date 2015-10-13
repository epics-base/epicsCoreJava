/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
