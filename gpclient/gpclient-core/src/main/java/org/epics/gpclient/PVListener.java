/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

/**
 * Callback for notification of new events.
 *
 * @param <R> the read type
 * @param <W> the write type
 * @author carcassi
 */
public interface PVListener<R, W> {

    /**
     * Called when a new event is available to be processed.
     *
     * @param event the event
     * @param pv the pv associated with the event
     */
    public void pvChanged(PVEvent event, PV<R, W> pv);
}
