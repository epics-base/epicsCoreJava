/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.misc;

/**
 * Interface implemented by ThreadCreate
 * @author mrk
 *
 */
public interface ThreadReady {
    /**
     * Called by RunnableReady.run when it is ready.
     */
    void ready();
}
