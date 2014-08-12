/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.Array;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.Serializable;
import org.epics.pvdata.pv.SerializableControl;

/**
 * Abstract base class for any complex (PVStructure, PVUnion) PVArray field.
 * @author mrk
 */
public abstract class AbstractPVComplexArray extends AbstractPVArray {

	protected AbstractPVComplexArray(Array array) {
        super(array);
    }

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.SerializableArray#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl, int, int)
	 */
	public void serialize(ByteBuffer buffer, SerializableControl flusher, int offset, int count)
	{
    	// cache
    	final int length = getLength();
    	final Serializable[] value = (Serializable[])getValue();
    	
		// check bounds
		if (offset < 0) offset = 0;
		else if (offset > length) offset = length;
		if (count < 0) count = length;

		final int maxCount = length - offset;
		if (count > maxCount)
			count = maxCount;
		
		// write
		if (getArray().getArraySizeType() != Array.ArraySizeType.fixed)
			SerializeHelper.writeSize(count, buffer, flusher);
		
		for (int i = 0; i < count; i++)
		{
			if (buffer.remaining() < 1)
				flusher.flushSerializeBuffer();
			
			Serializable pvComplex = value[i+offset];
			if (pvComplex==null)
			{
				buffer.put((byte)0);
			}
			else
			{
				buffer.put((byte)1);
				pvComplex.serialize(buffer, flusher);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
	 */
	public void deserialize(ByteBuffer buffer, DeserializableControl control)
	{

		// read size
		final int size = (getArray().getArraySizeType() != Array.ArraySizeType.fixed) ?
			SerializeHelper.readSize(buffer, control) :
			getArray().getMaximumCapacity();
			
		if (size >= 0)
		{
			// prepare array, if necessary
			if (size > capacity)
				setCapacity(size);
			
			for (int i = 0; i < size; i++)
			{
				control.ensureData(1);
				byte nullOrNonNull = buffer.get();
				if (nullOrNonNull == 0)
				{
					setAt(i, null);
				}
				else
				{
					Serializable obj = getAt(i);
					if (obj == null)
					{
						obj = createNewInstance();
						setAt(i, obj);
					}
					obj.deserialize(buffer, control);
				}
			}
			length = size;
		}
	}

	protected abstract Serializable getAt(int index);
	protected abstract void setAt(int index, Serializable value);
	protected abstract Serializable createNewInstance();

}
