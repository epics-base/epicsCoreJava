/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.property;


import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;


/**
 * Factory that implements PVProperty.
 * @author mrk
 *
 */
public class PVPropertyFactory {
    private PVPropertyFactory() {} // don't create
    private static PVPropertyImpl pvProperty = new PVPropertyImpl();
    /**
     * Get the interface for PVProperty.
     * There is only one implementation which can be shared by an arbitrary number of users.
     * @return The interface.
     */
    public static PVProperty getPVProperty() {
        return pvProperty;
    }
    
    private static final class PVPropertyImpl implements PVProperty{
        private static boolean isValueField(PVField pvField) {
            PVStructure parent = pvField.getParent();
            if(parent==null) return false;
            PVField[] pvFields = parent.getPVFields();
            String[] fieldNames = parent.getStructure().getFieldNames();
            for(int i=0; i< fieldNames.length; i++) {
                if(pvFields[i]== pvField) {
                    if(fieldNames[i].equals("value")) {
                        return true;
                    }
                }
            }
            return false;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.property.PVProperty#findProperty(org.epics.pvdata.pv.PVField, java.lang.String)
         */
        public PVField findProperty(PVField pvField,String fieldName) {
            if(!isValueField(pvField)) return null;
            PVField pvFound = pvField.getParent().getSubField(fieldName);
            if(pvFound!=null) return pvFound;
            if(fieldName.equals("timeStamp")) {
                return findPropertyViaParent(pvField,fieldName);
            }
            return null;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.property.PVProperty#findPropertyViaParent(org.epics.pvdata.pv.PVField, java.lang.String)
         */
        public PVField findPropertyViaParent(PVField pvf,String propertyName) {
            PVField currentPVField = pvf;
            PVStructure parentPVStructure = currentPVField.getParent();
            while(parentPVStructure!=null) {
                PVField pvField = parentPVStructure.getSubField(propertyName);
                if(pvField!=null) return pvField;
                parentPVStructure = parentPVStructure.getParent();
            }
            return null;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.property.PVProperty#getPropertyNames(org.epics.pvdata.pv.PVField)
         */
        public String[] getPropertyNames(PVField pv) {
            PVField pvField = pv;
            if(!isValueField(pvField)) return null;
            PVStructure pvStructure = pvField.getParent();
            PVField[] pvFields = pvStructure.getPVFields();
            String[] fieldNames = pvStructure.getStructure().getFieldNames();
            int size = 0;
            boolean addTimeStamp = true;
            PVField pvTimeStamp = null;
            for(int i=0; i<pvFields.length; i++) {
                if(fieldNames[i].equals("timeStamp")){
                    pvTimeStamp = pvFields[i];
                    addTimeStamp = false;
                }
                if(fieldNames[i].equals("value")) continue;
                size++;
            }
            if(addTimeStamp) {
                pvTimeStamp = findPropertyViaParent(pv,"timeStamp");
                if(pvTimeStamp!=null) size++;
            }
            if(size==0) return null;
            String[] propertyNames = new String[size];
            int index = 0;
            if(pvTimeStamp!=null) {
                propertyNames[index++] = "timeStamp";
            }
            for(int i=0; i<pvFields.length; i++) {
                if(fieldNames[i].equals("timeStamp")) continue;
                if(fieldNames[i].equals("value")) continue;
                propertyNames[index++] = fieldNames[i];
            }
            return propertyNames;
        }
    }
}
