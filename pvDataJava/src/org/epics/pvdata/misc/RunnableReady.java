/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.misc;

/**
 * The run method implemented by code that calls ThreadCreate.create;
 * @author mrk
 *
 */
public interface RunnableReady {
    /**
     * The run method.
     *
     * @param threadReady threadReady.ready() must be called when run is ready
     */
    void run(ThreadReady threadReady);
}
