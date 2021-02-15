/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvdata.pv.BooleanArrayData;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.PVBooleanArray;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.SerializableControl;


/**
 * Base class for implementing PVBooleanArray.
 * @author mrk
 *
 */
public class BasePVBooleanArray extends AbstractPVScalarArray implements PVBooleanArray
{
    protected boolean[] value;

    /**
     * Constructor.
     * @param array The introspection interface.
     */
    public BasePVBooleanArray(ScalarArray array)
    {
        super(array);
    }

    @Override
    protected void allocate(int newCapacity) {
    	value = new boolean[newCapacity];
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
    	value = (boolean[])array;
    }

    @Override
	protected int putToBuffer(ByteBuffer buffer, SerializableControl control, int offset, int length)
	{
    	final int end = length + offset;
    	for (int i = offset; i < end; i++)
    		buffer.put(value[i] ? (byte)1 : (byte)0);
		return length;
	}

    @Override
	protected int getFromBuffer(ByteBuffer buffer, DeserializableControl control, int offset, int length)
	{
    	final int end = length + offset;
    	for (int i = offset; i < end; i++)
    		value[i] = (buffer.get() == 0) ? false : true;
		return length;
	}

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVBooleanArray#get(int, int, org.epics.pvdata.pv.BooleanArrayData)
     */
    public int get(int offset, int len, BooleanArrayData data) {
    	return internalGet(offset, len, data);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVBooleanArray#put(int, int, boolean[], int)
     */
    public int put(int offset, int len, boolean[] from, int fromOffset) {
    	return internalPut(offset, len, from, fromOffset);
    }


    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVBooleanArray#shareData(boolean[])
     */
    public void shareData(boolean[] from) {
    	internalShareData(from);
    }

    @Override
    protected boolean valueEquals(Object obj)
    {
		PVBooleanArray b = (PVBooleanArray)obj;
	    BooleanArrayData arrayData = new BooleanArrayData();
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
