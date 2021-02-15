/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.DoubleArrayData;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.util.array.ArrayDouble;
import org.epics.util.array.CollectionNumbers;
import org.epics.util.array.ListNumber;


/**
 * Base class for implementing PVDoubleArray.
 * @author mrk
 *
 */
public class BasePVDoubleArray extends AbstractPVScalarArray implements PVDoubleArray
{
    protected double[] value;

    /**
     * Constructor.
     * @param array The introspection interface.
     */
    public BasePVDoubleArray(ScalarArray array)
    {
        super(array);
    }

    @Override
    protected void allocate(int newCapacity) {
    	value = new double[newCapacity];
    	capacity = newCapacity;
    }

    @Override
    protected Object getValue()
    {
    	return value;
    }

    @Override
    protected void setValue(Object array)
    {
    	value = (double[])array;
    }

    @Override
	protected int putToBuffer(ByteBuffer buffer, SerializableControl control, int offset, int length)
	{
		buffer.asDoubleBuffer().put(value, offset, length);
		buffer.position(buffer.position() + length*8);
		return length;
	}

    @Override
	protected int getFromBuffer(ByteBuffer buffer, DeserializableControl control, int offset, int length)
	{
    	buffer.asDoubleBuffer().get(value, offset, length).position();
		buffer.position(buffer.position() + length*8);
		return length;
	}

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVDoubleArray#get(int, int, org.epics.pvdata.pv.DoubleArrayData)
     */
    public int get(int offset, int len, DoubleArrayData data) {
    	return internalGet(offset, len, data);
    }

    public ArrayDouble get() {
        return CollectionNumbers.unmodifiableListDouble(value);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVDoubleArray#put(int, int, double[], int)
     */
    public int put(int offset, int len, double[] from, int fromOffset) {
    	return internalPut(offset, len, from, fromOffset);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVDoubleArray#shareData(double[])
     */
    public void shareData(double[] from) {
    	internalShareData(from);
    }

    @Override
    protected boolean valueEquals(Object obj)
    {
		PVDoubleArray b = (PVDoubleArray)obj;
	    DoubleArrayData arrayData = new DoubleArrayData();
    	// NOTE: this assumes entire array set to arrayData
	    b.get(0, b.getLength(), arrayData);
		return Arrays.equals(arrayData.data, value);
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(value);
	}
}
