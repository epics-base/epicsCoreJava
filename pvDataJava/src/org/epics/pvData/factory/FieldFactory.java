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
        public Field create(String fieldName, Field field) {
        	switch(field.getType()) {
        	case scalar: {
        		Scalar scalar = (Scalar)field;
        		return createScalar(fieldName,scalar.getScalarType());
        	}
        	case scalarArray:{
        		ScalarArray array = (ScalarArray)field;
        		return createScalarArray(fieldName,array.getElementType());
        	}
        	case structure: {
        		Structure structure = (Structure)field;
        		return createStructure(fieldName,structure.getFields());
        	}
        	case structureArray: {
        		StructureArray structureArray = (StructureArray)field;
        		return createStructureArray(fieldName,structureArray.getStructure());
        	}
        	}
        	throw new IllegalStateException("Logic error. Should never get here");
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#createArray(java.lang.String, org.epics.pvData.pv.ScalarType)
         */
        public ScalarArray createScalarArray(String fieldName, ScalarType elementType)
        {
            return new BaseScalarArray(fieldName,elementType);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#createArray(java.lang.String, org.epics.pvData.pv.Structure)
         */
        @Override
		public StructureArray createStructureArray(String fieldName, Structure elementStructure) {
        	
			return new BaseStructureArray(fieldName,elementStructure);
		}
		/* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#createScalar(java.lang.String, org.epics.pvData.pv.ScalarType)
         */
        public Scalar createScalar(String fieldName, ScalarType type)
        {
            return new BaseScalar(fieldName,type);
        }
		/* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#createStructure(java.lang.String, org.epics.pvData.pv.Field[])
         */
        public Structure createStructure(String fieldName, Field[] field)
        {
            return new BaseStructure(fieldName,field);
        }
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.FieldCreate#deserialize(java.nio.ByteBuffer, org.epics.pvData.pv.DeserializableControl)
		 */
		@Override
		public Field deserialize(ByteBuffer buffer, DeserializableControl control) {
			control.ensureData(1);
			final byte typeCode = buffer.get();

			// high nibble means scalar/array/structure
			final Type type = Type.values()[typeCode >>> 4]; 
			switch (type)
			{
				case scalar:
					final ScalarType scalar = ScalarType.values()[typeCode & 0x0F];
					final String scalarFieldName = SerializeHelper.deserializeString(buffer, control);
					return new BaseScalar(scalarFieldName, scalar);
					
				case scalarArray:
					final ScalarType element = ScalarType.values()[typeCode & 0x0F];
					final String arrayFieldName = SerializeHelper.deserializeString(buffer, control);
					return new BaseScalarArray(arrayFieldName, element);
					
				case structure:
					return BaseStructure.deserializeStructureField(buffer, control);

				case structureArray:
					final String structureArrayFieldName = SerializeHelper.deserializeString(buffer, control);
					final Structure arrayElement = BaseStructure.deserializeStructureField(buffer, control);
					return new BaseStructureArray(structureArrayFieldName, arrayElement);

				default:
					throw new UnsupportedOperationException("unsupported type: " + type);
			}
		}  
        
        
    }
}
