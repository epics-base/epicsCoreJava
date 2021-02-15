/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

/**
 * Exception thrown when a channel or an expression cannot return the type
 * requested.
 *
 * @author carcassi
 */
public class TypeMismatchException extends RuntimeException {

    /**
     * Creates a new exception with the given message.
     *
     * @param message the message
     */
    public TypeMismatchException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the given message and cause.
     *
     * @param message the message
     * @param cause the cause
     */
    public TypeMismatchException(String message, Throwable cause) {
        super(message, cause);
    }



}
