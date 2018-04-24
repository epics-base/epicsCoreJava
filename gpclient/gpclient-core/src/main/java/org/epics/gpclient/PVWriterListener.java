/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient;

/**
 * Callback for delivery notification of new value. 
 *
 * @param <T> the type of writer for the listener
 * @author carcassi
 */
public interface PVWriterListener<T> {
    
    /**
     * Notified when the value was written.
     * 
     * @param event the writer event
     * @param pvWriter the writer that generated the event
     */
    public void pvChanged(PVEvent event, PVWriter<T> pvWriter);
}
