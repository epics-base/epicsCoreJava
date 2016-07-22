/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

/**
 * Base interface for array field reflection.
 * @author mse
 *
 */
public interface UnionArray extends Array {
    /**
     * Get the union interface for an array element.
     * 
     * @return the interface
     */
    Union getUnion();
}
