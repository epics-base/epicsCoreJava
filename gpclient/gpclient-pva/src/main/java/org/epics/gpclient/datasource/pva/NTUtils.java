/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.pva;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.pv.*;
import org.epics.util.array.*;
import org.epics.vtype.*;

import java.util.Arrays;

final class NTUtils {

	private static final Class<?>[] classLUT = {
		boolean.class, // pvBoolean
		byte.class,    // pvByte
		short.class,   // pvShort
		int.class,     // pvInt
		long.class,    // pvLong
		byte.class,   // pvUByte
		short.class,  // pvUShort
		int.class,    // pvUInt
		long.class,   // pvULong
		float.class,   // pvFloat
		double.class,  // pvDouble
		String.class   // pvString
	};

	public static Class<?> scalarClass(ScalarType scalarType)
	{
		return classLUT[scalarType.ordinal()];
	}

	public static Class<?> scalarArrayElementClass(PVScalarArray scalarArray)
	{
		return scalarClass(scalarArray.getScalarArray().getElementType());
	}


	private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();

	public static Field vtypeToField(Class<?> vtypeClass)
	{
	    if (vtypeClass == null)
	      throw new IllegalArgumentException("vtypeClass == null");

	    // TODO no complex types

	    if (vtypeClass.isAssignableFrom(VDouble.class)) {
	      return fieldCreate.createScalar(ScalarType.pvDouble);
	    } else if (vtypeClass.isAssignableFrom(VFloat.class)) {
	      return fieldCreate.createScalar(ScalarType.pvFloat);
	    } else if (vtypeClass.isAssignableFrom(VString.class)) {
	      return fieldCreate.createScalar(ScalarType.pvString);
	    } else if (vtypeClass.isAssignableFrom(VInt.class)) {
	      return fieldCreate.createScalar(ScalarType.pvInt);
	    } else if (vtypeClass.isAssignableFrom(VShort.class)) {
	      return fieldCreate.createScalar(ScalarType.pvShort);
	    } else if (vtypeClass.isAssignableFrom(VLong.class)) {
	      return fieldCreate.createScalar(ScalarType.pvLong);
	    } else if (vtypeClass.isAssignableFrom(VByte.class)) {
	      return fieldCreate.createScalar(ScalarType.pvByte);
	    } else if (vtypeClass.isAssignableFrom(VBoolean.class)) {
	      return fieldCreate.createScalar(ScalarType.pvBoolean);

	    } else if (vtypeClass.isAssignableFrom(VDoubleArray.class)) {
	      return fieldCreate.createScalarArray(ScalarType.pvDouble);
	    } else if (vtypeClass.isAssignableFrom(VFloatArray.class)) {
	      return fieldCreate.createScalarArray(ScalarType.pvFloat);
//	    } else if (vtypeClass.isAssignableFrom(VStringArray.class)) {
//	      return fieldCreate.createScalarArray(ScalarType.pvString);
	    } else if (vtypeClass.isAssignableFrom(VIntArray.class)) {
	      return fieldCreate.createScalarArray(ScalarType.pvInt);
	    } else if (vtypeClass.isAssignableFrom(VLongArray.class)) {
	      return fieldCreate.createScalarArray(ScalarType.pvLong);
	    } else if (vtypeClass.isAssignableFrom(VShortArray.class)) {
	      return fieldCreate.createScalarArray(ScalarType.pvShort);
	    } else if (vtypeClass.isAssignableFrom(VByteArray.class)) {
	      return fieldCreate.createScalarArray(ScalarType.pvByte);
	    }

	    throw new IllegalArgumentException("V-type class " + vtypeClass.getSimpleName() + " not supported");
	}

	public static Object scalarArrayToList(PVScalarArray scalarArray, boolean readOnly)
	{
            // FIXME: This should all go away
            // FIXME: Should extract unsigned properly
            if (!readOnly) {
                throw new RuntimeException("Modifiable arrays are not supported by this function");
            }
    	int len = scalarArray.getLength();
		ScalarType elementType = scalarArray.getScalarArray().getElementType();
		switch (elementType)
		{
		case pvDouble:
		{
        	DoubleArrayData data = new DoubleArrayData();
        	((PVDoubleArray)scalarArray).get(0, len, data);
        	return ArrayDouble.of(data.data);
		}
		case pvFloat:
		{
        	FloatArrayData data = new FloatArrayData();
        	((PVFloatArray)scalarArray).get(0, len, data);
        	return ArrayFloat.of(data.data);
		}
		case pvInt:
		case pvUInt:
		{
        	IntArrayData data = new IntArrayData();
        	if (elementType == ScalarType.pvInt)
        		((PVIntArray)scalarArray).get(0, len, data);
        	else
        		((PVUIntArray)scalarArray).get(0, len, data);
        	return ArrayInteger.of(data.data);
		}
		case pvString:
		{
        	StringArrayData data = new StringArrayData();
        	((PVStringArray)scalarArray).get(0, len, data);
        	return Arrays.asList(data.data);
		}
		case pvLong:
		case pvULong:
		{
        	LongArrayData data = new LongArrayData();
        	if (elementType == ScalarType.pvLong)
        		((PVLongArray)scalarArray).get(0, len, data);
        	else
        		((PVULongArray)scalarArray).get(0, len, data);
        	return ArrayLong.of(data.data);
		}
		case pvShort:
		case pvUShort:
		{
        	ShortArrayData data = new ShortArrayData();
        	if (elementType == ScalarType.pvShort)
        		((PVShortArray)scalarArray).get(0, len, data);
        	else
        		((PVUShortArray)scalarArray).get(0, len, data);
        	return ArrayShort.of(data.data);
		}
		case pvByte:
		case pvUByte:
		{
        	ByteArrayData data = new ByteArrayData();
        	if (elementType == ScalarType.pvByte)
        		((PVByteArray)scalarArray).get(0, len, data);
        	else
        		((PVUByteArray)scalarArray).get(0, len, data);
        	return ArrayByte.of(data.data);
		}
		case pvBoolean:
		{
        	BooleanArrayData data = new BooleanArrayData();
        	((PVBooleanArray)scalarArray).get(0, len, data);
        	return new ArrayBoolean(data.data, readOnly);
		}
		default:
			throw new IllegalArgumentException("unsupported scalar array element type: " + elementType);
		}
	}
}
