/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.util.ArrayList;

import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVListener;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVRecordField;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Type;

/**
 * @author mrk
 *
 */
public class BasePVRecordField implements PVRecordField{
	 private PVField pvField = null;
     private PVRecord pvRecord = null;
     private boolean isStructure = false;
     // since list is mostly traversed an ArrayList is efficent.
     private ArrayList<PVListener> pvListenerList = new ArrayList<PVListener>(0);
     
     BasePVRecordField(PVField pvField,PVRecord pvRecord) {
         this.pvField = pvField;
         this.pvRecord = pvRecord;
         if(pvField.getField().getType()==Type.structure) isStructure = true;
     }
     /* (non-Javadoc)
      * @see org.epics.pvData.pv.PVField#getPVRecord()
      */
     @Override
     public PVRecord getPVRecord() {
        return pvRecord;
     }
     /* (non-Javadoc)
      * @see org.epics.pvData.pv.PVField#addListener(org.epics.pvData.pv.PVListener)
      */
     @Override
     public boolean addListener(PVListener pvListener) {
         if(!pvRecord.isRegisteredListener(pvListener)) return false;
         return pvListenerList.add(pvListener);
     }
     /* (non-Javadoc)
      * @see org.epics.pvData.pv.PVRecordField#removeListener(org.epics.pvData.pv.PVListener)
      */
     @Override
     public void removeListener(PVListener pvListener) {
         pvListenerList.remove(pvListener);
         if(isStructure) {
             PVStructure pvStructure = (PVStructure)pvField;
             PVField[] pvFields = pvStructure.getPVFields();
             for(int i=0; i<pvFields.length; i++) {
                 PVRecordField pvRecordField = (BasePVRecordField)pvFields[i].getPVRecordField();
                 pvRecordField.removeListener(pvListener);
             }
         }
     }
     
     /* (non-Javadoc)
      * @see org.epics.pvData.pv.PVField#postPut()
      */
     @Override
     public void postPut() {
         if(pvField.getNextFieldOffset()==0) return; // setOffsets has never been called.
         callListener();
         PVStructure pvParent = pvField.getParent();
         if(pvParent!=null) {
             BasePVRecordField pvRecordField = (BasePVRecordField)pvParent.getPVRecordField();
             pvRecordField.postParent(pvField);
         }
         if(isStructure) {
             PVStructure pvStructure = (PVStructure)pvField;
             PVField[] pvFields = pvStructure.getPVFields();
             for(int i=0; i<pvFields.length; i++) {
                 BasePVRecordField pvRecordField = (BasePVRecordField)pvFields[i].getPVRecordField();
                 postSubField(pvRecordField);
             }
         }
     }
     
     private void postParent(PVField subField) {
         for(int index=0; index<pvListenerList.size(); index++) {
             PVListener pvListener = pvListenerList.get(index);
             if(pvListener!=null) pvListener.dataPut((PVStructure)pvField,subField);
         }
         PVStructure pvParent = pvField.getParent();
         if(pvParent!=null) {
             BasePVRecordField pvRecordField = (BasePVRecordField)pvParent.getPVRecordField();
             pvRecordField.postParent(subField);
         }
     }
     
     private void postSubField(BasePVRecordField pvRecordField) {
         pvRecordField.callListener();
         if(pvRecordField.pvField.getField().getType()==Type.structure) {
             PVStructure pvStructure = (PVStructure)pvRecordField.pvField;
             PVField[] pvFields = pvStructure.getPVFields();
             for(int i=0; i<pvFields.length; i++) {
                 BasePVRecordField nextPVRecordField = (BasePVRecordField)pvFields[i].getPVRecordField();
                 postSubField(nextPVRecordField);
             }
         }
     }
     
     private void callListener() {
         // don't create iterator
         for(int index=0; index<pvListenerList.size(); index++) {
             PVListener pvListener = pvListenerList.get(index);
             if(pvListener!=null) pvListener.dataPut(pvField);
         }
     }
}
