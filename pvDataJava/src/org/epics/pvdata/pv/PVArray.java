/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
     * Get the current length of the array.
     * @return The current length of the array.
     */
    int getLength();
    /**
     * Set the length of the array.
     * @param length Set the length.
     * @throws IllegalStateException if the field is not mutable.
     */
    void setLength(int length);
    /**
     * Get the current capacity of the array,
     * i.e. the allocated number of elements.
     * @return The capacity.
     */
    int getCapacity();
    /**
     * Set the capacity.
     * @param length The new capacity for the array.
     * @throws IllegalStateException if the capacity can't be changed.
     */
    void setCapacity(int length);
    /**
     * Can the capacity be changed?
     * @return If it can be modified.
     */
    boolean isCapacityMutable();
    /**
     * Set capacityMutable.
     * @param isMutable New value for capacityMutable.
     */
    void setCapacityMutable(boolean isMutable);
}
