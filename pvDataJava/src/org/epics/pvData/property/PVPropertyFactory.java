package org.epics.pvData.property;


import java.util.regex.Pattern;

import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Type;


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
        private static  Pattern periodPattern = Pattern.compile("[.]");
        /* (non-Javadoc)
         * @see org.epics.pvData.property.PVProperty#findProperty(org.epics.pvData.pv.PVField, java.lang.String)
         */
        public PVField findProperty(PVField pvField,String fieldName) {
            if(fieldName==null || fieldName.length()==0) return null;
            String[] names = periodPattern.split(fieldName,2);
            if(names.length<=0) {
                return null;
            }
            PVField currentField = pvField;
            while(true) {
                String name = names[0];
                currentField = findField(currentField,name);
                if(currentField==null) break;;
                if(names.length<=1) break;
                names = periodPattern.split(names[1],2);
            }
            if(currentField==null) {
                if(fieldName.equals("timeStamp")) {
                    return findPropertyViaParent(pvField,fieldName);
                }
                String name = pvField.getField().getFieldName(); 
                if(name.equals("value")) {
                    return findProperty(pvField.getParent(),fieldName);
                }
            }
            return currentField;
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
            boolean skipValue = false;
            Field field = pvField.getField();
            if(field.getFieldName().equals("value")) {
                if(pvField.getParent()!=null) {
                    pvField = pvField.getParent();
                    skipValue = true;
                }
            }
            field = pvField.getField();
            if(field.getType()!=Type.structure) return null;
            PVStructure pvStructure = (PVStructure)pvField;
            PVField[] pvFields = pvStructure.getPVFields();
            int size = 0;
            boolean addTimeStamp = true;
            for(PVField pvf: pvFields) {
                field = pvf.getField();
                String fieldName = field.getFieldName();
                if(fieldName.equals("timeStamp")) addTimeStamp = false;
                if(skipValue && fieldName.equals("value")) continue;
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
                if(skipValue && fieldName.equals("value")) continue;
                propertyNames[index++] = pvf.getField().getFieldName();
            }
            return propertyNames;
        }
        
        private PVField findField(PVField pvField,String name) {
            if(pvField==null) return null;
            if(pvField.getField().getType()!=Type.structure) return null;
            PVStructure pvStructure = (PVStructure)pvField;
            return pvStructure.getSubField(name);
        }
    }
}
