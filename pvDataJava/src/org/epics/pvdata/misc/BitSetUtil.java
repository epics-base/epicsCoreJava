/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.misc;

import org.epics.pvdata.pv.PVStructure;

/**
 * Utility functions for a BitSet related to a PVStructure.
 * Currently it only has one method.
 * @author mrk
 *
 */
public interface BitSetUtil {
    /**
     * Compress the bits in a BitSet related to a structure.
     * <p>
     * For each structure:
     * 1) If the bit for the structure is set then the bit for all subfields of the structure are cleared.
     * 2) If the bit for the structure is not set but all immediate subfields have their bit set then
     * the bit for the structure is set and the bits for all subfields are cleared.
     * </p>
     * <p>Note that this is a recursive algorithm.</p>
     * <p>Channel Access can call this before sending data. It can then pass entire structures if the structure offset bit is set.</p>
     * @param bitSet The bitSet for pvStructure. 
     * @param pvStructure the PVStructure relative to which the compression is performed
     * @return (false,true) if (no, at least one) bit is set in bitSet.
     */
    boolean compress(BitSet bitSet,PVStructure pvStructure);
}
