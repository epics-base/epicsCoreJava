/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;

/**
 * Base interface for array field reflection.
 * @author mse
 *
 */
public interface UnionArray extends Field{
    /**
     * Get the union interface for an array element.
     * @return The interface.
     */
    Union getUnion();
}
