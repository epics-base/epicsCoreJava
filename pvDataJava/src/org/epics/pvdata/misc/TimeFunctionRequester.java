/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.misc;

/**
 * The interface that must be implemented by code that calls TimeFunction.
 * @author mrk
 *
 */
public interface TimeFunctionRequester {
    void function();
}
