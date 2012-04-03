/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.factory.StandardFieldFactory;
import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVBoolean;
import org.epics.pvData.pv.PVBooleanArray;
import org.epics.pvData.pv.PVByte;
import org.epics.pvData.pv.PVByteArray;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVDouble;
import org.epics.pvData.pv.PVDoubleArray;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVFloat;
import org.epics.pvData.pv.PVFloatArray;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVIntArray;
import org.epics.pvData.pv.PVLong;
import org.epics.pvData.pv.PVLongArray;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.PVShort;
import org.epics.pvData.pv.PVShortArray;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStringArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.PVUByte;
import org.epics.pvData.pv.PVUInt;
import org.epics.pvData.pv.PVUShort;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarArray;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.SerializableControl;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.StructureArray;

/**
 * JUnit test for PVData serialization.
 * @author mse
 *
 */
public class SerializationTest extends TestCase {
	
	private static class SerializableFlushImpl implements SerializableControl {

		@Override
		public void ensureBuffer(int size) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void flushSerializeBuffer() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void alignBuffer(int alignment) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.SerializableControl#cachedSerialize(org.epics.pvData.pv.Field, java.nio.ByteBuffer)
		 */
		@Override
		public void cachedSerialize(Field field, ByteBuffer buffer) {
			// no cache
			field.serialize(buffer, this);
		}
		
	}
	private static SerializableControl flusher = new SerializableFlushImpl();
	

	private static class DeserializableControlImpl implements DeserializableControl {

		@Override
		public void ensureData(int size) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void alignData(int alignment) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.DeserializableControl#cachedDeserialize(java.nio.ByteBuffer)
		 */
		@Override
		public Field cachedDeserialize(ByteBuffer buffer) {
			// no cache
			return FieldFactory.getFieldCreate().deserialize(buffer, this);
		}
		
	}
	private static DeserializableControl control = new DeserializableControlImpl();

	public void testScalarEquals()
	{
		PVDataCreate factory = PVDataFactory.getPVDataCreate();
		ScalarType[] types = ScalarType.values();
		for (int i = 0; i < types.length; i++)
		{
			PVScalar scalar1 = factory.createPVScalar(null, types[i]);
			PVScalar scalar2 = factory.createPVScalar(null, types[i]);
			assertEquals(scalar1, scalar2);
		}
	}

	private void serializatioTest(PVField field)
	{
		// serialize
		ByteBuffer buffer = ByteBuffer.allocate(1 << 16);
		field.serialize(buffer, flusher);
		
		// deserialize
		buffer.flip();
		
		// create new instance and deserialize
		PVField deserializedField = PVDataFactory.getPVDataCreate().createPVField(null, field.getField());
		deserializedField.deserialize(buffer, control);
	
		// must equal
		assertEquals(field.toString(), field, deserializedField);
	}

	public void testScalarNonInitialized()
	{
		PVDataCreate factory = PVDataFactory.getPVDataCreate();
		ScalarType[] types = ScalarType.values();
		for (int i = 0; i < types.length; i++)
		{
			PVScalar scalar = factory.createPVScalar(null, types[i]);
			serializatioTest(scalar);
		}
	}

	public void testScalar()
	{
		PVDataCreate factory = PVDataFactory.getPVDataCreate();
		
		PVBoolean pvBoolean = (PVBoolean)factory.createPVScalar(null, ScalarType.pvBoolean);
		pvBoolean.put(false);
		serializatioTest(pvBoolean);
		pvBoolean.put(true);
		serializatioTest(pvBoolean);
		
		PVByte pvByte = (PVByte)factory.createPVScalar(null, ScalarType.pvByte);
		pvByte.put((byte)0);
		serializatioTest(pvByte);
		pvByte.put((byte)12);
		serializatioTest(pvByte);
		pvByte.put((byte)Byte.MAX_VALUE);
		serializatioTest(pvByte);
		pvByte.put((byte)Byte.MIN_VALUE);
		serializatioTest(pvByte);
		
		PVShort pvShort = (PVShort)factory.createPVScalar(null, ScalarType.pvShort);
		pvShort.put((short)0);
		serializatioTest(pvShort);
		pvShort.put((short)1234);
		serializatioTest(pvShort);
		pvShort.put((short)Byte.MAX_VALUE);
		serializatioTest(pvShort);
		pvShort.put((short)Byte.MIN_VALUE);
		serializatioTest(pvShort);
		pvShort.put((short)Short.MAX_VALUE);
		serializatioTest(pvShort);
		pvShort.put((short)Short.MIN_VALUE);
		serializatioTest(pvShort);

		PVInt pvInt = (PVInt)factory.createPVScalar(null, ScalarType.pvInt);
		pvInt.put(0);
		serializatioTest(pvInt);
		pvInt.put(123456);
		serializatioTest(pvInt);
		pvInt.put(Byte.MAX_VALUE);
		serializatioTest(pvInt);
		pvInt.put(Byte.MIN_VALUE);
		serializatioTest(pvInt);
		pvInt.put(Short.MAX_VALUE);
		serializatioTest(pvInt);
		pvInt.put(Short.MIN_VALUE);
		serializatioTest(pvInt);
		pvInt.put(Integer.MAX_VALUE);
		serializatioTest(pvInt);
		pvInt.put(Integer.MIN_VALUE);
		serializatioTest(pvInt);

		PVLong pvLong = (PVLong)factory.createPVScalar(null, ScalarType.pvLong);
		pvLong.put(0);
		serializatioTest(pvLong);
		pvLong.put(12345678901L);
		serializatioTest(pvLong);
		pvLong.put(Byte.MAX_VALUE);
		serializatioTest(pvLong);
		pvLong.put(Byte.MIN_VALUE);
		serializatioTest(pvLong);
		pvLong.put(Short.MAX_VALUE);
		serializatioTest(pvLong);
		pvLong.put(Short.MIN_VALUE);
		serializatioTest(pvLong);
		pvLong.put(Integer.MAX_VALUE);
		serializatioTest(pvLong);
		pvLong.put(Integer.MIN_VALUE);
		serializatioTest(pvLong);
		pvLong.put(Long.MAX_VALUE);
		serializatioTest(pvLong);
		pvLong.put(Long.MIN_VALUE);
		serializatioTest(pvLong);
	
		PVUByte pvUByte = (PVUByte)factory.createPVScalar(null, ScalarType.pvUByte);
		pvUByte.put((byte)0);
		serializatioTest(pvUByte);
		pvUByte.put((byte)12);
		serializatioTest(pvUByte);
		pvUByte.put((byte)-12);
		serializatioTest(pvUByte);
		pvUByte.put((byte)Byte.MAX_VALUE);
		serializatioTest(pvUByte);
		pvUByte.put((byte)Byte.MIN_VALUE);
		serializatioTest(pvUByte);
		
		PVUShort pvUShort = (PVUShort)factory.createPVScalar(null, ScalarType.pvUShort);
		pvUShort.put((short)0);
		serializatioTest(pvUShort);
		pvUShort.put((short)1234);
		serializatioTest(pvUShort);
		pvUShort.put((short)-1234);
		serializatioTest(pvUShort);
		pvUShort.put((short)Byte.MAX_VALUE);
		serializatioTest(pvUShort);
		pvUShort.put((short)Byte.MIN_VALUE);
		serializatioTest(pvUShort);
		pvUShort.put((short)Short.MAX_VALUE);
		serializatioTest(pvUShort);
		pvUShort.put((short)Short.MIN_VALUE);
		serializatioTest(pvUShort);

		PVUInt pvUInt = (PVUInt)factory.createPVScalar(null, ScalarType.pvUInt);
		pvUInt.put(0);
		serializatioTest(pvUInt);
		pvUInt.put(123456);
		serializatioTest(pvUInt);
		pvUInt.put(-123456);
		serializatioTest(pvUInt);
		pvUInt.put(Byte.MAX_VALUE);
		serializatioTest(pvUInt);
		pvUInt.put(Byte.MIN_VALUE);
		serializatioTest(pvUInt);
		pvUInt.put(Short.MAX_VALUE);
		serializatioTest(pvUInt);
		pvUInt.put(Short.MIN_VALUE);
		serializatioTest(pvUInt);
		pvUInt.put(Integer.MAX_VALUE);
		serializatioTest(pvUInt);
		pvUInt.put(Integer.MIN_VALUE);
		serializatioTest(pvUInt);

		PVLong pvULong = (PVLong)factory.createPVScalar(null, ScalarType.pvULong);
		pvULong.put(0);
		serializatioTest(pvULong);
		pvULong.put(12345678901L);
		serializatioTest(pvULong);
		pvULong.put(-12345678901L);
		serializatioTest(pvULong);
		pvULong.put(Byte.MAX_VALUE);
		serializatioTest(pvULong);
		pvULong.put(Byte.MIN_VALUE);
		serializatioTest(pvULong);
		pvULong.put(Short.MAX_VALUE);
		serializatioTest(pvULong);
		pvULong.put(Short.MIN_VALUE);
		serializatioTest(pvULong);
		pvULong.put(Integer.MAX_VALUE);
		serializatioTest(pvULong);
		pvULong.put(Integer.MIN_VALUE);
		serializatioTest(pvULong);
		pvULong.put(Long.MAX_VALUE);
		serializatioTest(pvULong);
		pvULong.put(Long.MIN_VALUE);
		serializatioTest(pvULong);

		PVFloat pvFloat = (PVFloat)factory.createPVScalar(null, ScalarType.pvFloat);
		pvFloat.put(0);
		serializatioTest(pvFloat);
		pvFloat.put((float)12.345);
		serializatioTest(pvFloat);
		pvFloat.put(Float.MAX_VALUE);
		serializatioTest(pvFloat);
		pvFloat.put(Float.MIN_VALUE);
		serializatioTest(pvFloat);
		//pvFloat.put(Float.NaN);
		//serializatioTest(pvFloat);
		pvFloat.put(Float.POSITIVE_INFINITY);
		serializatioTest(pvFloat);
		pvFloat.put(Float.NEGATIVE_INFINITY);
		serializatioTest(pvFloat);

		PVDouble pvDouble = (PVDouble)factory.createPVScalar(null, ScalarType.pvDouble);
		pvDouble.put(0);
		serializatioTest(pvDouble);
		pvDouble.put(12.345);
		serializatioTest(pvDouble);
		pvDouble.put(Double.MAX_VALUE);
		serializatioTest(pvDouble);
		pvDouble.put(Double.MIN_VALUE);
		serializatioTest(pvDouble);
		//pvDouble.put(Double.NaN);
		//serializatioTest(pvDouble);
		pvDouble.put(Double.POSITIVE_INFINITY);
		serializatioTest(pvDouble);
		pvDouble.put(Double.NEGATIVE_INFINITY);
		serializatioTest(pvDouble);

		PVString pvString = (PVString)factory.createPVScalar(null, ScalarType.pvString);
		pvString.put("");
		serializatioTest(pvString);
		pvString.put("s");
		serializatioTest(pvString);
		pvString.put("string");
		serializatioTest(pvString);
		pvString.put("string with spaces");
		serializatioTest(pvString);
		pvString.put("string with spaces and special characters\f\n");
		serializatioTest(pvString);
		
		// huge string test
		StringBuffer strBuf = new StringBuffer(10000);
		for (int i = 0; i < strBuf.capacity(); i++)
			strBuf.append('a');
		pvString.put(strBuf.toString());
		serializatioTest(pvString);
	}
	
	public void testArrayNonInitialized()
	{
		PVDataCreate factory = PVDataFactory.getPVDataCreate();
		ScalarType[] types = ScalarType.values();
		for (int i = 0; i < types.length; i++)
		{
			PVArray array = factory.createPVScalarArray(null, types[i]);
			serializatioTest(array);
		}
	}

	
	public void testArray()
	{
		PVDataCreate factory = PVDataFactory.getPVDataCreate();
		
		PVBooleanArray pvBoolean = (PVBooleanArray)factory.createPVScalarArray(null, ScalarType.pvBoolean);
		pvBoolean.put(0, 0, new boolean[0], 0);
		serializatioTest(pvBoolean);
		final boolean[] bv = new boolean[] { false, true, false, true, true };
		pvBoolean.put(0, bv.length, bv, 0);
		serializatioTest(pvBoolean);

		PVByteArray pvByte = (PVByteArray)factory.createPVScalarArray(null, ScalarType.pvByte);
		pvByte.put(0, 0, new byte[0], 0);
		serializatioTest(pvByte);
		final byte[] byv = new byte[] { 0, 1, 2, -1, Byte.MAX_VALUE, Byte.MAX_VALUE - 1, Byte.MIN_VALUE + 1, Byte.MIN_VALUE };		
		pvByte.put(0, byv.length, byv, 0);
		serializatioTest(pvByte);

		PVShortArray pvShort = (PVShortArray)factory.createPVScalarArray(null, ScalarType.pvShort);
		pvShort.put(0, 0, new short[0], 0);
		serializatioTest(pvShort);
		final short[] sv = new short[] { 0, 1, 2, -1, Short.MAX_VALUE, Short.MAX_VALUE - 1, Short.MIN_VALUE + 1, Short.MIN_VALUE };		
		pvShort.put(0, sv.length, sv, 0);
		serializatioTest(pvShort);

		PVIntArray pvInt = (PVIntArray)factory.createPVScalarArray(null, ScalarType.pvInt);
		pvInt.put(0, 0, new int[0], 0);
		serializatioTest(pvInt);
		final int[] iv = new int[] { 0, 1, 2, -1, Integer.MAX_VALUE, Integer.MAX_VALUE - 1, Integer.MIN_VALUE + 1, Integer.MIN_VALUE };		
		pvInt.put(0, iv.length, iv, 0);
		serializatioTest(pvInt);

		PVLongArray pvLong = (PVLongArray)factory.createPVScalarArray(null, ScalarType.pvLong);
		pvLong.put(0, 0, new long[0], 0);
		serializatioTest(pvLong);
		final long[] lv = new long[] { 0, 1, 2, -1, Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MIN_VALUE + 1, Long.MIN_VALUE };		
		pvLong.put(0, lv.length, lv, 0);
		serializatioTest(pvLong);
	
		PVFloatArray pvFloat = (PVFloatArray)factory.createPVScalarArray(null, ScalarType.pvFloat);
		pvFloat.put(0, 0, new float[0], 0);
		serializatioTest(pvFloat);
		final float[] fv = new float[] { (float)0.0, (float)1.1, (float)2.3, (float)-1.4, Float.MAX_VALUE, Float.MAX_VALUE - (float)1.6, Float.MIN_VALUE + (float)1.1, Float.MIN_VALUE };		
		pvFloat.put(0, fv.length, fv, 0);
		serializatioTest(pvFloat);

		PVDoubleArray pvDouble = (PVDoubleArray)factory.createPVScalarArray(null, ScalarType.pvDouble);
		pvDouble.put(0, 0, new double[0], 0);
		serializatioTest(pvDouble);
		final double[] dv = new double[] { (double)0.0, (double)1.1, (double)2.3, (double)-1.4, Double.MAX_VALUE, Double.MAX_VALUE - (double)1.6, Double.MIN_VALUE + (double)1.1, Double.MIN_VALUE };		
		pvDouble.put(0, dv.length, dv, 0);
		serializatioTest(pvDouble);

		PVStringArray pvString = (PVStringArray)factory.createPVScalarArray(null, ScalarType.pvString);
		pvString.put(0, 0, new String[0], 0);
		serializatioTest(pvString);
		final String[] strv = new String[] { null, "", "a", "a b", " ", "test", "smile", "this is a little longer string... maybe a little but longer... this makes test better" };		
		pvString.put(0, strv.length, strv, 0);
		serializatioTest(pvString);
		
		// TODO unsigned
	}
	
	public void testIntrospectionSerialization()
	{
        FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        
        // scalars and its arrays
        for (ScalarType s : ScalarType.values())
        {
        	Scalar scalar = fieldCreate.createScalar(s);
        	serializatioTest(scalar);

        	ScalarArray scalarArray = fieldCreate.createScalarArray(s);
        	serializatioTest(scalarArray);
        }
        
        // and a structure
        Structure structure = (Structure)StandardFieldFactory.getStandardField().timeStamp();
        serializatioTest(structure);
        
        // and a structure array
        StructureArray structureArray = fieldCreate.createStructureArray(structure);
        serializatioTest(structureArray);
	}
	
	public void testStructure()
	{
        FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();   
        PVStructure pvStructure = (PVStructure)pvDataCreate.createPVField(null, StandardFieldFactory.getStandardField().timeStamp());
        pvStructure.getLongField("secondsPastEpoch").put(123);
        pvStructure.getIntField("nanoSeconds").put(456);

		serializatioTest(pvStructure);
		serializatioTest(pvStructure.getStructure());
		
		// and more complex :)
        Field[] fields2 = new Field[4];
        fields2[0] = fieldCreate.createScalar(ScalarType.pvLong);
        fields2[1] = fieldCreate.createScalar(ScalarType.pvInt);
        fields2[2] = fieldCreate.createScalarArray(ScalarType.pvDouble);
        fields2[3] = pvStructure.getField();
        PVStructure pvStructure2 = pvDataCreate.createPVStructure(null,
        		fieldCreate.createStructure(new String[] {"longVal", "intVal", "values", "timeStamp"},fields2));
        pvStructure2.getLongField("longVal").put(1234);
        pvStructure2.getIntField("intVal").put(4567);
        PVDoubleArray da = (PVDoubleArray)pvStructure2.getScalarArrayField("values", ScalarType.pvDouble);
        double[] dd = new double[] { 1.2, 3.4, 4.5 };
        da.put(0, dd.length, dd, 0);
        
        PVStructure ps = pvStructure2.getStructureField("timeStamp");
        ps.getLongField("secondsPastEpoch").put(789);
        ps.getIntField("nanoSeconds").put(1011);
        ps.getIntField("userTag").put(-1);

		serializatioTest(pvStructure2);
		serializatioTest(pvStructure2.getStructure());
	}
	
	
	private void serializatioTest(Field field)
	{
		// serialize
		ByteBuffer buffer = ByteBuffer.allocate(1 << 12);
		field.serialize(buffer, flusher);
		
		// deserialize
		buffer.flip();
		
		Field deserializedField = FieldFactory.getFieldCreate().deserialize(buffer, control);
	
		// must equal
		assertEquals("field " + field.toString() + " serialization broken", field, deserializedField);
	}
	
}
