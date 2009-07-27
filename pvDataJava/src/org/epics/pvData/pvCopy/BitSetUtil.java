/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;

import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.PVStructure;

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
     * @param pvStructure
     * @return (false,true) if (no, at least one) bit is set in bitSet.
     */
    boolean compress(BitSet bitSet,PVStructure pvStructure);
}
