/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.util.Formatter;

import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVScalarType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStructureArray;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.PVUnionArray;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.StructureArray;
import org.epics.pvdata.pv.Type;
import org.epics.pvdata.pv.Union;
import org.epics.pvdata.pv.UnionArray;

/**
 * Factory to create default implementations for PVField objects.
 * The PVField instances are created via interface PVDataCreate,
 * which is obtained via a call to <i>PVDataCreateFactory.getPVDataCreate</i>.
 * @author mrk
 *
 */
public class PVDataFactory {
    private PVDataFactory() {} // don't create
    /**
     * Get the interface for PVDataCreate.
     * @return The interface.
     */
    public static synchronized PVDataCreate getPVDataCreate() {
        return PVDataCreateImpl.getPVDataCreate();
    }

    private static final class PVDataCreateImpl implements PVDataCreate{
        private static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        private static Convert convert = ConvertFactory.getConvert();
        private static PVDataCreateImpl singleImplementation = null;
        private static synchronized PVDataCreateImpl getPVDataCreate() {
                if (singleImplementation==null) {
                    singleImplementation = new PVDataCreateImpl();
                }
                return singleImplementation;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVField(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.Field)
         */
        public PVField createPVField(Field field) {
			switch(field.getType()) {
			case scalar: 	     return createPVScalar((Scalar)field);
			case scalarArray:    return createPVScalarArray((ScalarArray)field);
			case structure:      return new BasePVStructure((Structure)field);
			case structureArray: return new BasePVStructureArray((StructureArray)field);
			case union: 	     return new BasePVUnion((Union)field);
			case unionArray:     return new BasePVUnionArray((UnionArray)field);
			}
            throw new IllegalArgumentException("Illegal Type");
		}
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVField(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.PVField)
         */
        public PVField createPVField(PVField fieldToClone) {
            switch(fieldToClone.getField().getType()) {
            case scalar:
                return createPVScalar((PVScalar)fieldToClone);
            case scalarArray:
                return createPVScalarArray((PVScalarArray)fieldToClone);
            case structure:
                return createPVStructure((PVStructure)fieldToClone);
            case structureArray:
                {
            	    PVStructureArray from =(PVStructureArray)fieldToClone;
            	    PVStructureArray to = createPVStructureArray(from.getStructureArray());
            	    convert.copyStructureArray(from, to);
            	    return to;
                }
            case union:
                return createPVUnion((PVUnion)fieldToClone);
            case unionArray:
            {
            	PVUnionArray from =(PVUnionArray)fieldToClone;
            	PVUnionArray to = createPVUnionArray(from.getUnionArray());
        	    convert.copyUnionArray(from, to);
        	    return to;
            }
            }
            throw new IllegalArgumentException("Illegal Type");
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVScalar(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.Scalar)
         */
        public PVScalar createPVScalar(Scalar scalar)
		{
           switch(scalar.getScalarType()) {
            case pvBoolean: return new BasePVBoolean(scalar);
            case pvByte:    return new BasePVByte(scalar);
            case pvShort:   return new BasePVShort(scalar);
            case pvInt:     return new BasePVInt(scalar);
            case pvLong:    return new BasePVLong(scalar);
            case pvUByte:    return new BasePVUByte(scalar);
            case pvUShort:   return new BasePVUShort(scalar);
            case pvUInt:     return new BasePVUInt(scalar);
            case pvULong:    return new BasePVULong(scalar);
            case pvFloat:   return new BasePVFloat(scalar);
            case pvDouble:  return new BasePVDouble(scalar);
            case pvString:  return new BasePVString(scalar);
            }
            throw new IllegalArgumentException(
                "Illegal Type. Must be pvBoolean,...");
	 	}
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVScalar(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.ScalarType)
         */
        public PVScalar createPVScalar(ScalarType scalarType)
        {
        	Scalar scalar = fieldCreate.createScalar( scalarType);
            return createPVScalar(scalar);
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVScalar(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.PVScalar)
         */
        public PVScalar createPVScalar(PVScalar scalarToClone) {
            PVScalar pvScalar = createPVScalar(scalarToClone.getScalar().getScalarType());
            convert.copyScalar(scalarToClone, pvScalar);
            return pvScalar;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVScalarArray(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.ScalarArray)
         */
        public PVScalarArray createPVScalarArray(ScalarArray array)
        {
        	switch(array.getElementType()) {
            case pvBoolean: return new BasePVBooleanArray(array);
            case pvByte:    return new BasePVByteArray(array);
            case pvShort:   return new BasePVShortArray(array);
            case pvInt:     return new BasePVIntArray(array);
            case pvLong:    return new BasePVLongArray(array);
            case pvUByte:    return new BasePVUByteArray(array);
            case pvUShort:   return new BasePVUShortArray(array);
            case pvUInt:     return new BasePVUIntArray(array);
            case pvULong:    return new BasePVULongArray(array);
            case pvFloat:   return new BasePVFloatArray(array);
            case pvDouble:  return new BasePVDoubleArray(array);
            case pvString:  return new BasePVStringArray(array);
            }
            throw new IllegalArgumentException("Illegal Type. Logic error");
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVScalarArray(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.ScalarType)
         */
        public PVScalarArray createPVScalarArray(ScalarType scalarType)
        {
        	return createPVScalarArray(fieldCreate.createScalarArray(scalarType));
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVScalarArray(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.PVScalarArray)
         */
        public PVScalarArray createPVScalarArray(PVScalarArray arrayToClone) {
            PVScalarArray pvArray = createPVScalarArray(arrayToClone.getScalarArray().getElementType());
            convert.copyScalarArray(arrayToClone,0, pvArray,0,arrayToClone.getLength());
            return pvArray;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVStructureArray(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.StructureArray)
         */
        public PVStructureArray createPVStructureArray(StructureArray structureArray) {
			return new BasePVStructureArray(structureArray);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.PVDataCreate#createPVUnionArray(org.epics.pvdata.pv.UnionArray)
		 */
		public PVUnionArray createPVUnionArray(UnionArray unionArray) {
			return new BasePVUnionArray(unionArray);
		}
		/* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVStructure(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.Structure)
         */
        public PVStructure createPVStructure(Structure structure)
        {
            return new BasePVStructure(structure);
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVStructure(org.epics.pvdata.pv.PVStructure, java.lang.String[], org.epics.pvdata.pv.PVField[])
         */
        public PVStructure createPVStructure(String[] fieldNames,PVField[] pvFields)
        {
            int length = pvFields.length;
            Field[] fields = new Field[length];
            for(int i=0; i<length; i++) fields[i] = pvFields[i].getField();
            Structure structure = fieldCreate.createStructure(fieldNames, fields);
            PVStructure pvStructure = new BasePVStructure(structure,pvFields);
            return pvStructure;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVStructure(org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.PVStructure)
         */
        public PVStructure createPVStructure(PVStructure structToClone)
        {
            if(structToClone==null) {
                throw new IllegalArgumentException("structToClone is null");
            }
        	Structure structure = fieldCreate.createStructure(structToClone.getStructure());
            PVStructure pvStructure = new BasePVStructure(structure);
        	if(!copyStructure(structToClone,pvStructure)) {
        	    System.err.printf("strictToClone%n%s%n", structToClone);
        	    System.err.printf("pvStructure%n%s%n", pvStructure);
        	    System.err.printf("strictToClone structure%n%s%n", structToClone.getStructure());
                System.err.printf("pvStructure structure%n%s%n", pvStructure.getStructure());
        	    throw new IllegalStateException("structureToClone differs from pvStructure");
        	}
        	return pvStructure;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#createPVUnion(org.epics.pvdata.pv.Union)
         */
        public PVUnion createPVUnion(Union union) {
        	return new BasePVUnion(union);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.PVDataCreate#createPVVariantUnion()
		 */
		public PVUnion createPVVariantUnion() {
			return new BasePVUnion(fieldCreate.createVariantUnion());
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.PVDataCreate#createPVUnion(org.epics.pvdata.pv.PVUnion)
		 */
		public PVUnion createPVUnion(PVUnion unionToClone) {
            if (unionToClone==null)
                throw new IllegalArgumentException("unionToClone is null");

			PVUnion union = new BasePVUnion(unionToClone.getUnion());
			// set cloned value
			union.set(unionToClone.getSelectedIndex(), createPVField(unionToClone.get()));
			return union;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.PVDataCreate#createPVVariantUnionArray()
		 */
		public PVUnionArray createPVVariantUnionArray() {
			return new BasePVUnionArray(fieldCreate.createVariantUnionArray());
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.PVDataCreate#createPVScalar(org.epics.pvdata.pv.PVScalarType)
		 */
		public <T extends PVScalar, TA extends PVScalarArray> T createPVScalar(
				PVScalarType<T, TA> scalarType) {
			return scalarType.getPVFieldClass().cast(createPVScalar(scalarType.getScalarType()));
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.PVDataCreate#createPVScalarArray(org.epics.pvdata.pv.PVScalarType)
		 */
		public <T extends PVScalar, TA extends PVScalarArray> TA createPVScalarArray(
				PVScalarType<T, TA> elementType) {
			return elementType.getPVFieldArrayClass().cast(createPVScalarArray(elementType.getScalarType()));
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.PVDataCreate#createPVStructureArray(org.epics.pvdata.pv.Structure)
		 */
		public PVStructureArray createPVStructureArray(Structure structure) {
			return createPVStructureArray(fieldCreate.createStructureArray(structure));
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.PVDataCreate#createPVUnionArray(org.epics.pvdata.pv.Union)
		 */
		public PVUnionArray createPVUnionArray(Union union) {
			return createPVUnionArray(fieldCreate.createUnionArray(union));
		}
		/* (non-Javadoc)
         * @see org.epics.pvdata.pv.PVDataCreate#flattenPVStructure(org.epics.pvdata.pv.PVStructure)
         */
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

        private boolean copyStructure(PVStructure from,PVStructure to)  {
            boolean result = true;
            PVField[] fromFields = from.getPVFields();
            PVField[] toFields = to.getPVFields();
            if(fromFields.length!=toFields.length) {
            	Formatter formatter = new Formatter();
            	formatter.format("PVDataFactory.copyStructure number of fields differ %nfrom %s%nto %s",
            			from.toString(),to.toString());
            	String message = formatter.toString();
            	System.out.println(message);
            	formatter.close();
            	return false;
            }
            for(int i=0; i<fromFields.length; i++) {
            	PVField fromPV = fromFields[i];
            	PVField toPV = toFields[i];
            	Type type = fromPV.getField().getType();
            	if(type==Type.structure) {
            		boolean xxx = copyStructure((PVStructure)fromPV,(PVStructure)toPV);
            		if(result) result = xxx;
            		continue;
            	}
                if(type==Type.scalar) {
                	convert.copyScalar((PVScalar)fromPV, (PVScalar)toPV);
                } else if(type==Type.scalarArray) {
                	PVScalarArray fromPVArray = (PVScalarArray)fromPV;
                	PVScalarArray toPVArray = (PVScalarArray)toPV;
                	convert.copyScalarArray(fromPVArray,0,toPVArray, 0, fromPVArray.getLength());
                } else {
                	PVStructureArray fromPVArray = (PVStructureArray)fromPV;
                	PVStructureArray toPVArray = (PVStructureArray)toPV;
                	convert.copyStructureArray(fromPVArray,toPVArray);
                }
            }
            return result;
        }
    }
}
