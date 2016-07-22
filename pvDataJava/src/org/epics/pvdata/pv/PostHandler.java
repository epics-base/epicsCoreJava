/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
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
