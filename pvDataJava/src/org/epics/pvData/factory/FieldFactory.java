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
            case scalar: return createScalar(fieldName,((Scalar)field).getScalarType());
            case scalarArray:return createArray(fieldName,((Array)field).getElementType());
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
    }
}
