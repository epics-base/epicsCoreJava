/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

/**
 * Callback for notification of new events.
 *
 * @param <R> the read type
 * @author carcassi
 */
public interface PVReaderListener<R> {

    /**
     * Called when a new event is available to be processed.
     *
     * @param event the event
     * @param pvReader the pv associated with the event
     */
    void pvChanged(PVEvent event, PVReader<R> pvReader);

}
