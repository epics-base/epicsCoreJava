/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.FloatArrayData;
import org.epics.pvdata.pv.PVFloatArray;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.util.array.ArrayFloat;
import org.epics.util.array.CollectionNumbers;


/**
 * Base class for implementing PVFloatArray.
 * @author mrk
 *
 */
public class BasePVFloatArray extends AbstractPVScalarArray implements PVFloatArray
{
    protected float[] value;

    /**
     * Constructor.
     * @param array The introspection interface.
     */
    public BasePVFloatArray(ScalarArray array)
    {
        super(array);
    }

    @Override
    protected void allocate(int newCapacity) {
    	value = new float[newCapacity];
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
    	value = (float[])array;
    }

    @Override
	protected int putToBuffer(ByteBuffer buffer, SerializableControl control, int offset, int length)
	{
		buffer.asFloatBuffer().put(value, offset, length);
		buffer.position(buffer.position() + length*4);
		return length;
	}

    @Override
	protected int getFromBuffer(ByteBuffer buffer, DeserializableControl control, int offset, int length)
	{
		buffer.asFloatBuffer().get(value, offset, length);
		buffer.position(buffer.position() + length*4);
		return length;
	}

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVFloatArray#get(int, int, org.epics.pvdata.pv.FloatArrayData)
     */
    public int get(int offset, int len, FloatArrayData data) {
    	return internalGet(offset, len, data);
    }

    public ArrayFloat get() {
        return CollectionNumbers.unmodifiableListFloat(value);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVFloatArray#put(int, int, float[], int)
     */
    public int put(int offset, int len, float[] from, int fromOffset) {
    	return internalPut(offset, len, from, fromOffset);
    }


    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVFloatArray#shareData(float[])
     */
    public void shareData(float[] from) {
    	internalShareData(from);
    }

    @Override
    protected boolean valueEquals(Object obj)
    {
		PVFloatArray b = (PVFloatArray)obj;
	    FloatArrayData arrayData = new FloatArrayData();
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
