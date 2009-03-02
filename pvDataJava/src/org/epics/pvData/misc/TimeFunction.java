/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.misc;

/**
 * Time a function call.
 * @author mrk
 *
 */
public interface TimeFunction {
    /**
     * Time the call.
     * TimeFunctionRequester.function is called repeatedly and the average time per call is calculated.
     * @return The average number of seconds the function call required.
     */
    double timeCall();
}
