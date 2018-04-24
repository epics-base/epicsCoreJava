/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

/**
 * Callback for delivery notification of new value. 
 *
 * @author carcassi
 * @param <R> the read object type
 * @param <W> the write object type
 */
public interface PVListener<R, W> {
    
    /**
     * Notified when the value was written.
     * 
     * @param event the event
     * @param pv the pv that generated the event
     */
    public void pvChanged(PVEvent event, PV<R, W> pv);
}
