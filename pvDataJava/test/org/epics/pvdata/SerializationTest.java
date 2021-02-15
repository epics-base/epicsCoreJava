/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.factory.StandardFieldFactory;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVArray;
import org.epics.pvdata.pv.PVBoolean;
import org.epics.pvdata.pv.PVBooleanArray;
import org.epics.pvdata.pv.PVByte;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVFloat;
import org.epics.pvdata.pv.PVFloatArray;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVIntArray;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVLongArray;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVShort;
import org.epics.pvdata.pv.PVShortArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStructureArray;
import org.epics.pvdata.pv.PVUByte;
import org.epics.pvdata.pv.PVUInt;
import org.epics.pvdata.pv.PVULong;
import org.epics.pvdata.pv.PVUShort;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.PVUnionArray;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.StructureArray;
import org.epics.pvdata.pv.StructureArrayData;
import org.epics.pvdata.pv.Union;
import org.epics.pvdata.pv.UnionArray;
import org.epics.pvdata.pv.UnionArrayData;

/**
 * JUnit test for PVData serialization.
 * @author mse
 *
 */
public class SerializationTest extends TestCase {

	private static class SerializableFlushImpl implements SerializableControl {
		public void ensureBuffer(int size) {
			// TODO Auto-generated method stub

		}

		public void flushSerializeBuffer() {
			// TODO Auto-generated method stub

		}

		public void alignBuffer(int alignment) {
			// TODO Auto-generated method stub

		}

		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.SerializableControl#cachedSerialize(org.epics.pvdata.pv.Field, java.nio.ByteBuffer)
		 */
		public void cachedSerialize(Field field, ByteBuffer buffer) {
			// no cache
			field.serialize(buffer, this);
		}

	}
	private static SerializableControl flusher = new SerializableFlushImpl();


	private static class DeserializableControlImpl implements DeserializableControl {
		public void ensureData(int size) {
			// TODO Auto-generated method stub

		}

 		public void alignData(int alignment) {
			// TODO Auto-generated method stub

		}

		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.DeserializableControl#cachedDeserialize(java.nio.ByteBuffer)
		 */
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
			PVScalar scalar1 = factory.createPVScalar(types[i]);
			PVScalar scalar2 = factory.createPVScalar(types[i]);
			assertEquals(scalar1, scalar2);
		}
	}

	private void serializationTest(PVField field)
	{
		// serialize
		ByteBuffer buffer = ByteBuffer.allocate(1 << 16);
		field.serialize(buffer, flusher);

		// deserialize
		buffer.flip();

		// create new instance and deserialize
		PVField deserializedField = PVDataFactory.getPVDataCreate().createPVField(field.getField());
		deserializedField.deserialize(buffer, control);

		// must equal
		assertEquals("equals test", field, deserializedField);
		assertEquals("toString equals test", field.toString(), deserializedField.toString());
		assertEquals("deserialization did not read entire serialization buffer", buffer.limit(), buffer.position());
	}

	public void testScalarNonInitialized()
	{
		PVDataCreate factory = PVDataFactory.getPVDataCreate();
		ScalarType[] types = ScalarType.values();
		for (ScalarType type : types) {
			PVScalar scalar = factory.createPVScalar(type);
			serializationTest(scalar);
		}
	}

	public void testScalar()
	{
		PVDataCreate factory = PVDataFactory.getPVDataCreate();

		PVBoolean pvBoolean = (PVBoolean)factory.createPVScalar(ScalarType.pvBoolean);
		pvBoolean.put(false);
		serializationTest(pvBoolean);
		pvBoolean.put(true);
		serializationTest(pvBoolean);

		PVByte pvByte = (PVByte)factory.createPVScalar(ScalarType.pvByte);
		pvByte.put((byte)0);
		serializationTest(pvByte);
		pvByte.put((byte)12);
		serializationTest(pvByte);
		pvByte.put((byte)Byte.MAX_VALUE);
		serializationTest(pvByte);
		pvByte.put((byte)Byte.MIN_VALUE);
		serializationTest(pvByte);

		PVShort pvShort = (PVShort)factory.createPVScalar(ScalarType.pvShort);
		pvShort.put((short)0);
		serializationTest(pvShort);
		pvShort.put((short)1234);
		serializationTest(pvShort);
		pvShort.put((short)Byte.MAX_VALUE);
		serializationTest(pvShort);
		pvShort.put((short)Byte.MIN_VALUE);
		serializationTest(pvShort);
		pvShort.put((short)Short.MAX_VALUE);
		serializationTest(pvShort);
		pvShort.put((short)Short.MIN_VALUE);
		serializationTest(pvShort);

		PVInt pvInt = (PVInt)factory.createPVScalar(ScalarType.pvInt);
		pvInt.put(0);
		serializationTest(pvInt);
		pvInt.put(123456);
		serializationTest(pvInt);
		pvInt.put(Byte.MAX_VALUE);
		serializationTest(pvInt);
		pvInt.put(Byte.MIN_VALUE);
		serializationTest(pvInt);
		pvInt.put(Short.MAX_VALUE);
		serializationTest(pvInt);
		pvInt.put(Short.MIN_VALUE);
		serializationTest(pvInt);
		pvInt.put(Integer.MAX_VALUE);
		serializationTest(pvInt);
		pvInt.put(Integer.MIN_VALUE);
		serializationTest(pvInt);

		PVLong pvLong = (PVLong)factory.createPVScalar(ScalarType.pvLong);
		pvLong.put(0);
		serializationTest(pvLong);
		pvLong.put(12345678901L);
		serializationTest(pvLong);
		pvLong.put(Byte.MAX_VALUE);
		serializationTest(pvLong);
		pvLong.put(Byte.MIN_VALUE);
		serializationTest(pvLong);
		pvLong.put(Short.MAX_VALUE);
		serializationTest(pvLong);
		pvLong.put(Short.MIN_VALUE);
		serializationTest(pvLong);
		pvLong.put(Integer.MAX_VALUE);
		serializationTest(pvLong);
		pvLong.put(Integer.MIN_VALUE);
		serializationTest(pvLong);
		pvLong.put(Long.MAX_VALUE);
		serializationTest(pvLong);
		pvLong.put(Long.MIN_VALUE);
		serializationTest(pvLong);

		PVUByte pvUByte = (PVUByte)factory.createPVScalar(ScalarType.pvUByte);
		pvUByte.put((byte)0);
		serializationTest(pvUByte);
		pvUByte.put((byte)12);
		serializationTest(pvUByte);
		pvUByte.put((byte)-12);
		serializationTest(pvUByte);
		pvUByte.put((byte)Byte.MAX_VALUE);
		serializationTest(pvUByte);
		pvUByte.put((byte)Byte.MIN_VALUE);
		serializationTest(pvUByte);

		PVUShort pvUShort = (PVUShort)factory.createPVScalar(ScalarType.pvUShort);
		pvUShort.put((short)0);
		serializationTest(pvUShort);
		pvUShort.put((short)1234);
		serializationTest(pvUShort);
		pvUShort.put((short)-1234);
		serializationTest(pvUShort);
		pvUShort.put((short)Byte.MAX_VALUE);
		serializationTest(pvUShort);
		pvUShort.put((short)Byte.MIN_VALUE);
		serializationTest(pvUShort);
		pvUShort.put((short)Short.MAX_VALUE);
		serializationTest(pvUShort);
		pvUShort.put((short)Short.MIN_VALUE);
		serializationTest(pvUShort);

		PVUInt pvUInt = (PVUInt)factory.createPVScalar(ScalarType.pvUInt);
		pvUInt.put(0);
		serializationTest(pvUInt);
		pvUInt.put(123456);
		serializationTest(pvUInt);
		pvUInt.put(-123456);
		serializationTest(pvUInt);
		pvUInt.put(Byte.MAX_VALUE);
		serializationTest(pvUInt);
		pvUInt.put(Byte.MIN_VALUE);
		serializationTest(pvUInt);
		pvUInt.put(Short.MAX_VALUE);
		serializationTest(pvUInt);
		pvUInt.put(Short.MIN_VALUE);
		serializationTest(pvUInt);
		pvUInt.put(Integer.MAX_VALUE);
		serializationTest(pvUInt);
		pvUInt.put(Integer.MIN_VALUE);
		serializationTest(pvUInt);

		PVULong pvULong = (PVULong)factory.createPVScalar(ScalarType.pvULong);
		pvULong.put(0);
		serializationTest(pvULong);
		pvULong.put(12345678901L);
		serializationTest(pvULong);
		pvULong.put(-12345678901L);
		serializationTest(pvULong);
		pvULong.put(Byte.MAX_VALUE);
		serializationTest(pvULong);
		pvULong.put(Byte.MIN_VALUE);
		serializationTest(pvULong);
		pvULong.put(Short.MAX_VALUE);
		serializationTest(pvULong);
		pvULong.put(Short.MIN_VALUE);
		serializationTest(pvULong);
		pvULong.put(Integer.MAX_VALUE);
		serializationTest(pvULong);
		pvULong.put(Integer.MIN_VALUE);
		serializationTest(pvULong);
		pvULong.put(Long.MAX_VALUE);
		serializationTest(pvULong);
		pvULong.put(Long.MIN_VALUE);
		serializationTest(pvULong);

		PVFloat pvFloat = (PVFloat)factory.createPVScalar(ScalarType.pvFloat);
		pvFloat.put(0);
		serializationTest(pvFloat);
		pvFloat.put((float)12.345);
		serializationTest(pvFloat);
		pvFloat.put(Float.MAX_VALUE);
		serializationTest(pvFloat);
		pvFloat.put(Float.MIN_VALUE);
		serializationTest(pvFloat);
		//pvFloat.put(Float.NaN);
		//serializatioTest(pvFloat);
		pvFloat.put(Float.POSITIVE_INFINITY);
		serializationTest(pvFloat);
		pvFloat.put(Float.NEGATIVE_INFINITY);
		serializationTest(pvFloat);

		PVDouble pvDouble = (PVDouble)factory.createPVScalar(ScalarType.pvDouble);
		pvDouble.put(0);
		serializationTest(pvDouble);
		pvDouble.put(12.345);
		serializationTest(pvDouble);
		pvDouble.put(Double.MAX_VALUE);
		serializationTest(pvDouble);
		pvDouble.put(Double.MIN_VALUE);
		serializationTest(pvDouble);
		//pvDouble.put(Double.NaN);
		//serializatioTest(pvDouble);
		pvDouble.put(Double.POSITIVE_INFINITY);
		serializationTest(pvDouble);
		pvDouble.put(Double.NEGATIVE_INFINITY);
		serializationTest(pvDouble);

		PVString pvString = (PVString)factory.createPVScalar(ScalarType.pvString);
		pvString.put("");
		serializationTest(pvString);
		pvString.put("s");
		serializationTest(pvString);
		pvString.put("string");
		serializationTest(pvString);
		pvString.put("string with spaces");
		serializationTest(pvString);
		pvString.put("string with spaces and special characters\f\n");
		serializationTest(pvString);

		// huge string test
		StringBuffer strBuf = new StringBuffer(10000);
		for (int i = 0; i < strBuf.capacity(); i++)
			strBuf.append('a');
		pvString.put(strBuf.toString());
		serializationTest(pvString);
	}

	public void testArrayNonInitialized()
	{
		PVDataCreate factory = PVDataFactory.getPVDataCreate();
		ScalarType[] types = ScalarType.values();
		for (int i = 0; i < types.length; i++)
		{
			PVArray array = factory.createPVScalarArray(types[i]);
			serializationTest(array);
		}
	}

	public void testArraySizeTypes()
	{
        FieldCreate fieldCreate = FieldFactory.getFieldCreate();
		PVDataCreate factory = PVDataFactory.getPVDataCreate();

		Structure s = fieldCreate.createFieldBuilder().
			addArray("array", ScalarType.pvDouble).
			addFixedArray("fixedArray", ScalarType.pvDouble, 10).
			addBoundedArray("boundedArray", ScalarType.pvDouble, 1024).
			add("scalar", ScalarType.pvDouble).
			createStructure();
		assertNotNull(s);
		assertEquals(Structure.DEFAULT_ID, s.getID());
		assertEquals(4, s.getFields().length);

		serializationTest(s);
		serializationTest(factory.createPVField(s));
	}

	public void testBoundedString()
	{
        FieldCreate fieldCreate = FieldFactory.getFieldCreate();
		PVDataCreate factory = PVDataFactory.getPVDataCreate();

		Structure s = fieldCreate.createFieldBuilder().
			add("str", ScalarType.pvString).
			addBoundedString("boundedStr", 8).
			add("scalar", ScalarType.pvDouble).
			createStructure();
		assertNotNull(s);
		assertEquals(Structure.DEFAULT_ID, s.getID());
		assertEquals(3, s.getFields().length);

		serializationTest(s);
		PVStructure pvs = factory.createPVStructure(s);
		serializationTest(pvs);

		PVString pvStr = pvs.getStringField("boundedStr");
		pvStr.put("");
		pvStr.put("small");
		pvStr.put("exact123");

		try {
			pvStr.put("tooLargeString");
			fail("too large string accepted");
		} catch (IllegalArgumentException iae) {
			// OK
		}
	}

	public void testArray()
	{
		PVDataCreate factory = PVDataFactory.getPVDataCreate();

		PVBooleanArray pvBoolean = (PVBooleanArray)factory.createPVScalarArray(ScalarType.pvBoolean);
		pvBoolean.put(0, 0, new boolean[0], 0);
		serializationTest(pvBoolean);
		final boolean[] bv = new boolean[] { false, true, false, true, true };
		pvBoolean.put(0, bv.length, bv, 0);
		serializationTest(pvBoolean);

		PVByteArray pvByte = (PVByteArray)factory.createPVScalarArray(ScalarType.pvByte);
		pvByte.put(0, 0, new byte[0], 0);
		serializationTest(pvByte);
		final byte[] byv = new byte[] { 0, 1, 2, -1, Byte.MAX_VALUE, Byte.MAX_VALUE - 1, Byte.MIN_VALUE + 1, Byte.MIN_VALUE };
		pvByte.put(0, byv.length, byv, 0);
		serializationTest(pvByte);

		PVShortArray pvShort = (PVShortArray)factory.createPVScalarArray(ScalarType.pvShort);
		pvShort.put(0, 0, new short[0], 0);
		serializationTest(pvShort);
		final short[] sv = new short[] { 0, 1, 2, -1, Short.MAX_VALUE, Short.MAX_VALUE - 1, Short.MIN_VALUE + 1, Short.MIN_VALUE };
		pvShort.put(0, sv.length, sv, 0);
		serializationTest(pvShort);

		PVIntArray pvInt = (PVIntArray)factory.createPVScalarArray(ScalarType.pvInt);
		pvInt.put(0, 0, new int[0], 0);
		serializationTest(pvInt);
		final int[] iv = new int[] { 0, 1, 2, -1, Integer.MAX_VALUE, Integer.MAX_VALUE - 1, Integer.MIN_VALUE + 1, Integer.MIN_VALUE };
		pvInt.put(0, iv.length, iv, 0);
		serializationTest(pvInt);

		PVLongArray pvLong = (PVLongArray)factory.createPVScalarArray(ScalarType.pvLong);
		pvLong.put(0, 0, new long[0], 0);
		serializationTest(pvLong);
		final long[] lv = new long[] { 0, 1, 2, -1, Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MIN_VALUE + 1, Long.MIN_VALUE };
		pvLong.put(0, lv.length, lv, 0);
		serializationTest(pvLong);

		PVFloatArray pvFloat = (PVFloatArray)factory.createPVScalarArray(ScalarType.pvFloat);
		pvFloat.put(0, 0, new float[0], 0);
		serializationTest(pvFloat);
		final float[] fv = new float[] { (float)0.0, (float)1.1, (float)2.3, (float)-1.4, Float.MAX_VALUE, Float.MAX_VALUE - (float)1.6, Float.MIN_VALUE + (float)1.1, Float.MIN_VALUE };
		pvFloat.put(0, fv.length, fv, 0);
		serializationTest(pvFloat);

		PVDoubleArray pvDouble = (PVDoubleArray)factory.createPVScalarArray(ScalarType.pvDouble);
		pvDouble.put(0, 0, new double[0], 0);
		serializationTest(pvDouble);
		final double[] dv = new double[] { (double)0.0, (double)1.1, (double)2.3, (double)-1.4, Double.MAX_VALUE, Double.MAX_VALUE - (double)1.6, Double.MIN_VALUE + (double)1.1, Double.MIN_VALUE };
		pvDouble.put(0, dv.length, dv, 0);
		serializationTest(pvDouble);

		PVStringArray pvString = (PVStringArray)factory.createPVScalarArray(ScalarType.pvString);
		pvString.put(0, 0, new String[0], 0);
		serializationTest(pvString);
		final String[] strv = new String[] { null, "", "a", "a b", " ", "test", "smile", "this is a little longer string... maybe a little but longer... this makes test better" };
		pvString.put(0, strv.length, strv, 0);
		serializationTest(pvString);

		// TODO unsigned
	}

	public void testIntrospectionSerialization()
	{
        FieldCreate fieldCreate = FieldFactory.getFieldCreate();

        // scalars and its arrays
        for (ScalarType s : ScalarType.values())
        {
        	Scalar scalar = fieldCreate.createScalar(s);
        	serializationTest(scalar);

        	ScalarArray scalarArray = fieldCreate.createScalarArray(s);
        	serializationTest(scalarArray);
        }

        // and a structure
        Structure structure = (Structure)StandardFieldFactory.getStandardField().timeStamp();
        serializationTest(structure);

        // and a structure array
        StructureArray structureArray = fieldCreate.createStructureArray(structure);
        serializationTest(structureArray);
	}

	public void testStructure()
	{
        FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
        PVStructure pvStructure = (PVStructure)pvDataCreate.createPVField(StandardFieldFactory.getStandardField().timeStamp());
        pvStructure.getLongField("secondsPastEpoch").put(123);
        pvStructure.getIntField("nanoseconds").put(456);

		serializationTest(pvStructure);
		serializationTest(pvStructure.getStructure());

		// and more complex :)
        Field[] fields2 = new Field[4];
        fields2[0] = fieldCreate.createScalar(ScalarType.pvLong);
        fields2[1] = fieldCreate.createScalar(ScalarType.pvInt);
        fields2[2] = fieldCreate.createScalarArray(ScalarType.pvDouble);
        fields2[3] = pvStructure.getField();
        PVStructure pvStructure2 = pvDataCreate.createPVStructure(fieldCreate.createStructure(new String[] {"longVal", "intVal", "values", "timeStamp"},fields2));
        pvStructure2.getLongField("longVal").put(1234);
        pvStructure2.getIntField("intVal").put(4567);
        PVDoubleArray da = (PVDoubleArray)pvStructure2.getScalarArrayField("values", ScalarType.pvDouble);
        double[] dd = new double[] { 1.2, 3.4, 4.5 };
        da.put(0, dd.length, dd, 0);

        PVStructure ps = pvStructure2.getStructureField("timeStamp");
        ps.getLongField("secondsPastEpoch").put(789);
        ps.getIntField("nanoseconds").put(1011);
        ps.getIntField("userTag").put(-1);

		serializationTest(pvStructure2);
		serializationTest(pvStructure2.getStructure());
	}

	public void testStructureID()
	{
        FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

        String[] fieldNames = new String[] { "longField", "intField" };
        Field[] fields = new Field[]
            {
        		fieldCreate.createScalar(ScalarType.pvLong),
        		fieldCreate.createScalar(ScalarType.pvInt)
            };
        Structure structureWithNoId = fieldCreate.createStructure(fieldNames, fields);
        Structure structure1 = fieldCreate.createStructure("id1", fieldNames, fields);
        Structure structure2 = fieldCreate.createStructure("id2", fieldNames, fields);

        assertFalse(structureWithNoId.equals(structure1));
        assertFalse(structure1.equals(structure2));

        serializationTest(structure1);

        PVStructure pvStructure = (PVStructure)pvDataCreate.createPVField(structure1);
		serializationTest(pvStructure);
	}

	public void testStructureArray()
	{
        FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
        StructureArray sarray = fieldCreate.createStructureArray(StandardFieldFactory.getStandardField().timeStamp());
        PVStructureArray pvStructureArray = (PVStructureArray)pvDataCreate.createPVField(sarray);
        pvStructureArray.setLength(4);
        StructureArrayData sad = new StructureArrayData();
        pvStructureArray.get(0, 3, sad);	// leave one null

        for (int i = 0; i < 3; i++)
        {
        	PVStructure pvStructure = pvDataCreate.createPVStructure(sarray.getStructure());
        	pvStructure.getLongField("secondsPastEpoch").put(123*i);
        	pvStructure.getIntField("nanoseconds").put(456*i);
        	sad.data[i] = pvStructure;
        }

		serializationTest(pvStructureArray);
		serializationTest(pvStructureArray.getStructureArray());
	}

	public void testVariantUnion()
	{
        PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

        // null variant union test
        PVUnion variant = pvDataCreate.createPVVariantUnion();
        assertNull(variant.get());
        assertEquals(PVUnion.UNDEFINED_INDEX, variant.getSelectedIndex());
        assertNull(variant.getSelectedFieldName());
        serializationTest(variant.getUnion());
        serializationTest(variant);

        PVDouble doubleValue = (PVDouble)pvDataCreate.createPVScalar(ScalarType.pvDouble);
        PVInt intValue = (PVInt)pvDataCreate.createPVScalar(ScalarType.pvInt);

        //
        // variant union tests
        //

        variant.set(doubleValue);
        assertSame(doubleValue, variant.get());
        assertEquals(PVUnion.UNDEFINED_INDEX, variant.getSelectedIndex());
        assertNull(variant.getSelectedFieldName());
        serializationTest(variant.getUnion());
        serializationTest(variant);

        variant.set(intValue);
        assertSame(intValue, variant.get());
        assertEquals(PVUnion.UNDEFINED_INDEX, variant.getSelectedIndex());
        assertNull(variant.getSelectedFieldName());
        serializationTest(variant.getUnion());
        serializationTest(variant);

        variant.set(PVUnion.UNDEFINED_INDEX, doubleValue);
        assertSame(doubleValue, variant.get());
        assertEquals(PVUnion.UNDEFINED_INDEX, variant.getSelectedIndex());

        variant.set(null);
        assertNull(variant.get());
        serializationTest(variant.getUnion());
        serializationTest(variant);
	}

	public void testVariantUnionArray()
	{
        PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

        // null variant union array test
        PVUnionArray variantArray = pvDataCreate.createPVVariantUnionArray();
        serializationTest(variantArray.getUnionArray());
        serializationTest(variantArray);

        variantArray.setLength(6);
        UnionArrayData uad = new UnionArrayData();
        variantArray.get(0, 5, uad);	// we leave one undefined

        for (int i = 0; i < 5; i++)
        {
        	PVUnion union = pvDataCreate.createPVVariantUnion();
        	if (i % 3 == 0)
        	{
        		// noop (null variant union test)
        	}
        	else
        	{
            	PVStructure pvStructure;
	        	if (i % 3 == 1)
	        	{
		        	pvStructure = pvDataCreate.createPVStructure(StandardFieldFactory.getStandardField().timeStamp());
	        	}
	        	else
	        	{
		        	pvStructure = pvDataCreate.createPVStructure(StandardFieldFactory.getStandardField().control());
	        	}

	        	union.set(pvStructure);
        	}

        	uad.data[i] = union;
        }

        serializationTest(variantArray);
	}

	public void testUnion()
	{
        FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

        Field[] fields = new Field[2];
		fields[0] = fieldCreate.createScalar(ScalarType.pvDouble);
		fields[1] = fieldCreate.createScalar(ScalarType.pvInt);
		String[] fieldNames = new String[2];
		fieldNames[0] = "doubleValue";
		fieldNames[1] = "intValue";
		PVUnion union = pvDataCreate.createPVUnion(fieldCreate.createUnion(fieldNames, fields));
		assertNotNull(union);

		// null union test
        assertNull(union.get());
        assertEquals(PVUnion.UNDEFINED_INDEX, union.getSelectedIndex());
        assertNull(union.getSelectedFieldName());
        serializationTest(union.getUnion());
        serializationTest(union);

        ((PVDouble)union.select(fieldNames[0])).put(12.3);
        assertEquals(12.3, ((PVDouble)union.get()).get());
        assertEquals(0, union.getSelectedIndex());
        assertEquals(fieldNames[0], union.getSelectedFieldName());
        serializationTest(union);

        ((PVInt)union.select(fieldNames[1])).put(543);
        assertEquals(543, ((PVInt)union.get()).get());
        assertEquals(1, union.getSelectedIndex());
        assertEquals(fieldNames[1], union.getSelectedFieldName());
        serializationTest(union);

        ((PVInt)union.select(1)).put(5432);
        assertEquals(5432, ((PVInt)union.get()).get());
        serializationTest(union);

        assertNull(union.select(PVUnion.UNDEFINED_INDEX));
        assertNull(union.get());
        assertEquals(PVUnion.UNDEFINED_INDEX, union.getSelectedIndex());
        assertNull(union.getSelectedFieldName());
        serializationTest(union);

        PVDouble doubleValue = (PVDouble)pvDataCreate.createPVScalar(ScalarType.pvDouble);
        union.set(0, doubleValue);
        assertSame(doubleValue, union.get());
        assertEquals(0, union.getSelectedIndex());

        try
        {
        	union.set(1, doubleValue);
        	fail("field type does not match, but set allowed");
        }
        catch (IllegalArgumentException iae)
        {
        	// expected
        }

        try
        {
        	union.select(120);
        	fail("index out of bounds allowed");
        }
        catch (IllegalArgumentException iae)
        {
        	// expected
        }

        try
        {
        	union.select(-2);
        	fail("index out of bounds allowed");
        }
        catch (IllegalArgumentException iae)
        {
        	// expected
        }

        try
        {
        	union.set(120, doubleValue);
        	fail("index out of bounds allowed");
        }
        catch (IllegalArgumentException iae)
        {
        	// expected
        }
	}

	public void testUnionArray()
	{
        FieldCreate fieldCreate = FieldFactory.getFieldCreate();
        PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();

        Field[] fields = new Field[2];
		fields[0] = fieldCreate.createScalar(ScalarType.pvDouble);
		fields[1] = fieldCreate.createScalar(ScalarType.pvInt);
		String[] fieldNames = new String[2];
		fieldNames[0] = "doubleValue";
		fieldNames[1] = "intValue";
		Union union = fieldCreate.createUnion(fieldNames, fields);
		UnionArray unionArray = fieldCreate.createUnionArray(union);

        // null union array test
        PVUnionArray pvUnionArray = pvDataCreate.createPVUnionArray(unionArray);
        serializationTest(pvUnionArray.getUnionArray());
        serializationTest(pvUnionArray);

        pvUnionArray.setLength(6);
        UnionArrayData uad = new UnionArrayData();
        pvUnionArray.get(0, 5, uad);	// we leave one undefined


        for (int i = 0; i < 5; i++)
        {
        	PVUnion pvUnion = pvDataCreate.createPVUnion(union);

        	if (i % 3 == 0)
        	{
        		// noop (null union test)
        	}
        	else
        	{
	        	if (i % 3 == 1)
	        	{
	        		((PVDouble)pvUnion.select(0)).put(i + 0.1);
	        	}
	        	else
	        	{
	        		((PVInt)pvUnion.select(1)).put(i);
	        	}
        	}

        	uad.data[i] = pvUnion;
        }

        serializationTest(pvUnionArray);
	}

	private void serializationTest(Field field)
	{
		// serialize
		ByteBuffer buffer = ByteBuffer.allocate(1 << 12);
		field.serialize(buffer, flusher);

		// deserialize
		buffer.flip();

		Field deserializedField = FieldFactory.getFieldCreate().deserialize(buffer, control);

		// must equal
		assertEquals("field " + field.toString() + " serialization broken", field, deserializedField);
		assertEquals("field " + field.toString() + " equals test", field.toString(), deserializedField.toString());
		assertEquals("deserialization did not read entire serialization buffer", buffer.limit(), buffer.position());
	}

}
