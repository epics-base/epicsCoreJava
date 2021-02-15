/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource;

import org.epics.gpclient.PVWriter;

/**
 * Exception thrown when a {@link PVWriter} is opened on a channel that
 * cannot be written.
 *
 * @author carcassi
 */
public class ReadOnlyChannelException extends RuntimeException {

    /**
     * Constructs a new runtime exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ReadOnlyChannelException(String message) {
        super(message);
    }

    /**
     * Constructs a new runtime exception with the specified detail message.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public ReadOnlyChannelException(String message, Throwable cause) {
        super(message, cause);
    }

}
