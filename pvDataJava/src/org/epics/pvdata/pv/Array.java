/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
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
