/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

import org.epics.util.array.ArrayInteger;
import org.epics.util.array.ListNumber;

/**
 * Get/put a int array.
 * The caller must be prepared to get/put the array in chunks.
 * The return argument is always the number of elements that were transfered.
 * It may be less than the number requested.
 * @author mrk
 *
 */
public interface PVIntArray extends PVNumberArray{
    /**
     * Get values from a <i>PVIntArray</i> and put them into <i>int[]to</i>.
     *
     * @param offset the offset to the first element to get
     * @param length the maximum number of elements to transfer
     * @param data the class containing the data and an offset into the data.
     * Get sets these values. The caller must do the actual data transfer.
     * @return the number of elements that can be transfered.
     * This is always less than or equal to length.
     * If the value is less then length then get should be called again.
     * If the return value is greater than 0 then data.data is
     * a reference to the array and data.offset is the offset into the
     * array.
     */
    int get(int offset, int length, IntArrayData data);

    /**
     * Returns an unmodifiable view of the data.
     *
     * @return an unmodifiable view of the data
     */
    ArrayInteger get();

    /**
     * Put values into a <i>PVIntArray</i> from <i>int[]from</i>.
     *
     * @param offset the offset to the first element to put
     * @param length the maximum number of elements to transfer
     * @param from the array from which to get the data
     * @param fromOffset the offset into from
     * @return the number of elements transfered.
     * This is always less than or equal to length.
     * If the value is less than the length then put should be called again.
     * @throws IllegalStateException if the field is not mutable
     */
    int put(int offset, int length, int[] from, int fromOffset);

    /**
     * Share the data from caller.
     * The capacity and length are taken from the array and this array is made immutable.
     * This should only be used to share immutable data.
     *
     * @param from the data to share
     */
    void shareData(int[] from);
}
