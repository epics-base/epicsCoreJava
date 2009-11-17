/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.util.Map;
import java.util.Set;

import org.epics.pvData.pv.Array;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVAuxInfo;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVDatabase;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.Type;



/**
 * Factory to create default implementations for PVField objects.
 * The PVField instances are created via interface PVDataCreate,
 * which is obtained via a call to <i>PVDataCreateFactory.getPVDataCreate</i>.
 * @author mrk
 *
 */
public class PVDataFactory {
    private PVDataFactory() {} // don't create
    private static PVDataCreateImpl pvdataCreate = new PVDataCreateImpl();
    /**
     * Get the interface for PVDataCreate.
     * @return The interface.
     */
    public static PVDataCreate getPVDataCreate() {
        return pvdataCreate;
    }
    
    private static final class PVDataCreateImpl implements PVDataCreate{
        private static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        private static Convert convert = ConvertFactory.getConvert();
        
        
        /* (non-Javadoc)
		 * @see org.epics.pvData.pv.PVDataCreate#createPVField(org.epics.pvData.pv.PVStructure, org.epics.pvData.pv.Field)
		 */
        @Override
		public PVField createPVField(PVStructure parent, Field field) {
			switch(field.getType()) {
			case scalar: 	  return createPVScalar(parent, (Scalar)field); 
			case scalarArray: return createPVArray(parent, (Array)field); 
			case structure:   return new BasePVStructure(parent,(Structure)field);
			}
            throw new IllegalArgumentException("Illegal Type");
		}
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVField(org.epics.pvData.pv.PVStructure, java.lang.String, org.epics.pvData.pv.PVField)
         */
        @Override
        public PVField createPVField(PVStructure parent, String fieldName,PVField fieldToClone) {
            switch(fieldToClone.getField().getType()) {
            case scalar:
                return createPVScalar(parent,fieldName,(PVScalar)fieldToClone);
            case scalarArray:
                return createPVArray(parent,fieldName,(PVArray)fieldToClone);
            case structure:
                return createPVStructure(parent,fieldName,(PVStructure)fieldToClone);
            }
            throw new IllegalArgumentException(
            "Logic error in PVDataFactory");
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVScalar(org.epics.pvData.pv.PVStructure, org.epics.pvData.pv.Scalar)
         */
        @Override
        public PVScalar createPVScalar(PVStructure parent, Scalar scalar)
		{
           switch(scalar.getScalarType()) {
            case pvBoolean: return new BasePVBoolean(parent,scalar);
            case pvByte:    return new BasePVByte(parent,scalar);
            case pvShort:   return new BasePVShort(parent,scalar);
            case pvInt:     return new BasePVInt(parent,scalar);
            case pvLong:    return new BasePVLong(parent,scalar);
            case pvFloat:   return new BasePVFloat(parent,scalar);
            case pvDouble:  return new BasePVDouble(parent,scalar);
            case pvString:  return new BasePVString(parent,scalar);
            }
            throw new IllegalArgumentException(
                "Illegal Type. Must be pvBoolean,...,pvStructure");
	 	}
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVScalar(org.epics.pvData.pv.PVStructure, java.lang.String, org.epics.pvData.pv.ScalarType)
         */
        @Override
        public PVScalar createPVScalar(PVStructure parent,String fieldName,ScalarType scalarType)
        {
            return createPVScalar(parent, fieldCreate.createScalar(fieldName, scalarType));
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVScalar(org.epics.pvData.pv.PVStructure, java.lang.String, org.epics.pvData.pv.PVScalar)
         */
        @Override
        public PVScalar createPVScalar(PVStructure parent, String fieldName,PVScalar scalarToClone) {
            PVScalar pvScalar = createPVScalar(parent,fieldName,scalarToClone.getScalar().getScalarType());
            convert.copyScalar(scalarToClone, pvScalar);
            Map<String,PVScalar> attributes = scalarToClone.getPVAuxInfo().getInfos();
            PVAuxInfo pvAttribute = pvScalar.getPVAuxInfo();
            Set<Map.Entry<String, PVScalar>> set = attributes.entrySet();
            for(Map.Entry<String,PVScalar> entry : set) {
                String key = entry.getKey();
                PVScalar fromAttribute = attributes.get(key);
                PVScalar toAttribute = pvAttribute.createInfo(key, fromAttribute.getScalar().getScalarType());
                convert.copyScalar(fromAttribute, toAttribute);
            }
            return pvScalar;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVArray(org.epics.pvData.pv.PVStructure, java.lang.String, org.epics.pvData.pv.ScalarType)
         */
        @Override
        public PVArray createPVArray(PVStructure parent,Array array)
        {
        	switch(array.getElementType()) {
            case pvBoolean: return new BasePVBooleanArray(parent,array);
            case pvByte:    return new BasePVByteArray(parent,array);
            case pvShort:   return new BasePVShortArray(parent,array);
            case pvInt:     return new BasePVIntArray(parent,array);
            case pvLong:    return new BasePVLongArray(parent,array);
            case pvFloat:   return new BasePVFloatArray(parent,array);
            case pvDouble:  return new BasePVDoubleArray(parent,array);
            case pvString:  return new BasePVStringArray(parent,array);
            }
            throw new IllegalArgumentException("Illegal Type. Logic error");
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVArray(org.epics.pvData.pv.PVStructure, java.lang.String, org.epics.pvData.pv.ScalarType)
         */
        @Override
        public PVArray createPVArray(PVStructure parent,String fieldName,ScalarType scalarType)
        {
        	return createPVArray(parent, fieldCreate.createArray(fieldName, scalarType));
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVArray(org.epics.pvData.pv.PVStructure, java.lang.String, org.epics.pvData.pv.PVArray)
         */
        @Override
        public PVArray createPVArray(PVStructure parent, String fieldName,PVArray arrayToClone) {
            PVArray pvArray = createPVArray(parent,fieldName,arrayToClone.getArray().getElementType());
            convert.copyArray(arrayToClone,0, pvArray,0,arrayToClone.getLength());
            Map<String,PVScalar> attributes = arrayToClone.getPVAuxInfo().getInfos();
            PVAuxInfo pvAttribute = pvArray.getPVAuxInfo();
            Set<Map.Entry<String, PVScalar>> set = attributes.entrySet();
            for(Map.Entry<String,PVScalar> entry : set) {
                String key = entry.getKey();
                PVScalar fromAttribute = attributes.get(key);
                PVScalar toAttribute = pvAttribute.createInfo(key, fromAttribute.getScalar().getScalarType());
                convert.copyScalar(fromAttribute, toAttribute);
            }
            return pvArray;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVStructure(org.epics.pvData.pv.PVStructure, org.epics.pvData.pv.Structure)
         */
        @Override
        public PVStructure createPVStructure(PVStructure parent,Structure structure)
        {
            return new BasePVStructure(parent,structure);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVStructure(org.epics.pvData.pv.PVStructure, java.lang.String, org.epics.pvData.pv.Field[])
         */
        @Override
        public PVStructure createPVStructure(PVStructure parent,String fieldName,Field[] fields) {
        	Structure structure = fieldCreate.createStructure(fieldName, fields);
            return new BasePVStructure(parent,structure);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVStructure(org.epics.pvData.pv.PVStructure, java.lang.String, org.epics.pvData.pv.PVStructure)
         */
        @Override
        public PVStructure createPVStructure(PVStructure parent,String fieldName,PVStructure structToClone)
        {
        	Field[] fields = null;
        	if(structToClone==null) {
        	    fields = new Field[0];	
        	} else {
        	    fields = structToClone.getStructure().getFields();
        	}
        	Structure structure = fieldCreate.createStructure(fieldName,fields);
        	PVStructure pvStructure = new BasePVStructure(parent,structure);
        	if(structToClone!=null) copyStructure(structToClone,pvStructure);
        	return pvStructure;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVStructure(org.epics.pvData.pv.PVStructure, java.lang.String, org.epics.pvData.pv.PVDatabase, java.lang.String)
         */
        @Override
        public PVStructure createPVStructure(PVStructure parent,String fieldName,PVDatabase pvDatabase,String structureName)
        {
        	PVStructure pvSource = pvDatabase.findStructure(structureName);
        	if(pvSource==null) {
        		pvDatabase.message("clonePVStructure structureName " + structureName + " not found",
        				MessageType.error);
        		return null;
        	}
        	return createPVStructure(parent,fieldName,pvSource);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVRecord(java.lang.String, org.epics.pvData.pv.Field[])
         */
        @Override
        public PVRecord createPVRecord(String recordName,Field[] fields) {
            Structure structure = fieldCreate.createStructure("", fields);
            PVRecord pvRecord = new BasePVRecord(recordName,structure);
            return pvRecord;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVRecord(java.lang.String, org.epics.pvData.pv.PVStructure)
         */
        @Override
        public PVRecord createPVRecord(String recordName,PVStructure structToClone) {
        	Field[] fields = null;
        	if(structToClone==null) {
        		fields = new Field[0];
        	} else {
        	    fields  = structToClone.getStructure().getFields();
        	}
        	Structure structure = fieldCreate.createStructure("", fields);
            PVRecord pvRecord = new BasePVRecord(recordName,structure);
            if(structToClone!=null) copyStructure(structToClone,pvRecord.getPVStructure());
            return pvRecord;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#createPVRecord(java.lang.String, org.epics.pvData.pv.PVDatabase, java.lang.String)
         */
        @Override
        public PVRecord createPVRecord(String recordName,PVDatabase pvDatabase,String structureName) {
        	PVStructure structToClone = pvDatabase.findStructure(structureName);
        	if(structToClone==null) {
        		pvDatabase.message("createPVRecord structureName " + structureName + " not found",
        				MessageType.error);
        		return null;
        	}
        	return createPVRecord(recordName,structToClone);
        }
        
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.PVDataCreate#flattenPVStructure(org.epics.pvData.pv.PVStructure)
         */
        @Override
        public PVField[] flattenPVStructure(PVStructure pvStructure) {
            Flatten temp = new Flatten(pvStructure);
            temp.initStructure(pvStructure);
            return temp.pvFields;
        }
        
        private static class Flatten {
            
            private Flatten(PVStructure pvStructure) {
                pvFields = new PVField[pvStructure.getNumberFields()];
            }
            
            private void initStructure(PVStructure pvStructure) {
                this.pvFields[currentIndex++] = pvStructure;
                PVField[] pvStructureFields = pvStructure.getPVFields();
                for(PVField pvField : pvStructureFields) {
                    if(pvField.getField().getType()==Type.structure) {
                        initStructure((PVStructure)pvField);
                    } else {
                        this.pvFields[currentIndex++] = pvField;
                    }
                }
            }
            private PVField[] pvFields = null;
            private int currentIndex = 0;
        }
        
        private void copyStructure(PVStructure from,PVStructure to)  {
            Map<String,PVScalar> attributes = from.getPVAuxInfo().getInfos();
            PVAuxInfo pvAttribute = to.getPVAuxInfo();
            Set<Map.Entry<String, PVScalar>> set = attributes.entrySet();
            for(Map.Entry<String,PVScalar> entry : set) {
                String key = entry.getKey();
                PVScalar fromAttribute = attributes.get(key);
                PVScalar toAttribute = pvAttribute.createInfo(key, fromAttribute.getScalar().getScalarType());
                convert.copyScalar(fromAttribute, toAttribute);
            }
            PVField[] fromFields = from.getPVFields();
            PVField[] toFields = to.getPVFields();
            for(int i=0; i<fromFields.length; i++) {
            	PVField fromPV = fromFields[i];
            	PVField toPV = toFields[i];
            	Type type = fromPV.getField().getType();
            	if(type==Type.structure) {
            		copyStructure((PVStructure)fromPV,(PVStructure)toPV);
            		continue;
            	}
            	attributes = fromPV.getPVAuxInfo().getInfos();
            	pvAttribute = toPV.getPVAuxInfo();
            	set = attributes.entrySet();
                for(Map.Entry<String,PVScalar> entry : set) {
                    String key = entry.getKey();
                    PVScalar fromAttribute = attributes.get(key);
                    PVScalar toAttribute = pvAttribute.createInfo(key, fromAttribute.getScalar().getScalarType());
                    convert.copyScalar(fromAttribute, toAttribute);
                }
                if(type==Type.scalar) {
                	convert.copyScalar((PVScalar)fromPV, (PVScalar)toPV);
                } else {
                	PVArray fromPVArray = (PVArray)fromPV;
                	PVArray toPVArray = (PVArray)toPV;
                	convert.copyArray(fromPVArray,0,toPVArray, 0, fromPVArray.getLength());
                }
            }
        }
        
    }
}
