/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

/**
 * Get/put a double array.
 * The caller must be prepared to get/put the array in chunks.
 * The return argument is always the number of elements that were transfered.
 * It may be less than the number requested.
 * @author mrk
 *
 */
public interface PVDoubleArray extends PVScalarArray{
    /**
     * Get values from a <i>PVDoubleArray</i>
     * and put them into <i>double[]to</i>
     * @param offset The offset to the first element to get.
     * @param length The maximum number of elements to transfer.
     * @param data The class containing the data and an offset into the data.
     * Get sets these values. The caller must do the actual data transfer.
     * @return The number of elements that can be transfered.
     * This is always less than or equal to length.
     * If the value is less then length then get should be called again.
     * If the return value is greater than 0 then data.data is
     * a reference to the array and data.offset is the offset into the
     * array.
     */
    int get(int offset, int length, DoubleArrayData data);
    /**
     * Put values into a <i>PVDoubleArray</i> from <i>double[]from</i>
     * @param offset The offset to the first element to put.
     * @param length The maximum number of elements to transfer.
     * @param from The array from which to get the data.
     * @param fromOffset The offset into from.
     * @return The number of elements transfered.
     * This is always less than or equal to length.
     * If the value is less then length then put should be called again.
     * @throws IllegalStateException if the field is not mutable.
     */
    int put(int offset,int length, double[] from, int fromOffset);
    /**
     * Share the data from caller.
     * The capacity and length are taken from the array and this array is made immutable.
     * This should only be used to share immutable data.
     * @param from The data to share.
     */
    void shareData(double[] from);
}
