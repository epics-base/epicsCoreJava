/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

/**
 * Base interface for array data.
 * Each PVType has an array interface that extends PVArray.
 * @author mrk
 *
 */
public interface PVScalarArray extends PVArray {
    /**
     * Get the Array introspection interface.
     *
     * @return the introspection interface
     */
    ScalarArray getScalarArray();
}
