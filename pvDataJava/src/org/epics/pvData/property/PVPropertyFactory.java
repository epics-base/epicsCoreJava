/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.property;


import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVStructure;


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
        /* (non-Javadoc)
         * @see org.epics.pvData.property.PVProperty#findProperty(org.epics.pvData.pv.PVField, java.lang.String)
         */
        public PVField findProperty(PVField pvField,String fieldName) {
            Field field = pvField.getField();
            if(!field.getFieldName().equals("value")) return null;
            if(fieldName==null || fieldName.length()==0) return null;
            PVStructure pvParent = pvField.getParent();
            PVField pvFound = pvParent.getSubField(fieldName);
            if(pvFound!=null) return pvFound;
            if(fieldName.equals("timeStamp")) {
                return findPropertyViaParent(pvField,fieldName);
            }
            return null;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.PVProperty#findPropertyViaParent(org.epics.pvData.pv.PVField, java.lang.String)
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
         * @see org.epics.pvData.property.PVProperty#getPropertyNames(org.epics.pvData.pv.PVField)
         */
        public String[] getPropertyNames(PVField pv) {
            PVField pvField = pv;
            Field field = pvField.getField();
            if(!field.getFieldName().equals("value"))  return null;
            if(pvField.getParent()==null) return null;
            pvField = pvField.getParent();
            PVStructure pvStructure = (PVStructure)pvField;
            PVField[] pvFields = pvStructure.getPVFields();
            int size = 0;
            boolean addTimeStamp = true;
            for(PVField pvf: pvFields) {
                field = pvf.getField();
                String fieldName = field.getFieldName();
                if(fieldName.equals("timeStamp")) addTimeStamp = false;
                if(fieldName.equals("value")) continue;
                size++;
            }
            PVField pvTimeStamp = null;
            if(addTimeStamp) {
                pvTimeStamp = findPropertyViaParent(pv,"timeStamp");
                if(pvTimeStamp!=null) size++;
            }
            if(size==0) return null;
            String[] propertyNames = new String[size];
            int index = 0;
            if(pvTimeStamp!=null) {
                propertyNames[index++] = pvTimeStamp.getField().getFieldName();
            }
            for(PVField pvf: pvFields) {
                field = pvf.getField();
                String fieldName = field.getFieldName();
                if(fieldName.equals("value")) continue;
                propertyNames[index++] = pvf.getField().getFieldName();
            }
            return propertyNames;
        }
    }
}
