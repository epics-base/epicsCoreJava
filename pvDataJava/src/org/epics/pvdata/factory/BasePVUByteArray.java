/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvdata.pv.ByteArrayData;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.PVUByteArray;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.SerializableControl;


/**
 * Base class for implementing PVUByteArray.
 * @author mrk
 *
 */
public class BasePVUByteArray extends AbstractPVScalarArray implements PVUByteArray
{
    protected byte[] value;
    
    /**
     * Constructor.
     * @param array The introspection interface.
     */
    public BasePVUByteArray(ScalarArray array)
    {
        super(array);
    }
    
    @Override
    protected void allocate(int newCapacity) {
    	value = new byte[newCapacity];
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
    	value = (byte[])array;
    }

    @Override
	protected int putToBuffer(ByteBuffer buffer, SerializableControl control, int offset, int length)
	{
		buffer.put(value, offset, length);
		return length;
	}
	
    @Override
	protected int getFromBuffer(ByteBuffer buffer, DeserializableControl control, int offset, int length)
	{
		buffer.get(value, offset, length);
		return length;
	}

    @Override
    public int get(int offset, int len, ByteArrayData data) {
    	return internalGet(offset, len, data);
    }
    
    @Override
    public int put(int offset, int len, byte[] from, int fromOffset) {
    	return internalPut(offset, len, from, fromOffset);
    }


    @Override
    public void shareData(byte[] from) {
    	internalShareData(from);
    }

    @Override
    protected boolean valueEquals(Object obj)
    {
		PVUByteArray b = (PVUByteArray)obj;
	    ByteArrayData arrayData = new ByteArrayData();
    	// NOTE: this assumes entire array set to arrayData
	    b.get(0, b.getLength(), arrayData);
		return Arrays.equals(arrayData.data, value);
    }
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(value);
	}
}
