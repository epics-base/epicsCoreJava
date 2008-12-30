/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

/**
 * Types for messages.
 * @author mrk
 *
 */
public enum MessageType {
    /**
     * Informational message.
     */
    info,
    /**
     * Warning message.
     */
    warning,
    /**
     * Error message.
     */
    error,
    /**
     * Fatal message.
     */
    fatalError
}
