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
public interface PVListener<R, W> extends PVReaderListener<R>, PVWriterListener<W> {
    
    /**
     * Notified when the value was written.
     * 
     * @param event the writer event
     * @param pvWriter
     */
    public void pvChanged(PVEvent event, PV<R, W> pv);
}
