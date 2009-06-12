/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;

import java.util.BitSet;

import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVStructure;

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
         * @see org.epics.pvData.pvCopy.BitSetUtil#compress(java.util.BitSet, org.epics.pvData.pv.PVStructure)
         */
        @Override
        public boolean compress(BitSet bitSet, PVStructure pvStructure) {
            return checkBitSetPVField(pvStructure,bitSet,0);
        }
        
        private boolean checkBitSetPVField(PVField pvField,BitSet bitSet,int initialOffset) {
            boolean atLeastOneBitSet = false;
            boolean allBitsSet = true;
            int offset = initialOffset;
            int nbits = pvField.getNumberFields();
            if(nbits==1) return bitSet.get(offset);
            int nextSetBit = bitSet.nextSetBit(offset);
            if(nextSetBit>=(offset+nbits)) return false;
            if(bitSet.get(offset)) {
                if(nbits>1) bitSet.clear(offset+1, offset+nbits);
                return true;
            }
            PVStructure pvStructure = (PVStructure)pvField;
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
                    offset++;
                    PVStructure pvSubStructure = (PVStructure)pvField;
                    PVField[] pvSubStructureFields = pvSubStructure.getPVFields();
                    for(PVField pvSubSubField: pvSubStructureFields) {
                        boolean result = checkBitSetPVField(pvSubSubField,bitSet,offset);
                        if(result) {
                            atLeastOneBitSet = true;
                            if(!bitSet.get(offset)) {
                                allBitsSet = false;
                            }
                        } else {
                            allBitsSet = false;
                        }
                        offset += pvSubSubField.getNumberFields();
                    }
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
