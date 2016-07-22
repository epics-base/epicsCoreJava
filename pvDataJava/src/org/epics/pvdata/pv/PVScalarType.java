/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;


/**
 * PV scalar and scalar array type metadata class.
 * @author mse
 */
public class PVScalarType<T extends PVScalar, TA extends PVScalarArray> {

	public static final PVScalarType<PVBoolean, PVBooleanArray> pvBoolean =
    	new PVScalarType<PVBoolean, PVBooleanArray>(ScalarType.pvBoolean,
    			PVBoolean.class, PVBooleanArray.class);

	public static final PVScalarType<PVByte, PVByteArray> pvByte =
    	new PVScalarType<PVByte, PVByteArray>(ScalarType.pvByte,
    			PVByte.class, PVByteArray.class);

	public static final PVScalarType<PVShort, PVShortArray> pvShort =
    	new PVScalarType<PVShort, PVShortArray>(ScalarType.pvShort,
    			PVShort.class, PVShortArray.class);

	public static final PVScalarType<PVInt, PVIntArray> pvInt =
    	new PVScalarType<PVInt, PVIntArray>(ScalarType.pvInt,
    			PVInt.class, PVIntArray.class);

	public static final PVScalarType<PVLong, PVLongArray> pvLong =
    	new PVScalarType<PVLong, PVLongArray>(ScalarType.pvLong,
    			PVLong.class, PVLongArray.class);

	public static final PVScalarType<PVUByte, PVUByteArray> pvUByte =
    	new PVScalarType<PVUByte, PVUByteArray>(ScalarType.pvUByte,
    			PVUByte.class, PVUByteArray.class);

	public static final PVScalarType<PVUShort, PVUShortArray> pvUShort =
    	new PVScalarType<PVUShort, PVUShortArray>(ScalarType.pvUShort,
    			PVUShort.class, PVUShortArray.class);

	public static final PVScalarType<PVUInt, PVUIntArray> pvUInt =
    	new PVScalarType<PVUInt, PVUIntArray>(ScalarType.pvUInt,
    			PVUInt.class, PVUIntArray.class);

	public static final PVScalarType<PVULong, PVULongArray> pvULong =
    	new PVScalarType<PVULong, PVULongArray>(ScalarType.pvULong,
    			PVULong.class, PVULongArray.class);
	
	public static final PVScalarType<PVFloat, PVFloatArray> pvFloat =
    	new PVScalarType<PVFloat, PVFloatArray>(ScalarType.pvFloat,
    			PVFloat.class, PVFloatArray.class);
	
	public static final PVScalarType<PVDouble, PVDoubleArray> pvDouble =
    	new PVScalarType<PVDouble, PVDoubleArray>(ScalarType.pvDouble,
    			PVDouble.class, PVDoubleArray.class);

	public static final PVScalarType<PVString, PVStringArray> pvString =
    	new PVScalarType<PVString, PVStringArray>(ScalarType.pvString,
    			PVString.class, PVStringArray.class);
	
	private final ScalarType scalarType;
	private final Class<T> pvFieldClass;
	private final Class<TA> pvFieldArrayClass;
	
	protected PVScalarType(ScalarType scalarType,
			Class<T> pvFieldClass, Class<TA> pvFieldArrayClass)
	{
		this.scalarType = scalarType;
		this.pvFieldClass = pvFieldClass;
		this.pvFieldArrayClass = pvFieldArrayClass;
	}

	public ScalarType getScalarType() {
		return scalarType;
	}

	public Class<T> getPVFieldClass() {
		return pvFieldClass;
	}

	public Class<TA> getPVFieldArrayClass() {
		return pvFieldArrayClass;
	}
}

