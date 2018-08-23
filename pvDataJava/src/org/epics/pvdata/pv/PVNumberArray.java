/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

import org.epics.util.array.ListNumber;

/**
 * Get/put a numeric array array.
 * The caller must be prepared to get/put the array in chunks.
 * The return argument is always the number of elements that were transfered.
 * It may be less than the number requested.
 *
 */
public interface PVNumberArray extends PVScalarArray {
    
    /**
     * Returns an unmodifiable view of the data.
     * 
     * @return an unmodifiable view of the data
     */
    ListNumber get();
    
    /**
     * Puts the new value contained in the list starting from the offset.
     * 
     * @param offset the first element to be changed
     * @param list the values to be copied
     */
    void put(int offset, ListNumber list);
}
