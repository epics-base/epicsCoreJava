/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
