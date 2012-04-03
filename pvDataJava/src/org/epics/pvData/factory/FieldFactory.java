/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarArray;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.StructureArray;

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
        }
        return singleImplementation;
    }
    
    private static final class FieldCreateImpl implements FieldCreate{
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#createScalar(org.epics.pvData.pv.ScalarType)
         */
        @Override
        public Scalar createScalar(ScalarType scalarType)
        {
            return scalars[scalarType.ordinal()];
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#createArray(java.lang.String, org.epics.pvData.pv.ScalarType)
         */
        public ScalarArray createScalarArray(ScalarType elementType)
        {
            return scalarArrays[elementType.ordinal()];
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#createArray(java.lang.String, org.epics.pvData.pv.Structure)
         */
        @Override
		public StructureArray createStructureArray(Structure elementStructure)
        {
			return new BaseStructureArray(elementStructure);
		}
		/* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#createStructure(java.lang.String, org.epics.pvData.pv.Field[])
         */
        public Structure createStructure(String[] fieldNames, Field[] fields)
        {
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
        
        
        
        
    	static final ScalarType integerLUT[] =
    	{
    		ScalarType.pvByte,  // 8-bits
    		ScalarType.pvShort, // 16-bits
    		ScalarType.pvInt,   // 32-bits
    		ScalarType.pvLong,  // 64-bits
    		null, 
    		null, 
    		null, 
    		null, 
    		ScalarType.pvUByte,  // unsigned 8-bits
    		ScalarType.pvUShort, // unsigned 16-bits
    		ScalarType.pvUInt,   // unsigned 32-bits
    		ScalarType.pvULong,  // unsigned 64-bits
    		null,
    		null,
    		null,
    		null
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
    		null,
    		null, 
    		null,
    		null, 
    		null,
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
    		case 1: return integerLUT[code & 0x0F];
    		case 2: return floatLUT[code & 0x0F];
    		case 3: return ScalarType.pvString;
    		default: return null;
    		}
    	}
        
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.FieldCreate#deserialize(java.nio.ByteBuffer, org.epics.pvData.pv.DeserializableControl)
		 */
		@Override
		public Field deserialize(ByteBuffer buffer, DeserializableControl control) {
    		control.ensureData(1);
    		final byte code = buffer.get();
    		if (code == (byte)-1)
    			return null;
    		
    		final int typeCode = code & 0xE0;
    		final boolean notArray = ((code & 0x10) == 0);
    		if (notArray)
    		{			
    			if (typeCode < 0x80)
    			{
    				// Type type = Type.scalar;
    				ScalarType scalarType = decodeScalar(code);
    				if (scalarType == null)
    					throw new IllegalArgumentException("invalid scalar type encoding");
    				return new BaseScalar(scalarType);
    			}
    			else if (typeCode == 0x80)
    			{
    				// Type type = Type.structure;
    				return BaseStructure.deserializeStructureField(buffer, control);
    			}
    			else
    				throw new IllegalArgumentException("invalid type encoding");
    		}
    		else // array
    		{
    			if (typeCode < 0x80)
    			{
    				// Type type = Type.scalarArray;
    				ScalarType scalarType = decodeScalar(code);
    				if (scalarType == null)
    					throw new IllegalArgumentException("invalid scalarArray type encoding");
    				return new BaseScalarArray(scalarType);
    			}
    			else if (typeCode == 0x80)
    			{
    				// Type type = Type.structureArray;
    				final Structure elementStructure = (Structure)control.cachedDeserialize(buffer);
    				return new BaseStructureArray(elementStructure);
    			}
    			else
    				throw new IllegalArgumentException("invalid type encoding");
    		}
		}  
        
        
    }
}
