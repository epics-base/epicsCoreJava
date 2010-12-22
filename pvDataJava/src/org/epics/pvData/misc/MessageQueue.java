/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.misc;

import org.epics.pvData.pv.MessageType;


/**
 * A queue for Requester messages.
 * @author mrk
 *
 */
public interface MessageQueue {
    /**
     * Get the next message.
     * @return The next message or null if no messages.
     */
    MessageNode get();
    /**
     * PutFactory a new message into the queue.
     * @param message The message.
     * @param messageType The message type.
     * @param replaceLast If queue is full should last message be replaced by this message.
     * @return true if the message was put into the queue or false if the queue was full.
     */
    boolean put(String message,MessageType messageType,boolean replaceLast);
    /**
     * Is the message queue empty?
     * @return (false,true) if it (is not, is) empty.
     */
    boolean isEmpty();
    /**
     * Is the queue full?
     * @return (false,true) if there (is, is not) a free MessageNode.
     */
    boolean isFull();
    /**
     * Get the number of calls to replaceFirst and/or replaceLast since the last call.
     * @return The number.
     */
    int getClearOverrun();
}
