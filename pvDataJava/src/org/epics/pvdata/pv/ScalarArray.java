/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

/**
 * Base interface for array field reflection.
 * @author mrk
 *
 */
public interface ScalarArray extends Array {
    /**
     * Get the element type for the array.
     * @return The element ScalarType, non-<code>null</code>.
     */
    ScalarType getElementType();
}
