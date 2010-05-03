/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.test;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.pv.Array;
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
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.SerializableControl;
import org.epics.pvData.pv.Structure;

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
		
	}
	private static SerializableControl flusher = new SerializableFlushImpl();
	

	private static class DeserializableControlImpl implements DeserializableControl {

		@Override
		public void ensureData(int size) {
			// TODO Auto-generated method stub
			
		}
		
	}
	private static DeserializableControl control = new DeserializableControlImpl();

	public void testScalarEquals()
	{
		PVDataCreate factory = PVDataFactory.getPVDataCreate();
		ScalarType[] types = ScalarType.values();
		for (int i = 0; i < types.length; i++)
		{
			ScalarType scalarType = types[i];
			if(scalarType==ScalarType.pvStructure) break;
			PVScalar scalar1 = factory.createPVScalar(null, types[i].name(), types[i]);
			PVScalar scalar2 = factory.createPVScalar(null, types[i].name(), types[i]);
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
		assertEquals(field.getFullName(), field, deserializedField);
	}

	public void testScalarNonInitialized()
	{
		PVDataCreate factory = PVDataFactory.getPVDataCreate();
		ScalarType[] types = ScalarType.values();
		for (int i = 0; i < types.length; i++)
		{
			PVScalar scalar = factory.createPVScalar(null, types[i].name(), types[i]);
			serializatioTest(scalar);
		}
	}

	public void testScalar()
	{
		PVDataCreate factory = PVDataFactory.getPVDataCreate();
		
		PVBoolean pvBoolean = (PVBoolean)factory.createPVScalar(null, ScalarType.pvBoolean.name(), ScalarType.pvBoolean);
		pvBoolean.put(false);
		serializatioTest(pvBoolean);
		pvBoolean.put(true);
		serializatioTest(pvBoolean);
		
		PVByte pvByte = (PVByte)factory.createPVScalar(null, ScalarType.pvByte.name(), ScalarType.pvByte);
		pvByte.put((byte)0);
		serializatioTest(pvByte);
		pvByte.put((byte)12);
		serializatioTest(pvByte);
		pvByte.put((byte)Byte.MAX_VALUE);
		serializatioTest(pvByte);
		pvByte.put((byte)Byte.MIN_VALUE);
		serializatioTest(pvByte);
		
		PVShort pvShort = (PVShort)factory.createPVScalar(null, ScalarType.pvShort.name(), ScalarType.pvShort);
		pvShort.put((short)0);
		serializatioTest(pvShort);
		pvShort.put((short)123);
		serializatioTest(pvShort);
		pvShort.put((short)Byte.MAX_VALUE);
		serializatioTest(pvShort);
		pvShort.put((short)Byte.MIN_VALUE);
		serializatioTest(pvShort);
		pvShort.put((short)Short.MAX_VALUE);
		serializatioTest(pvShort);
		pvShort.put((short)Short.MIN_VALUE);
		serializatioTest(pvShort);

		PVInt pvInt = (PVInt)factory.createPVScalar(null, ScalarType.pvInt.name(), ScalarType.pvInt);
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

		PVLong pvLong = (PVLong)factory.createPVScalar(null, ScalarType.pvLong.name(), ScalarType.pvLong);
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
	
		PVFloat pvFloat = (PVFloat)factory.createPVScalar(null, ScalarType.pvFloat.name(), ScalarType.pvFloat);
		pvFloat.put(0);
		serializatioTest(pvFloat);
		pvFloat.put((float)12.345);
		serializatioTest(pvFloat);
		pvFloat.put(Float.MAX_VALUE);
		serializatioTest(pvFloat);
		pvFloat.put(Float.MIN_VALUE);
		serializatioTest(pvFloat);

		PVDouble pvDouble = (PVDouble)factory.createPVScalar(null, ScalarType.pvDouble.name(), ScalarType.pvDouble);
		pvDouble.put(0);
		serializatioTest(pvDouble);
		pvDouble.put(12.345);
		serializatioTest(pvDouble);
		pvDouble.put(Double.MAX_VALUE);
		serializatioTest(pvDouble);
		pvDouble.put(Double.MIN_VALUE);
		serializatioTest(pvDouble);

		PVString pvString = (PVString)factory.createPVScalar(null, ScalarType.pvString.name(), ScalarType.pvString);
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
			PVArray array = factory.createPVArray(null, types[i].name() + "Array", types[i]);
			serializatioTest(array);
		}
	}

	
	public void testArray()
	{
		PVDataCreate factory = PVDataFactory.getPVDataCreate();
		
		PVBooleanArray pvBoolean = (PVBooleanArray)factory.createPVArray(null, ScalarType.pvBoolean.name() + "Array", ScalarType.pvBoolean);
		pvBoolean.put(0, 0, new boolean[0], 0);
		serializatioTest(pvBoolean);
		final boolean[] bv = new boolean[] { false, true, false, true, true };
		pvBoolean.put(0, bv.length, bv, 0);
		serializatioTest(pvBoolean);

		PVByteArray pvByte = (PVByteArray)factory.createPVArray(null, ScalarType.pvByte.name() + "Array", ScalarType.pvByte);
		pvByte.put(0, 0, new byte[0], 0);
		serializatioTest(pvByte);
		final byte[] byv = new byte[] { 0, 1, 2, -1, Byte.MAX_VALUE, Byte.MAX_VALUE - 1, Byte.MIN_VALUE + 1, Byte.MIN_VALUE };		
		pvByte.put(0, byv.length, byv, 0);
		serializatioTest(pvByte);

		PVShortArray pvShort = (PVShortArray)factory.createPVArray(null, ScalarType.pvShort.name() + "Array", ScalarType.pvShort);
		pvShort.put(0, 0, new short[0], 0);
		serializatioTest(pvShort);
		final short[] sv = new short[] { 0, 1, 2, -1, Short.MAX_VALUE, Short.MAX_VALUE - 1, Short.MIN_VALUE + 1, Short.MIN_VALUE };		
		pvShort.put(0, sv.length, sv, 0);
		serializatioTest(pvShort);

		PVIntArray pvInt = (PVIntArray)factory.createPVArray(null, ScalarType.pvInt.name() + "Array", ScalarType.pvInt);
		pvInt.put(0, 0, new int[0], 0);
		serializatioTest(pvInt);
		final int[] iv = new int[] { 0, 1, 2, -1, Integer.MAX_VALUE, Integer.MAX_VALUE - 1, Integer.MIN_VALUE + 1, Integer.MIN_VALUE };		
		pvInt.put(0, iv.length, iv, 0);
		serializatioTest(pvInt);

		PVLongArray pvLong = (PVLongArray)factory.createPVArray(null, ScalarType.pvLong.name() + "Array", ScalarType.pvLong);
		pvLong.put(0, 0, new long[0], 0);
		serializatioTest(pvLong);
		final long[] lv = new long[] { 0, 1, 2, -1, Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MIN_VALUE + 1, Long.MIN_VALUE };		
		pvLong.put(0, lv.length, lv, 0);
		serializatioTest(pvLong);
	
		PVFloatArray pvFloat = (PVFloatArray)factory.createPVArray(null, ScalarType.pvFloat.name() + "Array", ScalarType.pvFloat);
		pvFloat.put(0, 0, new float[0], 0);
		serializatioTest(pvFloat);
		final float[] fv = new float[] { (float)0.0, (float)1.1, (float)2.3, (float)-1.4, Float.MAX_VALUE, Float.MAX_VALUE - (float)1.6, Float.MIN_VALUE + (float)1.1, Float.MIN_VALUE };		
		pvFloat.put(0, fv.length, fv, 0);
		serializatioTest(pvFloat);

		PVDoubleArray pvDouble = (PVDoubleArray)factory.createPVArray(null, ScalarType.pvDouble.name() + "Array", ScalarType.pvDouble);
		pvDouble.put(0, 0, new double[0], 0);
		serializatioTest(pvDouble);
		final double[] dv = new double[] { (double)0.0, (double)1.1, (double)2.3, (double)-1.4, Double.MAX_VALUE, Double.MAX_VALUE - (double)1.6, Double.MIN_VALUE + (double)1.1, Double.MIN_VALUE };		
		pvDouble.put(0, dv.length, dv, 0);
		serializatioTest(pvDouble);

		PVStringArray pvString = (PVStringArray)factory.createPVArray(null, ScalarType.pvString.name() + "Array", ScalarType.pvString);
		pvString.put(0, 0, new String[0], 0);
		serializatioTest(pvString);
		final String[] strv = new String[] { null, "", "a", "a b", " ", "test", "smile", "this is a little longer string... maybe a little but longer... this makes test better" };		
		pvString.put(0, strv.length, strv, 0);
		serializatioTest(pvString);
	}
	
	public void testIntrospectionSerialization()
	{
        FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        
        Scalar scalar = fieldCreate.createScalar("scalar", ScalarType.pvDouble);
        serializatioTest(scalar);
		
        Array array = fieldCreate.createArray("array", ScalarType.pvDouble);
        serializatioTest(array);
        
        // only emptry structure basic
        Structure structure = fieldCreate.createStructure("structure", null);
        serializatioTest(structure);
        
	}
	
	public void testStructure()
	{
        FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();        
        Field[] fields = new Field[2];
        fields[0] = fieldCreate.createScalar("secondsSinceEpoch",ScalarType.pvLong);
        fields[1] = fieldCreate.createScalar("nanoSeconds",ScalarType.pvInt);
        PVStructure pvStructure = pvDataCreate.createPVStructure(null,"timeStamp",fields);
        pvStructure.getLongField(fields[0].getFieldName()).put(123);
        pvStructure.getIntField(fields[1].getFieldName()).put(456);

		serializatioTest(pvStructure);
		serializatioTest(pvStructure.getStructure());
		
		// and more complex :)
        Field[] fields2 = new Field[4];
        fields2[0] = fieldCreate.createScalar("longVal",ScalarType.pvLong);
        fields2[1] = fieldCreate.createScalar("intVal",ScalarType.pvInt);
        fields2[2] = fieldCreate.createArray("values",ScalarType.pvDouble);
        fields2[3] = fieldCreate.createStructure("timeStamp",fields);
        PVStructure pvStructure2 = pvDataCreate.createPVStructure(null,"complexStructure",fields2);
        pvStructure2.getLongField(fields2[0].getFieldName()).put(1234);
        pvStructure2.getIntField(fields2[1].getFieldName()).put(4567);
        PVDoubleArray da = (PVDoubleArray)pvStructure2.getArrayField(fields2[2].getFieldName(), ScalarType.pvDouble);
        double[] dd = new double[] { 1.2, 3.4, 4.5 };
        da.put(0, dd.length, dd, 0);
        
        PVStructure ps = pvStructure2.getStructureField(fields2[3].getFieldName());
        ps.getLongField(fields[0].getFieldName()).put(789);
        ps.getIntField(fields[1].getFieldName()).put(1011);

		serializatioTest(pvStructure2);
		serializatioTest(pvStructure2.getStructure());
	}
	
	
	private void serializatioTest(Field field)
	{
		// not implemented... since it is to specific
		/*
		// serialize
		ByteBuffer buffer = ByteBuffer.allocate(1 << 12);
		field.serialize(buffer);
		
		// deserialize
		buffer.flip();
		
		Field deserializedField = BaseStructure.deserializeFromType(field.getType(), buffer);
	
		// must equal
		assertEquals(field.getFieldName(), field, deserializedField);
		*/
	}
	
}
