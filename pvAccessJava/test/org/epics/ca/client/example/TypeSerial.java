package org.epics.ca.client.example;
import java.nio.ByteBuffer;

import org.epics.ca.PVFactory;
import org.epics.pvData.misc.SerializeHelper;
import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.SerializableControl;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.Type;
import org.omg.CORBA.BooleanHolder;


public class TypeSerial {

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
		ScalarType.pvByte,  // unsigned 8-bits
		ScalarType.pvShort, // unsigned 16-bits
		ScalarType.pvInt,   // unsigned 32-bits
		ScalarType.pvLong,  // unsigned 64-bits
		/* TODO !!!!
		ScalarType.pvUByte,  // unsigned 8-bits
		ScalarType.pvUShort, // unsigned 16-bits
		ScalarType.pvUInt,   // unsigned 32-bits
		ScalarType.pvULong,  // unsigned 64-bits
		*/
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

	static final FieldCreate fieldCreate = PVFactory.getFieldCreate();

	static final Field decodeFieldDesc(ByteBuffer buffer, DeserializableControl control)
	{
		control.ensureData(1);
		return decodeFieldDesc(buffer.get(), buffer, control);
	}
	static final Field decodeFieldDesc(byte code, ByteBuffer buffer, DeserializableControl control)
	{
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
				final String fieldName = SerializeHelper.deserializeString(buffer, control);
				return fieldCreate.createScalar(fieldName, scalarType);
			}
			else if (typeCode == 0x80)
			{
				// Type type = Type.structure;
				final String fieldName = SerializeHelper.deserializeString(buffer, control);
				int numFields = SerializeHelper.readSize(buffer, control);
				Field[] fields = new Field[numFields];
				for (int i = 0; i < numFields; i++)
					fields[i] = control.cachedDeserialize(buffer);
				return fieldCreate.createStructure(fieldName, fields);
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
				final String fieldName = SerializeHelper.deserializeString(buffer, control);
				return fieldCreate.createScalarArray(fieldName, scalarType);
			}
			else if (typeCode == 0x80)
			{
				// Type type = Type.structureArray;
				final String fieldName = SerializeHelper.deserializeString(buffer, control);
				Structure elementStructure = (Structure)control.cachedDeserialize(buffer);
				return fieldCreate.createStructureArray(fieldName, elementStructure);
			}
			else
				throw new IllegalArgumentException("invalid type encoding");
		}
	}

	
	
	/**
	 * Null type.
	 */
	public static final byte NULL_TYPE_CODE = (byte)-1;

	/**
	 * Serialization contains only an ID (that was assigned by one of the previous <code>FULL_WITH_ID</code> descriptions).
	 */
	public static final byte ONLY_ID_TYPE_CODE = (byte)-2;

	/**
	 * Serialization contains an ID (that can be used later, if cached) and full interface description.
	 */
	public static final byte FULL_WITH_ID_TYPE_CODE = (byte)-3;
	
	// TODO no cache
	static final Field decodeField(ByteBuffer buffer, DeserializableControl control)
	{
		control.ensureData(1);
		final byte typeCode = buffer.get();
		if (typeCode == NULL_TYPE_CODE)
			return null;
		else if (typeCode == ONLY_ID_TYPE_CODE)
		{
			/*
			if (deserializationRegistry == null)
				throw new IllegalStateException("deserialization provided cached ID, but no registry provided");
			control.ensureData(2);
			return deserializationRegistry.getIntrospectionInterface(buffer.getShort());
			*/
			return null;
		}
		else if (typeCode == FULL_WITH_ID_TYPE_CODE)
		{
			/*
			if (deserializationRegistry == null)
				throw new IllegalStateException("deserialization provided cached ID, but no registry provided");
			control.ensureData(2);
			final short id = buffer.getShort();
			final Field field = decodeFieldDesc(buffer, control);
			deserializationRegistry.registerIntrospectionInterface(id, field);
			return field;
			*/
			return null;
		}
		else // if (typeCode < (byte)0xDF)
			return decodeFieldDesc(typeCode, buffer, control);
	}
	
	// TODO cachedDeserialize calls decodeField with the right registry
	
	
	static short key = 0;
	
	static final void encodeField(Field field, ByteBuffer buffer, SerializableControl control) {
		if (field == null) {
			control.ensureBuffer(1);
    		buffer.put(NULL_TYPE_CODE);
		}
		else
		{ 
			// use registry check for structures only
			if (field.getType() == Type.structure)
			{
				BooleanHolder existing = new BooleanHolder();
				// TODO final short key = registry.registerIntrospectionInterface(field, existing);
				existing.value = false; key++;
				
				if (existing.value)
				{
					control.ensureBuffer(3);
					buffer.put(ONLY_ID_TYPE_CODE);
					buffer.putShort(key);
					return;
				} 
				else
				{
					control.ensureBuffer(3);
					buffer.put(FULL_WITH_ID_TYPE_CODE);
					buffer.putShort(key);
				}
			}

			field.serialize(buffer, control);
		}
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
