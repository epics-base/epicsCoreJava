/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvData.misc.SerializeHelper;
import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.PVUShortArray;
import org.epics.pvData.pv.ScalarArray;
import org.epics.pvData.pv.SerializableControl;
import org.epics.pvData.pv.ShortArrayData;


/**
 * Base class for implementing PVShortArray.
 * @author mrk
 *
 */
public class BasePVUShortArray extends AbstractPVScalarArray implements PVUShortArray
{
    protected short[] value;
    
    /**
     * Constructor.
     * @param parent The parent.
     * @param array The Introspection interface.
     */
    public BasePVUShortArray(PVStructure parent,ScalarArray array)
    {
        super(parent,array);
        value = new short[capacity];
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVArray#setCapacity(int)
     */
    @Override
    public void setCapacity(int len) {
    	if(capacity==len) return;
        if(!capacityMutable) {
            super.message("not capacityMutable", MessageType.error);
            return;
        }
        if(length>len) length = len;
        short[]newarray = new short[len];
        if(length>0) System.arraycopy(value,0,newarray,0,length);
        value = newarray;
        capacity = len;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVShortArray#get(int, int, org.epics.pvData.pv.ShortArrayData)
     */
    @Override
    public int get(int offset, int len, ShortArrayData data) { 
        int n = len;
        if(offset+len > length) n = Math.max(0, length-offset);
        data.data = value;
        data.offset = offset;
        return n;      
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVShortArray#put(int, int, short[], int)
     */
    @Override
    public int put(int offset, int len, short[]from, int fromOffset) {
        if(super.isImmutable()) {
            super.message("field is immutable", MessageType.error);
            return 0;
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
     * @see org.epics.pvData.pv.PVShortArray#shareData(short[])
     */
    @Override
    public void shareData(short[] from) {
        this.value = from;
        super.capacity = from.length;
        super.length = from.length;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.SerializableArray#serialize(java.nio.ByteBuffer, org.epics.pvData.pv.SerializableControl, int, int)
     */
    @Override
	public void serialize(ByteBuffer buffer, SerializableControl flusher, int offset, int count) {
    	// cache
    	final int length = this.length;
    	final short[] value = this.value;

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
        	final int maxIndex = Math.min(end-i, buffer.remaining()/(Short.SIZE/Byte.SIZE))+i;
			for (; i < maxIndex; i++)
				buffer.putShort(value[i]);
			if (i < end)
				flusher.flushSerializeBuffer();
			else
				break;
		}
	}
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvData.pv.DeserializableControl)
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
				final int maxIndex = Math.min(size-i, buffer.remaining()/(Short.SIZE/Byte.SIZE))+i;
				for (; i < maxIndex; i++)
					value[i] = buffer.getShort();
				if (i < size)
					control.ensureData(Short.SIZE/Byte.SIZE);
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
		if (obj instanceof PVUShortArray) {
			PVUShortArray b = (PVUShortArray)obj;
			ShortArrayData shortArrayData = new ShortArrayData();
			b.get(0, b.getLength(), shortArrayData);
			if(shortArrayData.data==value) return true;
			return Arrays.equals(shortArrayData.data, value);
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