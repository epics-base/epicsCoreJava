/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;
import org.epics.pvData.pv.*;

import java.util.*;

/**
 * @author mrk
 *
 */
public class MapToFromRecordFactory {
    
    public static MapToFromRecord create(PVCopy pvCopy,PVStructure pvStructure) {
        MapToFromRecordImpl mapToFromRecord = new MapToFromRecordImpl(pvCopy,pvStructure);
        mapToFromRecord.init();
        return mapToFromRecord;
    }
       
    private static class MapToFromRecordImpl implements MapToFromRecord {
        
        private MapToFromRecordImpl(PVCopy pvCopy,PVStructure pvStructure) {
            this.pvCopy = pvCopy;
            this.pvStructure = pvStructure;
        }

        private PVCopy pvCopy = null;
        private PVStructure pvStructure = null;
        private PVRecord pvRecord = null;
        private PVField[] recordPVFields = null;
        private int[] offsets = null;
        private int nrecordPVFields = 0;
        
        
        private boolean gotRecordPVField = false;
        private int pvRecordFieldsIndex = -1;
        private PVField pvRecordField = null;
        
        private void init() {
            pvRecord = pvCopy.getPVRecord();
            recordPVFields = pvCopy.getRecordPVFields();
            offsets = pvCopy.getStructureOffsets();
            nrecordPVFields = recordPVFields.length;
        }
        
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.MapToFromRecord#getPVStructureOffset(org.epics.pvData.pv.PVField)
         */
        public int getPVStructureOffset(PVField recordPVField) {
            for(int i=0; i<nrecordPVFields; i++) {
                if(recordPVFields[i]==recordPVField) return offsets[i];
            }
            throw new IllegalStateException("recordPVField is not in recordPVFields");
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.MapToFromRecord#getPVStructureOffset(org.epics.pvData.pv.PVStructure, org.epics.pvData.pv.PVField)
         */
        public int getPVStructureOffset(PVStructure recordPVStructure,PVField recordPVField)
        {
            int index = -1;
            PVStructure pvStructure = null;
            for(int i=0; i<nrecordPVFields; i++) {
                if(recordPVFields[i]==recordPVStructure) {
                    pvStructure = (PVStructure)recordPVFields[i];
                    index= i;
                    break;
                }
            }
            if(index<0) {
                throw new IllegalStateException("recordPVStructure is not in recordPVFields");
            }
            int offset = offsets[index];
            gotRecordPVField = false;
            return getStructureOffset(recordPVField,pvStructure,pvStructure,offset);
        }
        private int getStructureOffset(PVField recordPVField,PVStructure pvTop,PVStructure pvStructure,int offset) {
            PVField[] pvFields = pvStructure.getPVFields();
            for(int i=0; i<pvFields.length; i++) {
                PVField pvField = pvFields[i];
                offset++;
                if(pvField==recordPVField) {
                    gotRecordPVField = true;
                    return offset;
                }
                if(pvField.getField().getType()==Type.structure) {
                    offset = getStructureOffset(recordPVField,pvTop,(PVStructure)pvField,offset);
                    if(gotRecordPVField) return offset;
                }
            }
            if(pvStructure==pvTop) {
                throw new IllegalStateException("could not find recordPVField");
            }
            offset++;
            return offset;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.MapToFromRecord#getPVRecordField(int)
         */
        public void getPVRecordField(int offsetInStructure) {
            pvRecordFieldsIndex = -1;
            pvRecordField = null;
            int index = nrecordPVFields-1;
            for(int i=0; i<nrecordPVFields; i++) {
                if(offsetInStructure>=offsets[i]) {
                    index = i;
                    break;
                }
            }
            pvRecordFieldsIndex = index;
            PVStructure pvStructure = (PVStructure)recordPVFields[index];
            getPVRecordField(offsetInStructure,offsets[index],pvStructure,pvStructure);
        }
        private int getPVRecordField(int offsetInStructure,int offset,PVStructure pvStrucvture,PVStructure pvTop) {
            if(offsetInStructure==offset) {
                pvRecordField = pvStructure;
                return offset;
            }
            PVField[] pvFields = pvStructure.getPVFields();
            for(int i=0; i<pvFields.length; i++) {
                PVField pvField = pvFields[i];
                offset++;
                if(offsetInStructure==offset) {
                    pvRecordField = pvField;
                    return offset;
                }
                if(pvField.getField().getType()==Type.structure) {
                    offset = getPVRecordField(offsetInStructure,offset,(PVStructure)pvField,pvTop);
                    if(pvRecordField!=null) return offset;;
                }
            }
            if(pvStructure==pvTop) {
                throw new IllegalStateException("could not find recordPVField");
            }
            offset++;
            return offset;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.MapToFromRecord#getPVFieldsIndex()
         */
        public int getPVFieldsIndex() {
            return pvRecordFieldsIndex;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pvCopy.MapToFromRecord#getPVField(int)
         */
        public PVField getPVField(int offset) {
            return pvRecordField;
        }
    }
}
