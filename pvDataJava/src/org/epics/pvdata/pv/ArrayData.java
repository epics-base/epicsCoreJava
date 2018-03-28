/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;
/**
 * Generic class implementation required by get/put PVArray methods.
 * T must be an array class.
 * @author mrk
 */
public class ArrayData<T> {

    /**
     * The array.
     * PVArray.get sets this value.
     */
    public T data;

    /**
     * The offset.
     * PVArray.get sets this value.
     */
    public int offset;

    /**
     * Generic method that sets data and offset.
     *
     * @param array object representing an array instance
     * @param offset offset within the array
     */
    @SuppressWarnings("unchecked")
    public void set(Object array, int offset)
    {
        this.data = (T)array;
        this.offset = offset;
    }
}
