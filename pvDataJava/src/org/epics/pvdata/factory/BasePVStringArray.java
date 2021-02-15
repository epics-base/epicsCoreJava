/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.StringArrayData;


/**
 * Base class for implementing PVStringArray.
 * @author mrk
 *
 */
public class BasePVStringArray extends AbstractPVScalarArray implements PVStringArray
{
    protected String[] value;

    /**
     * Constructor.
     * @param array The introspection interface.
     */
    public BasePVStringArray(ScalarArray array)
    {
        super(array);
    }

    @Override
    protected void allocate(int newCapacity) {
    	value = new String[newCapacity];
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
    	value = (String[])array;
    }

    @Override
	protected int putToBuffer(ByteBuffer buffer, SerializableControl control, int offset, int length)
	{
    	final int end = length + offset;
    	for (int i = offset; i < end; i++)
    		SerializeHelper.serializeString(value[i], buffer, control);
		return length;
	}

    @Override
	protected int getFromBuffer(ByteBuffer buffer, DeserializableControl control, int offset, int length)
	{
    	final int end = length + offset;
    	for (int i = offset; i < end; i++)
    		value[i] = SerializeHelper.deserializeString(buffer, control);
		return length;
	}

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStringArray#get(int, int, org.epics.pvdata.pv.StringArrayData)
     */
    public int get(int offset, int len, StringArrayData data) {
    	return internalGet(offset, len, data);
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStringArray#put(int, int, java.lang.String[], int)
     */
    public int put(int offset, int len, String[] from, int fromOffset) {
    	return internalPut(offset, len, from, fromOffset);
    }


    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVStringArray#shareData(java.lang.String[])
     */
    public void shareData(String[] from) {
    	internalShareData(from);
    }

    @Override
    protected boolean valueEquals(Object obj)
    {
		PVStringArray b = (PVStringArray)obj;
	    StringArrayData arrayData = new StringArrayData();
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
