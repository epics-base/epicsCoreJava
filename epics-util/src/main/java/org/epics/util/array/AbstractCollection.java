/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * Helper methods for all collections.
 *
 * @author George McIntyre. 15-Feb-2021, SLAC
 */
public abstract class AbstractCollection {
    /**
     * Check whether we can store something at the specified index in the array
     *
     * @param index           the index to check
     * @param readOnly        whether the array is readOnly - if so then throw {@link UnsupportedOperationException}
     * @param checkBoundaries do we really want to check boundaries, if not then we don't check for non-read-only arrays
     * @param size            the size of the array
     * @throws UnsupportedOperationException when the array is read only
     * @throws IndexOutOfBoundsException     when the index is not within bounds and {@code checkBoundaries} is false
     */
    void checkBounds(int index, boolean readOnly, boolean checkBoundaries, int size) {
        if (!readOnly) {
            if (checkBoundaries) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
            }
        } else {
            throw new UnsupportedOperationException("Read only list.");
        }
    }
}
