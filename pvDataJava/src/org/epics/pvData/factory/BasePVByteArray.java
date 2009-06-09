/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvData.misc.SerializeHelper;
import org.epics.pvData.pv.Array;
import org.epics.pvData.pv.ByteArrayData;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVByteArray;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVStructure;


/**
 * Base class for implementing PVByteArray.
 * @author mrk
 *
 */
public class BasePVByteArray extends AbstractPVArray implements PVByteArray
{
    protected byte[] value;
    private ByteArrayData byteArrayData = new ByteArrayData();
    
    /**
     * Constructor.
     * @param parent The parent.
     * @param array The Introspection interface.
     */
    public BasePVByteArray(PVStructure parent,Array array)
    {
        super(parent,array);
        value = new byte[capacity];
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVField#toString(int)
     */
    @Override
    public String toString(int indentLevel) {
        return convert.getString(this, indentLevel)
        + super.toString(indentLevel);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVArray#setCapacity(int)
     */
    @Override
    public void setCapacity(int len) {
        if(!capacityMutable) {
            super.message("not capacityMutable", MessageType.error);
            return;
        }
        if(length>len) length = len;
        byte[]newarray = new byte[len];
        if(length>0) System.arraycopy(value,0,newarray,0,length);
        value = newarray;
        capacity = len;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVByteArray#get(int, int, org.epics.pvData.pv.ByteArrayData)
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
     * @see org.epics.pvData.pv.PVByteArray#put(int, int, byte[], int)
     */
    @Override
    public int put(int offset, int len, byte[]from, int fromOffset) {
        if(super.isImmutable()) {
            super.message("field is immutable", MessageType.error);
            return 0;
        }
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
        PVRecord pvRecord = super.getPVRecord();
        if(pvRecord!=null) pvRecord.beginGroupPut();
        System.arraycopy(from,fromOffset,value,offset,len);
        super.postPut();
        if(pvRecord!=null) pvRecord.endGroupPut();
        return len;        
    }
	/* (non-Javadoc)
     * @see org.epics.pvData.pv.PVByteArray#shareData(byte[])
     */
    @Override
    public void shareData(byte[] from) {
        this.value = from;
        super.capacity = from.length;
        super.length = from.length;
    }
    /* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer, int, int)
	 */
    @Override
	public void serialize(ByteBuffer buffer, int offset, int count) {
		// check bounds
		if (offset < 0) offset = 0;
		else if (offset > length) offset = length;
		if (count < 0) count = length;

		final int maxCount = length - offset;
		if (count > maxCount)
			count = maxCount;
		
		// write
		SerializeHelper.writeSize(count, buffer);
		if (offset < length)
			buffer.put(value, offset, count);
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
	 */
    @Override
	public void deserialize(ByteBuffer buffer) {
		final int size = SerializeHelper.readSize(buffer);
		if (size >= 0) {
			// prepare array, if necessary
			if (size > capacity)
				setCapacity(size);
			// retrieve value from the buffer
			buffer.get(value, 0, size);
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
		if (obj instanceof PVByteArray) {
			PVByteArray b = (PVByteArray)obj;
			b.get(0, b.getLength(), byteArrayData);
			if(byteArrayData.data==value) return true;
			return Arrays.equals(byteArrayData.data, value);
		}
		else
			return false;
	}
}