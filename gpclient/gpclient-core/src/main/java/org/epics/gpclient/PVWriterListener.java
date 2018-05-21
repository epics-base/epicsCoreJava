/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

/**
 * Callback for notification of new events. 
 *
 * @param <W> the write type
 * @author carcassi
 */
public interface PVWriterListener<W> {
    
    /**
     * Notified when the value was written.
     * 
     * @param event the writer event
     * @param pvWriter the writer that generated the event
     */
    public void pvChanged(PVEvent event, PVWriter<W> pvWriter);
}
