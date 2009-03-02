/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.misc;

import org.epics.pvData.pv.MessageType;


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
