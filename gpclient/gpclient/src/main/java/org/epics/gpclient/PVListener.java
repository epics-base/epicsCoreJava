/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient;

/**
 * Callback for delivery notification of new value. 
 *
 * @author carcassi
 * @param <R> the read object type
 * @param <W> the write object type
 */
public interface PVListener<R, W> extends PVReaderListener<R>, PVWriterListener<W> {
    
    /**
     * Notified when the value was written.
     * 
     * @param event the event
     * @param pv the pv that generated the event
     */
    public void pvChanged(PVEvent event, PV<R, W> pv);
}
