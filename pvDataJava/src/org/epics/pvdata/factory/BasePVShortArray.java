/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.PVShortArray;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.ShortArrayData;


/**
 * Base class for implementing PVShortArray.
 * @author mrk
 *
 */
public class BasePVShortArray extends AbstractPVScalarArray implements PVShortArray
{
    protected short[] value;
    
    /**
     * Constructor.
     * @param array The introspection interface.
     */
    public BasePVShortArray(ScalarArray array)
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
     * @see org.epics.pvdata.pv.PVShortArray#get(int, int, org.epics.pvdata.pv.ShortArrayData)
     */
    @Override
    public int get(int offset, int len, ShortArrayData data) {
    	return internalGet(offset, len, data);
    }
    
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVShortArray#put(int, int, short[], int)
     */
    @Override
    public int put(int offset, int len, short[] from, int fromOffset) {
    	return internalPut(offset, len, from, fromOffset);
    }


    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVShortArray#shareData(short[])
     */
    @Override
    public void shareData(short[] from) {
    	internalShareData(from);
    }

    @Override
    protected boolean valueEquals(Object obj)
    {
		PVShortArray b = (PVShortArray)obj;
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
