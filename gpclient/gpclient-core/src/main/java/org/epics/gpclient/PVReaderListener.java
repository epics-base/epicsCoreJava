/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

/**
 * Callback for any change in the PV value. Cannot simply use a PropertyChangedListener
 * because the payload of the PV will be typically updated in place for complex
 * data structures, and therefore the data object is the same and would not
 * trigger a PropertyChangedEvent.
 *
 * @param <T> the type of reader for the listener
 * @author carcassi
 */
public interface PVReaderListener<T> {

    /**
     * Notified when the value of the PV has changed.
     * 
     * @param event the reader event
     * @param pvReader the reader that generated the event
     */
    void pvChanged(PVEvent event, PVReader<T> pvReader);

}
