/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.ByteArrayData;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.PVUByteArray;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.SerializableControl;


/**
 * Base class for implementing PVByteArray.
 * @author mrk
 *
 */
public class BasePVUByteArray extends AbstractPVScalarArray implements PVUByteArray
{
    protected byte[] value;
    private ByteArrayData byteArrayData = new ByteArrayData();
    
    /**
     * Constructor.
     * @param array The Introspection interface.
     */
    public BasePVUByteArray(ScalarArray array)
    {
        super(array);
        value = new byte[capacity];
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.factory.AbstractPVArray#setCapacity(int)
     */
    @Override
    public void setCapacity(int len) {
    	if(capacity==len) return;
        if(!capacityMutable) {
            throw new IllegalArgumentException("capacity is immutable");
        }
        if(length>len) length = len;
        byte[]newarray = new byte[len];
        if(length>0) System.arraycopy(value,0,newarray,0,length);
        value = newarray;
        capacity = len;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVByteArray#get(int, int, org.epics.pvdata.pv.ByteArrayData)
     */
    @Override
    public int get(int offset, int len, ByteArrayData data) {
        int n = len;
        if(offset+len > length) n = Math.max(0, length-offset);;
        data.data = value;
        data.offset = offset;
        return n;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVByteArray#put(int, int, byte[], int)
     */
    @Override
    public int put(int offset, int len, byte[]from, int fromOffset) {
        if(super.isImmutable()) {
            throw new IllegalArgumentException("field is immutable");
        }
        if(from==value) return len;
        if(offset+len > length) {
            int newlength = offset + len;
            if(newlength>capacity) {
                setCapacity(newlength);
                newlength = capacity;
                len = newlength - offset;
                if(len<=0) return 0;
            }
            length = newlength;
        }
        System.arraycopy(from,fromOffset,value,offset,len);
        super.postPut();
        return len;        
    }
	/* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVByteArray#shareData(byte[])
     */
    @Override
    public void shareData(byte[] from) {
        this.value = from;
        super.capacity = from.length;
        super.length = from.length;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.SerializableArray#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl, int, int)
     */
    @Override
	public void serialize(ByteBuffer buffer, SerializableControl flusher, int offset, int count) {
    	// cache
    	final int length = this.length;
    	final byte[] value = this.value;

    	// check bounds
		if (offset < 0) offset = 0;
		else if (offset > length) offset = length;
		if (count < 0) count = length;

		final int maxCount = length - offset;
		if (count > maxCount)
			count = maxCount;
		
		// write
		SerializeHelper.writeSize(count, buffer, flusher);
		final int end = offset + count;
		int i = offset;
		while (true)
		{
        	final int maxIndex = Math.min(end-i, buffer.remaining())+i;
			for (; i < maxIndex; i++)
				buffer.put(value[i]);
			if (i < end)
				flusher.flushSerializeBuffer();
			else
				break;
		}
	}
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
     */
    @Override
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {
		final int size = SerializeHelper.readSize(buffer, control);
		if (size >= 0) {
			// prepare array, if necessary
			if (size > capacity)
				setCapacity(size);
			// retrieve value from the buffer
			int i = 0;
			while (true)
			{
				final int toRead = Math.min(size-i, buffer.remaining());
				buffer.get(value, i, toRead);
				i += toRead;
				if (i < size)
					control.ensureData(1);
				else
					break;
			}
			// set new length
			length = size;
		}
		// TODO null arrays (size == -1) not supported
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// TODO anything else?
		if (obj instanceof PVUByteArray) {
			PVUByteArray b = (PVUByteArray)obj;
			b.get(0, b.getLength(), byteArrayData);
			if(byteArrayData.data==value) return true;
			return Arrays.equals(byteArrayData.data, value);
		}
		else
			return false;
	}
    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(value);
	}
}
