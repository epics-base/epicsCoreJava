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
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVShortArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.ShortArrayData;


/**
 * Base class for implementing PVShortArray.
 * @author mrk
 *
 */
public class BasePVShortArray extends AbstractPVArray implements PVShortArray
{
    protected short[] value;
    
    /**
     * Constructor.
     * @param parent The parent.
     * @param array The Introspection interface.
     */
    public BasePVShortArray(PVStructure parent,Array array)
    {
        super(parent,array);
        value = new short[capacity];
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
     * @see org.epics.pvData.pv.PVShortArray#shareData(short[])
     */
    @Override
    public void shareData(short[] from) {
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
		final int end = offset + count;
		for (int i = offset; i < end; i++)
			buffer.putShort(value[i]);
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
			for (int i = 0; i < size; i++)
				value[i] = buffer.getShort();
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
		if (obj instanceof PVShortArray) {
			PVShortArray b = (PVShortArray)obj;
			ShortArrayData shortArrayData = new ShortArrayData();
			b.get(0, b.getLength(), shortArrayData);
			if(shortArrayData.data==value) return true;
			return Arrays.equals(shortArrayData.data, value);
		}
		else
			return false;
	}
}