/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

/**
 * Exception thrown when a {@link PVReader} or {@link PVWriter} exceed their
 * timeout.
 *
 * @author carcassi
 */
public class TimeoutException extends RuntimeException {

    TimeoutException(String message) {
        super(message);
    }

}
