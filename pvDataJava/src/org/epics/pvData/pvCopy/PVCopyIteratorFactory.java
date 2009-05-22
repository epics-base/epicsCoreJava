/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;

import java.util.BitSet;

import org.epics.pvData.pv.*;

/**
 * @author mrk
 *
 */
public class PVCopyIteratorFactory {
    public static PVCopyIterator create(PVStructure pvStructure) {
        PVCopyIteratorImpl impl = new PVCopyIteratorImpl(pvStructure);
        impl.init();
        return impl;
    }
    
    private static int addNumFields(PVStructure pvStructure,int num) {
        num++;
        PVField[] pvFields = pvStructure.getPVFields();
        for(PVField pvField : pvFields) {
            if(pvField.getField().getType()==Type.structure) {
                num = addNumFields((PVStructure)pvField,num);
            } else {
                num++;
            }
        }
        return num;
    }
    
    private static int setFields(PVField[] pvFields,int index,PVField pvField) {
        pvFields[index++] = pvField;
        if(pvField.getField().getType()==Type.structure) {
            PVStructure pvStructure = (PVStructure)pvField;
            PVField[] pvs = pvStructure.getPVFields();
            for(PVField pv : pvs) {
                index = setFields(pvFields,index,pv);
            }
        }
        return index;
    }
    
   private static class PVCopyIteratorImpl implements PVCopyIterator {
        
        private PVCopyIteratorImpl(PVStructure pvStructure) {
            this.pvStructure = pvStructure;
        }
        
        private void init() {
            numberFields = addNumFields(pvStructure,0);
            bitSet = new BitSet(numberFields);
            pvFields = new PVField[numberFields];
            setFields(pvFields,0,pvStructure);
        }
        
        
        
        private PVStructure pvStructure;
        private int numberFields = 0;
        private BitSet bitSet = null;
        private int currentOffset = 0;
        private boolean onlyModified = false;
        private PVField currentPVField = null;
        private PVField[] pvFields = null;
        
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopyIterator#getPVStructure()
         */
        public PVStructure getPVStructure() {
            return pvStructure;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopyIterator#clearModified(int)
         */
        public void clearModified(int offset) {
            bitSet.clear(offset);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopyIterator#clearModified()
         */
        public void clearModified() {
            bitSet.clear();
        }
        
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopyIterator#getOffset(org.epics.pvData.pv.PVField)
         */
        public int getOffset(PVField pvField) {
            for(int i=0; i<numberFields; i++) {
                if(pvField==pvFields[i]) return i;
            }
            throw new IllegalStateException("pvField not in pvStructure");
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopyIterator#getPVField(int)
         */
        public PVField getPVField(int offset) {
            return pvFields[offset];
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopyIterator#isModified(int)
         */
        public boolean isModified(int offset) {
            return bitSet.get(offset);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopyIterator#isModified()
         */
        public boolean isModified() {
            return bitSet.isEmpty();
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopyIterator#setModified(int)
         */
        public void setModified(int offset) {
            bitSet.set(offset);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopyIterator#startIterator(boolean)
         */
        public boolean startIterator(boolean onlyModified) {
            if(onlyModified && bitSet.isEmpty()) return false;
            this.onlyModified = onlyModified;
            currentOffset = 0;
            return true;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopyIterator#next()
         */
        public boolean next() {
            while(currentOffset<numberFields) {
                currentOffset++;
                if(!onlyModified) return true;
                if(bitSet.get(currentOffset)) return true;
            }
            return false;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopyIterator#getOffset()
         */
        public int getOffset() {
            return currentOffset;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.PVCopyIterator#getPVField()
         */
        public PVField getPVField() {
            return currentPVField;
        }
       
    }
}
