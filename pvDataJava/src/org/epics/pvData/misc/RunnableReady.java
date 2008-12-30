/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.misc;

/**
 * The run method implemented by code that calls ThreadCreate.create;
 * @author mrk
 *
 */
public interface RunnableReady {
     /**
      * The run method.
     * @param threadReady threadReady.ready() must be called when run is reasdy.
     */
    void run(ThreadReady threadReady);
}
