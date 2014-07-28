/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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

    @Override
    public int get(int offset, int len, BooleanArrayData data) {
    	return internalGet(offset, len, data);
    }
    
    @Override
    public int put(int offset, int len, boolean[] from, int fromOffset) {
    	return internalPut(offset, len, from, fromOffset);
    }


    @Override
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
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(value);
	}
}
