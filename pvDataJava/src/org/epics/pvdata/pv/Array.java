/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;

/**
 * Reflection interface for a array field.
 * @author mse
 */
public interface Array extends Field {

    enum ArraySizeType { variable, fixed, bounded };
    
    /**
     * Get array size type (i.e. variable/fixed/bounded size array).
     *
     * @return array size type enum
     */
    ArraySizeType getArraySizeType();
    
    /**
     * Get maximum capacity of the array.
     * 
     * @return maximum capacity of the array, 0 indicates variable size array
     */
    int getMaximumCapacity();
}
