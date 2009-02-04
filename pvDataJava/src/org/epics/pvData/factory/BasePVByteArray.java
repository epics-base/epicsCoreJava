/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvData.pv.Array;
import org.epics.pvData.pv.ByteArrayData;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVByteArray;
import org.epics.pvData.pv.PVStructure;


/**
 * Base class for implementing PVByteArray.
 * @author mrk
 *
 */
public class BasePVByteArray extends AbstractPVArray implements PVByteArray
{
    protected byte[] value;
    protected ByteArrayData byteArrayData = new ByteArrayData();
    
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
    public String toString(int indentLevel) {
        return convert.getString(this, indentLevel)
        + super.toString(indentLevel);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVArray#setCapacity(int)
     */
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
    public int get(int offset, int len, ByteArrayData data) {
        int n = len;
        if(offset+len > length) n = length - offset;
        data.data = value;
        data.offset = offset;
        return n;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVByteArray#put(int, int, byte[], int)
     */
    public int put(int offset, int len, byte[]from, int fromOffset) {
        if(!super.isMutable()) {
            super.message("not isMutable", MessageType.error);
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
        System.arraycopy(from,fromOffset,value,offset,len);
        super.postPut();
        return len;        
    }
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#getSerializationSize()
	 */
	public int getSerializationSize() {
		return getSerializedSizeSize(length) + length;
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
	 */
	public void serialize(ByteBuffer buffer) {
		writeSize(length, buffer);
		buffer.put(value, 0, length);
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
	 */
	public void deserialize(ByteBuffer buffer) {
		final int size = readSize(buffer);
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
			ByteArrayData bad = new ByteArrayData();
			b.get(0, b.getLength(), bad);
			return Arrays.equals(bad.data, value);
		}
		else
			return false;
	}
}