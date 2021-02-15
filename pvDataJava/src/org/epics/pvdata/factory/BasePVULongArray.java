/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.LongArrayData;
import org.epics.pvdata.pv.PVULongArray;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.util.array.ArrayULong;
import org.epics.util.array.CollectionNumbers;


/**
 * Base class for implementing PVULongArray.
 * @author mrk
 *
 */
public class BasePVULongArray extends AbstractPVScalarArray implements PVULongArray
{
    protected long[] value;

    /**
     * Constructor.
     * @param array The introspection interface.
     */
    public BasePVULongArray(ScalarArray array)
    {
        super(array);
    }

    @Override
    protected void allocate(int newCapacity) {
    	value = new long[newCapacity];
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
    	value = (long[])array;
    }

    @Override
	protected int putToBuffer(ByteBuffer buffer, SerializableControl control, int offset, int length)
	{
		buffer.asLongBuffer().put(value, offset, length);
		buffer.position(buffer.position() + length*8);
		return length;
	}

    @Override
	protected int getFromBuffer(ByteBuffer buffer, DeserializableControl control, int offset, int length)
	{
		buffer.asLongBuffer().get(value, offset, length);
		buffer.position(buffer.position() + length*8);
		return length;
	}

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVULongArray#get(int, int, org.epics.pvdata.pv.LongArrayData)
     */
    public int get(int offset, int len, LongArrayData data) {
    	return internalGet(offset, len, data);
    }

    public ArrayULong get() {
        return CollectionNumbers.unmodifiableListULong(value);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVULongArray#put(int, int, long[], int)
     */
    public int put(int offset, int len, long[] from, int fromOffset) {
    	return internalPut(offset, len, from, fromOffset);
    }


    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVULongArray#shareData(long[])
     */
    public void shareData(long[] from) {
    	internalShareData(from);
    }

    @Override
    protected boolean valueEquals(Object obj)
    {
		PVULongArray b = (PVULongArray)obj;
	    LongArrayData arrayData = new LongArrayData();
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
