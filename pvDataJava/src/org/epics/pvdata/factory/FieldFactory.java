/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.BoundedString;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldBuilder;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.StructureArray;
import org.epics.pvdata.pv.Union;
import org.epics.pvdata.pv.UnionArray;

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
    private static FieldCreateImpl singleImplementation = null;
    private static Scalar[] scalars = null;
    private static ScalarArray[] scalarArrays = null;
    private static Union variantUnion = null;
    private static UnionArray variantUnionArray = null;
    /**
     * Get the FieldCreate interface.
     * @return The interface for creating introspection objects.
     */
    public static synchronized FieldCreate getFieldCreate() {
        if (singleImplementation==null) {
            singleImplementation = new FieldCreateImpl();
            ScalarType[] scalarTypes =  ScalarType.values();
            int num = scalarTypes.length;
            scalars = new Scalar[num];
            for(int i = 0; i<num; i++) scalars[i] = new BaseScalar(scalarTypes[i]);
            scalarArrays = new ScalarArray[num];
            for(int i = 0; i<num; i++) scalarArrays[i] = new BaseScalarArray(scalarTypes[i]);
            variantUnion = new BaseUnion();
            variantUnionArray = new BaseUnionArray(variantUnion);
        }
        return singleImplementation;
    }

    private static final class FieldCreateImpl implements FieldCreate{
        /* (non-Javadoc)
		 * @see org.epics.pvdata.pv.FieldCreate#createFieldBuilder()
		 */
		public FieldBuilder createFieldBuilder() {
			return new BaseFieldBuilder();
		}
		/* (non-Javadoc)
         * @see org.epics.pvdata.pv.FieldCreate#createScalar(org.epics.pvdata.pv.ScalarType)
         */
        public Scalar createScalar(ScalarType scalarType)
        {
            return scalars[scalarType.ordinal()];
        }

        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.FieldCreate#createBoundedString(int)
         */
        public BoundedString createBoundedString(int maxLength) {
			return new BaseBoundedString(maxLength);
		}
		/* (non-Javadoc)
         * @see org.epics.pvdata.pv.FieldCreate#createArray(java.lang.String, org.epics.pvdata.pv.ScalarType)
         */
        public ScalarArray createScalarArray(ScalarType elementType)
        {
            return scalarArrays[elementType.ordinal()];
        }

        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.FieldCreate#createFixedScalarArray(org.epics.pvdata.pv.ScalarType, int)
         */
        public ScalarArray createFixedScalarArray(ScalarType elementType, int size) {
        	return new BaseScalarFixedArray(elementType, size);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.FieldCreate#createBoundedScalarArray(org.epics.pvdata.pv.ScalarType, int)
		 */
		public ScalarArray createBoundedScalarArray(ScalarType elementType, int bound) {
        	return new BaseScalarBoundedArray(elementType, bound);
		}
		/* (non-Javadoc)
         * @see org.epics.pvdata.pv.FieldCreate#createArray(java.lang.String, org.epics.pvdata.pv.Structure)
         */
        public StructureArray createStructureArray(Structure elementStructure)
        {
			return new BaseStructureArray(elementStructure);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.FieldCreate#createUnionArray(org.epics.pvdata.pv.Union)
		 */
		public UnionArray createUnionArray(Union elementUnion) {
			return new BaseUnionArray(elementUnion);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.FieldCreate#createVariantUnionArray()
		 */
		public UnionArray createVariantUnionArray() {
			return variantUnionArray;
		}
		/* (non-Javadoc)
         * @see org.epics.pvdata.pv.FieldCreate#createStructure(java.lang.String, org.epics.pvdata.pv.Field[])
         */
        public Structure createStructure(String[] fieldNames, Field[] fields)
        {
            validateFieldNames(fieldNames);
            return new BaseStructure(fieldNames,fields);
        }

		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.FieldCreate#createStructure(java.lang.String, java.lang.String[], org.epics.pvdata.pv.Field[])
		 */
		public Structure createStructure(String id, String[] fieldNames, Field[] fields) {
            validateFieldNames(fieldNames);
            return new BaseStructure(id,fieldNames,fields);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.FieldCreate#createStructure(org.epics.pvdata.pv.Structure)
		 */
		public Structure createStructure(Structure structToClone) {
		    String[] oldFieldNames = structToClone.getFieldNames();
		    Field[] oldFields = structToClone.getFields();
		    int n = oldFieldNames.length;
		    String[] fieldNames = new String[n];
		    Field[] fields = new Field[n];
		    for(int i=0; i<n; i++) {
		        fieldNames[i] = oldFieldNames[i];
		        fields[i] = oldFields[i];
		    }
		    String id = structToClone.getID();
		    BaseStructure structure = (BaseStructure)createStructure(id,fieldNames,fields);
		    structure.clone(fields, fieldNames);
		    return structure;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.FieldCreate#appendField(org.epics.pvdata.pv.Structure, java.lang.String, org.epics.pvdata.pv.Field)
         */
        public Structure appendField(Structure structure, String fieldName, Field field) {
		    validateFieldName(fieldName);
		    String[] oldNames = structure.getFieldNames();
		    Field[] oldFields = structure.getFields();
                    String oldID = structure.getID();
		    int oldlen = oldNames.length;
		    String[] newNames = new String[oldlen+1];
		    Field[] newFields = new Field[oldlen+1];
		    for(int i=0; i<oldlen; i++) {
		        newNames[i] = oldNames[i];
		        newFields[i] = oldFields[i];
		    }
		    newNames[oldlen] = fieldName;
		    newFields[oldlen] = field;
            return createStructure(oldID,newNames,newFields);

        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.pv.FieldCreate#appendFields(org.epics.pvdata.pv.Structure, java.lang.String[], org.epics.pvdata.pv.Field[])
         */
        public Structure appendFields(Structure structure, String[] fieldNames,Field[] fields) {
            validateFieldNames(fieldNames);
            String[] oldNames = structure.getFieldNames();
            Field[] oldFields = structure.getFields();
            String oldID = structure.getID();
            int oldlen = oldNames.length;
            int extra = fieldNames.length;
            int newlen = oldlen + fieldNames.length;
            String[] newNames = new String[newlen];
            Field[] newFields = new Field[newlen];
            for(int i=0; i<oldlen; i++) {
                newNames[i] = oldNames[i];
                newFields[i] = oldFields[i];
            }
            for(int i=0; i<extra; i++) {
                newNames[i+oldlen] = fieldNames[i];
                newFields[i+oldlen] = fields[i];
            }
            return createStructure(oldID,newNames,newFields);
        }

    	/* (non-Javadoc)
    	 * @see org.epics.pvdata.pv.FieldCreate#createVariantUnion()
    	 */
    	public Union createVariantUnion() {
			return variantUnion;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.FieldCreate#createUnion(java.lang.String[], org.epics.pvdata.pv.Field[])
		 */
		public Union createUnion(String[] fieldNames, Field[] fields) {
			validateFieldNames(fieldNames);
			return new BaseUnion(fieldNames, fields);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.FieldCreate#createUnion(java.lang.String, java.lang.String[], org.epics.pvdata.pv.Field[])
		 */
		public Union createUnion(String id, String[] fieldNames, Field[] fields) {
			validateFieldNames(fieldNames);
			return new BaseUnion(id, fieldNames, fields);
		}




		static final ScalarType integerLUT[] =
    	{
    		ScalarType.pvByte,  // 8-bits
    		ScalarType.pvShort, // 16-bits
    		ScalarType.pvInt,   // 32-bits
    		ScalarType.pvLong,  // 64-bits
    		ScalarType.pvUByte,  // unsigned 8-bits
    		ScalarType.pvUShort, // unsigned 16-bits
    		ScalarType.pvUInt,   // unsigned 32-bits
    		ScalarType.pvULong   // unsigned 64-bits
    	};

    	static final ScalarType floatLUT[] =
    	{
    		null, // reserved
    		null, // 16-bits
    		ScalarType.pvFloat,   // 32-bits
    		ScalarType.pvDouble,  // 64-bits
    		null,
    		null,
    		null,
    		null
    	};

    	static final ScalarType decodeScalar(byte code)
    	{
    		// bits 7-5
    		switch (code >> 5)
    		{
    		case 0: return ScalarType.pvBoolean;
    		case 1: return integerLUT[code & 0x07];
    		case 2: return floatLUT[code & 0x07];
    		case 3: return ScalarType.pvString;
    		default: return null;
    		}
    	}

		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.FieldCreate#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
		 */
		public Field deserialize(ByteBuffer buffer, DeserializableControl control) {
    		control.ensureData(1);
    		final byte code = buffer.get();
    		if (code == (byte)-1)
    			return null;

    		final int typeCode = code & 0xE7;
    		final int scalarOrArray = code & 0x18;
    		final boolean notArray = (scalarOrArray == 0);
    		if (notArray)
    		{
    			if (typeCode < 0x80)
    			{
    				// Type type = Type.scalar;
    				ScalarType scalarType = decodeScalar(code);
    				if (scalarType == null)
    					throw new IllegalArgumentException("invalid scalar type encoding");
    				return scalars[scalarType.ordinal()];
    			}
    			else if (typeCode == 0x80)
    			{
    				// Type type = Type.structure;
    				return BaseStructure.deserializeStructureField(buffer, control);
    			}
    			else if (typeCode == 0x81)
    			{
    				// Type type = union;
    				return BaseUnion.deserializeUnionField(buffer, control);
    			}
    			else if (typeCode == 0x82)
    			{
    				// Type type = union; variant union (aka any type)
    				return variantUnion;
    			}
    			else if (typeCode == 0x83)
    			{
    				// TODO cache some sizes?
    				// bounded string
    				int maxLength = SerializeHelper.readSize(buffer, control);
    				return new BaseBoundedString(maxLength);
    			}
    			else
    				throw new IllegalArgumentException("invalid type encoding");
    		}
    		else // array
    		{
    			final boolean isVariable = (scalarOrArray == 0x08);
    			// bounded == 0x10;
    			final boolean isFixed = (scalarOrArray == 0x18);

				int size = (isVariable ? 0 : SerializeHelper.readSize(buffer, control));

    			if (typeCode < 0x80)
    			{
    				// Type type = Type.scalarArray;
    				ScalarType scalarType = decodeScalar(code);
    				if (scalarType == null)
    					throw new IllegalArgumentException("invalid scalarArray type encoding");
    				if (isVariable)
    					return scalarArrays[scalarType.ordinal()];
    				else if (isFixed)
						return new BaseScalarFixedArray(scalarType, size);
					else
						return new BaseScalarBoundedArray(scalarType, size);
    			}
    			else if (typeCode == 0x80)
    			{
    				// TODO fixed and bounded array support
    				if (!isVariable)
       					throw new IllegalArgumentException("fixed and bounded structure array not supported");

    				// Type type = Type.structureArray;
    				final Structure elementStructure = (Structure)control.cachedDeserialize(buffer);
    				return new BaseStructureArray(elementStructure);
    			}
    			else if (typeCode == 0x81)
    			{
    				// TODO fixed and bounded array support
    				if (!isVariable)
       					throw new IllegalArgumentException("fixed and bounded union array not supported");

    				// Type type = unionArray;
    				final Union elementUnion = (Union)control.cachedDeserialize(buffer);
    				return new BaseUnionArray(elementUnion);
    			}
    			else if (typeCode == 0x82)
    			{
    				// TODO fixed and bounded array support
    				if (!isVariable)
       					throw new IllegalArgumentException("fixed and bounded union array not supported");

    				// Type type = unionArray; variant union (aka any type)
    				return variantUnionArray;
    			}
    			else
    				throw new IllegalArgumentException("invalid type encoding");
    		}
		}

		private void validateFieldNames(String[] names)
		{
			for(String name : names) {
				validateFieldName(name);
			}
		}

		private void validateFieldName(String name)
		{
			if(!name.matches("[_a-zA-Z][_a-zA-Z0-9]*"))
					throw new IllegalArgumentException("Invalid field name '"+name+"'");
		}
    }
}
