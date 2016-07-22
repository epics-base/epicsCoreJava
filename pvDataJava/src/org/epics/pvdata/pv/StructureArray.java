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
public interface StructureArray extends Array {
    /**
     * Get the structure interface for an array element.
     * @return The interface.
     */
    Structure getStructure();
}
