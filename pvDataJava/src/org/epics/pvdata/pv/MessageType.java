/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

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
