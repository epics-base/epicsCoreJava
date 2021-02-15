/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.misc;

import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;

/**
 *
 * Factory that implements BitSetUtil.
 * It has NO internal state.
 * @author mrk
 *
 */
public class BitSetUtilFactory {
    private BitSetUtilFactory() {} // don't create
    private static CompressBitSetImpl compressBitSet = new CompressBitSetImpl();
    /**
     * Get the interface for BitSetUtil.
     * There is only one implementation which can be shared by an arbitrary number of users.
     * @return The interface.
     */
    public static BitSetUtil getCompressBitSet() {
        return compressBitSet;
    }

    private static final class CompressBitSetImpl implements BitSetUtil{
        /* (non-Javadoc)
         * @see org.epics.pvdata.pvCopy.BitSetUtil#compress(org.epics.pvdata.misc.BitSet, org.epics.pvdata.pv.PVStructure)
         */
        public boolean compress(BitSet bitSet, PVStructure pvStructure) {
            return checkBitSetPVField(pvStructure,bitSet,0);
        }

        private boolean checkBitSetPVField(PVField pvField,BitSet bitSet,int initialOffset) {
            int offset = initialOffset;
            int nbits = pvField.getNumberFields();
            if(nbits==1) return bitSet.get(offset);
            int nextSetBit = bitSet.nextSetBit(offset);
            if(nextSetBit>=(offset+nbits)) return false;
            if(nextSetBit<0) return false;
            if(bitSet.get(offset)) {
                if(nbits>1) bitSet.clear(offset+1, offset+nbits);
                return true;
            }
            boolean atLeastOneBitSet = false;
            boolean allBitsSet = true;
            PVStructure pvStructure = (PVStructure)pvField;
            offset = pvStructure.getFieldOffset() + 1;
            while(offset<initialOffset + nbits) {
                PVField pvSubField = pvStructure.getSubField(offset);
                int nbitsNow = pvSubField.getNumberFields();
                if(nbitsNow==1) {
                    if(bitSet.get(offset)) {
                        atLeastOneBitSet = true;
                    } else {
                        allBitsSet = false;
                    }
                    offset++;
                } else {
                    boolean result = checkBitSetPVField(pvSubField,bitSet,offset);
                    if(result) {
                        atLeastOneBitSet = true;
                        if(!bitSet.get(offset)) {
                            allBitsSet = false;
                        }
                    } else {
                        allBitsSet = false;
                    }
                    offset += pvSubField.getNumberFields();

                }
            }
            if(allBitsSet) {
                if(nbits>1) {
                    bitSet.clear(initialOffset+1, initialOffset+nbits);
                }
                bitSet.set(initialOffset);
            }
            return atLeastOneBitSet;
        }

    }
}
