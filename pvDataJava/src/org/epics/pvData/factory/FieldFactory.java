/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import org.epics.pvData.pv.Array;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.StructureArray;
import org.epics.pvData.pv.StructureScalar;

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
    private static FieldCreateImpl fieldCreate = new FieldCreateImpl(); 
    /**
     * Get the FieldCreate interface.
     * @return The interface for creating introspection objects.
     */
    public static FieldCreate getFieldCreate() {
        return fieldCreate;
    }
    
    private static final class FieldCreateImpl implements FieldCreate{
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#create(java.lang.String, org.epics.pvData.pv.Field)
         */
        @Override
        public Field create(String fieldName, Field field) {
            switch(field.getType()) {
            case scalar: {
            	Scalar scalar = (Scalar)field;
            	if(scalar.getScalarType()!=ScalarType.pvStructure) {
            		return createScalar(fieldName,scalar.getScalarType());
            	}
            	StructureScalar structureScalar = (StructureScalar)scalar;
            	return createStructureScalar(fieldName,structureScalar.getStructure());
            }
            case scalarArray:{
            	Array array = (Array)field;
            	if(array.getElementType()!=ScalarType.pvStructure) {
            	    return createArray(fieldName,array.getElementType());
            	}
            	StructureArray structureArray = (StructureArray) array;
            	return createStructureArray(fieldName,structureArray.getStructure());
            }
            case structure: return createStructure(fieldName,((Structure)field).getFields());
            }
            throw new IllegalStateException("Logic error. Should never get here");
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#createArray(java.lang.String, org.epics.pvData.pv.ScalarType)
         */
        public Array createArray(String fieldName, ScalarType elementType)
        {
            return new BaseArray(fieldName,elementType);
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
         * @see org.epics.pvData.pv.FieldCreate#createStructureScalar(java.lang.String, org.epics.pvData.pv.Structure)
         */
        @Override
		public StructureScalar createStructureScalar(String fieldName, Structure structure) {
			return new BaseStructureScalar(fieldName,structure);
		}
		/* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#createStructure(java.lang.String, org.epics.pvData.pv.Field[])
         */
        public Structure createStructure(String fieldName, Field[] field)
        {
            return new BaseStructure(fieldName,field);
        }    
    }
}
