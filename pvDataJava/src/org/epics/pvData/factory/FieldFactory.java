/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

import org.epics.pvData.misc.SerializeHelper;
import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarArray;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.StructureArray;
import org.epics.pvData.pv.Type;

/**
 * FieldFactory creates Field instances.
 * User code creates introspection objects via FieldCreate,
 * which is obtained via a call to <i>FieldFactory.getFieldCreate</i>.
 * This is a complete factory for the <i>PV</i> reflection.
 * Most <i>PV</i> database implementations should find this sufficient for
 * <i>PV</i> reflection.
 * @author mrk
 *
 */
public final class FieldFactory {   
    private FieldFactory(){} // don't create
    /**
     * Get the FieldCreate interface.
     * @return The interface for creating introspection objects.
     */
    public static FieldCreate getFieldCreate() {
        return FieldCreateImpl.getFieldCreate();
    }
    
    private static final class FieldCreateImpl implements FieldCreate{
    	private static FieldCreateImpl singleImplementation = null;
        private static synchronized FieldCreateImpl getFieldCreate() {
                if (singleImplementation==null) {
                    singleImplementation = new FieldCreateImpl();
                }
                return singleImplementation;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#create(java.lang.String, org.epics.pvData.pv.Field)
         */
        @Override
        public Field create(Field field) {
        	switch(field.getType()) {
        	case scalar: {
        		Scalar scalar = (Scalar)field;
        		return createScalar(scalar.getScalarType());
        	}
        	case scalarArray:{
        		ScalarArray array = (ScalarArray)field;
        		return createScalarArray(array.getElementType());
        	}
        	case structure: {
        		throw new IllegalArgumentException("can not create a structure without fieldNames");
        	}
        	case structureArray: {
        		StructureArray structureArray = (StructureArray)field;
        		return createStructureArray(structureArray.getStructure());
        	}
        	}
        	throw new IllegalStateException("Logic error. Should never get here");
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#createArray(java.lang.String, org.epics.pvData.pv.ScalarType)
         */
        public ScalarArray createScalarArray(ScalarType elementType)
        {
            return new BaseScalarArray(elementType);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#createArray(java.lang.String, org.epics.pvData.pv.Structure)
         */
        @Override
		public StructureArray createStructureArray(Structure elementStructure) {
        	
			return new BaseStructureArray(elementStructure);
		}
		/* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#createScalar(java.lang.String, org.epics.pvData.pv.ScalarType)
         */
        public Scalar createScalar(ScalarType type)
        {
            return new BaseScalar(type);
        }
		/* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#createStructure(java.lang.String, org.epics.pvData.pv.Field[])
         */
        public Structure createStructure(String[] fieldNames, Field[] fields)
        {
            if(fieldNames.length != fields.length) {
                throw new IllegalArgumentException("fieldNames and fields have different length");
            }
            return new BaseStructure(fieldNames,fields);
        }
		@Override
        public Structure appendField(Structure structure, String fieldName, Field field) {
		    String[] oldNames = structure.getFieldNames();
		    Field[] oldFields = structure.getFields();
		    int oldlen = oldNames.length;
		    String[] newNames = new String[oldlen+1];
		    Field[] newFields = new Field[oldlen+1];
		    for(int i=0; i<oldlen; i++) {
		        newNames[i] = oldNames[i];
		        newFields[i] = oldFields[i];
		    }
		    newNames[oldlen] = fieldName;
		    newFields[oldlen] = field;
            return createStructure(newNames,newFields);
            
        }
        @Override
        public Structure appendFields(Structure structure, String[] fieldNames,Field[] fields) {
            String[] oldNames = structure.getFieldNames();
            Field[] oldFields = structure.getFields();
            int oldlen = oldNames.length;
            int newlen = oldlen + fieldNames.length;
            String[] newNames = new String[newlen];
            Field[] newFields = new Field[newlen];
            for(int i=0; i<oldlen; i++) {
                newNames[i] = oldNames[i];
                newFields[i] = oldFields[i];
            }
            for(int i=0; i<newlen; i++) {
                newNames[i+oldlen] = fieldNames[i];
                newFields[i+oldlen] = oldFields[i];
            }
            return createStructure(newNames,newFields);
        }
        /* (non-Javadoc)
		 * @see org.epics.pvData.pv.FieldCreate#deserialize(java.nio.ByteBuffer, org.epics.pvData.pv.DeserializableControl)
		 */
		@Override
		public Field deserialize(ByteBuffer buffer, DeserializableControl control) {
			return null;
			// MATEJ 
//			control.ensureData(1);
//			final byte typeCode = buffer.get();
//
//			// high nibble means scalar/array/structure
//			final Type type = Type.values()[typeCode >>> 4]; 
//			switch (type)
//			{
//				case scalar:
//					final ScalarType scalar = ScalarType.values()[typeCode & 0x0F];
//					final String scalarFieldName = SerializeHelper.deserializeString(buffer, control);
//					return new BaseScalar(scalarFieldName, scalar);
//					
//				case scalarArray:
//					final ScalarType element = ScalarType.values()[typeCode & 0x0F];
//					final String arrayFieldName = SerializeHelper.deserializeString(buffer, control);
//					return new BaseScalarArray(arrayFieldName, element);
//					
//				case structure:
//					return BaseStructure.deserializeStructureField(buffer, control);
//
//				case structureArray:
//					final String structureArrayFieldName = SerializeHelper.deserializeString(buffer, control);
//					final Structure arrayElement = BaseStructure.deserializeStructureField(buffer, control);
//					return new BaseStructureArray(structureArrayFieldName, arrayElement);
//
//				default:
//					throw new UnsupportedOperationException("unsupported type: " + type);
//			}
		}  
        
        
    }
}
