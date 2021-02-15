/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.IntArrayData;
import org.epics.pvdata.pv.PVUIntArray;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.util.array.ArrayUInteger;
import org.epics.util.array.CollectionNumbers;


/**
 * Base class for implementing PVUIntArray.
 * @author mrk
 *
 */
public class BasePVUIntArray extends AbstractPVScalarArray implements PVUIntArray
{
    protected int[] value;

    /**
     * Constructor.
     * @param array The introspection interface.
     */
    public BasePVUIntArray(ScalarArray array)
    {
        super(array);
    }

    @Override
    protected void allocate(int newCapacity) {
    	value = new int[newCapacity];
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
    	value = (int[])array;
    }

    @Override
	protected int putToBuffer(ByteBuffer buffer, SerializableControl control, int offset, int length)
	{
		buffer.asIntBuffer().put(value, offset, length);
		buffer.position(buffer.position() + length*4);
		return length;
	}

    @Override
	protected int getFromBuffer(ByteBuffer buffer, DeserializableControl control, int offset, int length)
	{
		buffer.asIntBuffer().get(value, offset, length);
		buffer.position(buffer.position() + length*4);
		return length;
	}

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVUIntArray#get(int, int, org.epics.pvdata.pv.IntArrayData)
     */
    public int get(int offset, int len, IntArrayData data) {
    	return internalGet(offset, len, data);
    }

    public ArrayUInteger get() {
        return CollectionNumbers.unmodifiableListUInt(value);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVUIntArray#put(int, int, int[], int)
     */
    public int put(int offset, int len, int[] from, int fromOffset) {
    	return internalPut(offset, len, from, fromOffset);
    }


    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVUIntArray#shareData(int[])
     */
    public void shareData(int[] from) {
    	internalShareData(from);
    }

    @Override
    protected boolean valueEquals(Object obj)
    {
		PVUIntArray b = (PVUIntArray)obj;
	    IntArrayData arrayData = new IntArrayData();
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
