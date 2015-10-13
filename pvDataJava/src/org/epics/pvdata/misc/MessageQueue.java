/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.misc;

import org.epics.pvdata.pv.MessageType;


/**
 * A queue for Requester messages.
 * @author mrk
 *
 */
public interface MessageQueue {
    /**
     * Get the next message.
     *
     * @return the next message or null if no messages
     */
    MessageNode get();

    /**
     * PutFactory a new message into the queue.
     *
     * @param message the message
     * @param messageType the message type
     * @param replaceLast whether the last message should be replaced by this message if queue is full
     * @return true if the message was put into the queue or false if the queue was full
     */
    boolean put(String message, MessageType messageType, boolean replaceLast);

    /**
     * Is the message queue empty?
     * 
     * @return (false,true) if it (is not, is) empty
     */
    boolean isEmpty();

    /**
     * Is the queue full?
     *
     * @return (false,true) if there (is, is not) a free MessageNode
     */
    boolean isFull();

    /**
     * Get the number of calls to replaceFirst and/or replaceLast since the last call.
     *
     * @return the number of calls to replaceFirst and/or replaceLast since the last call
     */
    int getClearOverrun();
}
