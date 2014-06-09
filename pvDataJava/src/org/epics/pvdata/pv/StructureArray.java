/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
