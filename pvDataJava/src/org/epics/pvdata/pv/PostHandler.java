/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;

/**
 * Interface for PostHandler.
 * @author mrk
 *
 */
public interface PostHandler {
     /**
     * Called when a field is changed.
     */
    void postPut();
}
