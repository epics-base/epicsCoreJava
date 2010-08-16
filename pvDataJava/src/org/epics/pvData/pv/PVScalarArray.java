/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

/**
 * Base interface for array data.
 * Each PVType has an array interface that extends PVArray.
 * @author mrk
 *
 */
public interface PVScalarArray extends PVArray {
    /**
     * Get the Array introspection interface.
     * @return The introspection interface.
     */
    ScalarArray getScalarArray();
}
