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
public interface PVArray extends PVField, SerializableArray {
    /**
     * Get the Array introspection interface.
     *
     * @return the introspection interface
     */
    Array getArray();

    /**
     * Get the current length of the array.
     *
     * @return the current length of the array
     */
    int getLength();

    /**
     * Set the length of the array.
     *
     * @param length the new length of the array
     * @throws IllegalStateException if the field is not mutable
     */
    void setLength(int length);

    /**
     * Get the current capacity of the array,
     * that is, the allocated number of elements.
     *
     * @return the capacity
     */
    int getCapacity();

    /**
     * Set the capacity.
     *
     * @param length the new capacity for the array
     * @throws IllegalStateException if the capacity can't be changed
     */
    void setCapacity(int length);

    /**
     * Can the capacity be changed?
     *
     * @return whether it can be modified
     */
    boolean isCapacityMutable();

    /**
     * Set capacityMutable.
     *
     * @param isMutable the new value for capacityMutable
     */
    void setCapacityMutable(boolean isMutable);
}
