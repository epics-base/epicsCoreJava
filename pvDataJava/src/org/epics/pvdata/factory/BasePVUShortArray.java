/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.PVUShortArray;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.ShortArrayData;
import org.epics.util.array.ArrayUShort;
import org.epics.util.array.CollectionNumbers;


/**
 * Base class for implementing PVUShortArray.
 * @author mrk
 *
 */
public class BasePVUShortArray extends AbstractPVScalarArray implements PVUShortArray
{
    protected short[] value;

    /**
     * Constructor.
     * @param array The introspection interface.
     */
    public BasePVUShortArray(ScalarArray array)
    {
        super(array);
    }

    @Override
    protected void allocate(int newCapacity) {
    	value = new short[newCapacity];
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
    	value = (short[])array;
    }

    @Override
	protected int putToBuffer(ByteBuffer buffer, SerializableControl control, int offset, int length)
	{
		buffer.asShortBuffer().put(value, offset, length);
		buffer.position(buffer.position() + length*2);
		return length;
	}

    @Override
	protected int getFromBuffer(ByteBuffer buffer, DeserializableControl control, int offset, int length)
	{
		buffer.asShortBuffer().get(value, offset, length);
		buffer.position(buffer.position() + length*2);
		return length;
	}

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVUShortArray#get(int, int, org.epics.pvdata.pv.ShortArrayData)
     */
    public int get(int offset, int len, ShortArrayData data) {
    	return internalGet(offset, len, data);
    }

    public ArrayUShort get() {
        return CollectionNumbers.unmodifiableListUShort(value);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVUShortArray#put(int, int, short[], int)
     */
    public int put(int offset, int len, short[] from, int fromOffset) {
    	return internalPut(offset, len, from, fromOffset);
    }


    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVUShortArray#shareData(short[])
     */
    public void shareData(short[] from) {
    	internalShareData(from);
    }

    @Override
    protected boolean valueEquals(Object obj)
    {
		PVUShortArray b = (PVUShortArray)obj;
	    ShortArrayData arrayData = new ShortArrayData();
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
