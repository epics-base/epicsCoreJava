/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.Array;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.SerializableControl;

/**
 * Abstract base class for any scalar PVArray field.
 * @author mrk
 */
public abstract class AbstractPVScalarArray extends AbstractPVArray implements PVScalarArray {

	protected AbstractPVScalarArray(ScalarArray array) {
        super(array);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVArray#getArray()
     */
    public ScalarArray getScalarArray() {
        return (ScalarArray)getField();
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.SerializableArray#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl, int, int)
     */
	public void serialize(ByteBuffer buffer, SerializableControl flusher, int offset, int count) {
    	// check bounds
		if (offset < 0) offset = 0;
		else if (offset > length) offset = length;
		if (count < 0) count = length;

		final int maxCount = length - offset;
		if (count > maxCount)
			count = maxCount;

		// write size
		if (getArray().getArraySizeType() != Array.ArraySizeType.fixed)
			SerializeHelper.writeSize(count, buffer, flusher);
		else if (count != getArray().getMaximumCapacity())
			throw new IllegalStateException("fixed array cannot be partially serialized");

		// write elements
		final int elementSize = getElementSize();
		if (elementSize <= 0)
			putToBuffer(buffer, flusher, offset, count);
		else
		{
			final int end = offset + count;
			int i = offset;
			while (true)
			{
	        	final int n = Math.min(end-i, buffer.remaining()/elementSize);
	        	i += putToBuffer(buffer, flusher, i, n);
				if (i < end)
					flusher.flushSerializeBuffer();
				else
					break;
			}
		}
	}

    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
     */
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {

    	// read size
		final int size = (getArray().getArraySizeType() != Array.ArraySizeType.fixed) ?
			SerializeHelper.readSize(buffer, control) :
			getArray().getMaximumCapacity();

		if (size >= 0) {
			// prepare array, if necessary
			if (size > capacity)
				setCapacity(size);
			// retrieve value from the buffer
			final int elementSize = getElementSize();
			if (elementSize <= 0)
				getFromBuffer(buffer, control, 0, size);
			else
			{
				int i = 0;
				while (true)
				{
					final int n = Math.min(size-i, buffer.remaining()/elementSize);
					i += getFromBuffer(buffer, control, i, n);
					if (i < size)
						control.ensureData(elementSize);
					else
						break;
				}
			}
			// set new length
			length = size;
		}
		// TODO null arrays (size == -1) not supported
	}

    private static final int[] elementSizeLUT =
    {
		1, // pvBoolean
		1, // pvByte
		2, // pvShort
		4, // pvInt
		8, // pvLong
		1, // pvUByte
		2, // pvUShort
		4, // pvUInt
		8, // pvULong
		4, // pvFloat
		8, // pvDouble
		-1  // pvString
	};

    protected final int getElementSize()
    {
    	return elementSizeLUT[getScalarArray().getElementType().ordinal()];
    }

	protected abstract int putToBuffer(ByteBuffer buffer, SerializableControl control, int offset, int length);
	protected abstract int getFromBuffer(ByteBuffer buffer, DeserializableControl control, int offset, int length);
}
