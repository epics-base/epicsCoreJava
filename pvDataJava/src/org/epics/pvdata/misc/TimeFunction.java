/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.misc;

/**
 * Time a function call.
 * @author mrk
 *
 */
public interface TimeFunction {
    /**
     * Time the call.
     * TimeFunctionRequester.function is called repeatedly and the average time per call is calculated.
     *
     * @return the average number of seconds the function call required
     */
    double timeCall();
}
