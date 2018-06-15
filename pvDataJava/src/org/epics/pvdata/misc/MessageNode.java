/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.misc;

import org.epics.pvdata.pv.MessageType;


/**
 * Node for a MessageQueue.
 * @author mrk
 *
 */
public class MessageNode {
    /**
     * The message.
     */
    public String message;

    /**
     * The message type.
     */
    public MessageType messageType;
}
