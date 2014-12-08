/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
