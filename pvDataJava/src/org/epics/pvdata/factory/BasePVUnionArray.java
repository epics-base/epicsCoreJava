/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.PVUnionArray;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.Union;
import org.epics.pvdata.pv.UnionArray;
import org.epics.pvdata.pv.UnionArrayData;


/**
 * Base class for implementing PVDoubleArray.
 * @author mse
 *
 */
public class BasePVUnionArray  extends AbstractPVArray implements PVUnionArray
{
	private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
	protected UnionArray unionArray;
    protected PVUnion[] value;
    private UnionArrayData unionArrayData = new UnionArrayData();
    
    /**
     * Constructor.
     * @param unionArray The Introspection interface.
     */
    public BasePVUnionArray(UnionArray unionArray)
    {
        super(unionArray);
        this.unionArray = unionArray;
        value = new PVUnion[capacity];
    }
    @Override
	public UnionArray getUnionArray() {
		return unionArray;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.factory.AbstractPVArray#setCapacity(int)
     */
    @Override
    public void setCapacity(int len) {
    	if(capacity==len) return;
        if(!capacityMutable) {
            super.message("not capacityMutable", MessageType.error);
            return;
        }
        if(length==len) return;
        PVUnion[] newarray = new PVUnion[len];
        int num = length;
        if(len<length) num = len;
        for(int i=0; i<num; i++) newarray[i] = value[i];
        for(int i=num; i<len; i++) newarray[i] = null;
        value = newarray;
        capacity = len;
    }       
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVUnionArray#get(int, int, org.epics.pvdata.pv.UnionArrayData)
     */
    @Override
    public int get(int offset, int len, UnionArrayData data) {
        int n = len;
        if(offset+len > length) n = Math.max(0, length-offset);
        data.data = value;
        data.offset = offset;
        return n;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVUnionArray#put(int, int, org.epics.pvdata.pv.PVUnion[], int)
     */
    @Override
    public int put(int offset, int len, PVUnion[]from, int fromOffset) {
        if(super.isImmutable()) {
            super.message("field is immutable", MessageType.error);
            return 0;
        }
        if(from==value) return len;
        if(len<1) return 0;
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
        Union union = unionArray.getUnion();
        for(int i=0; i<len; i++) {
        	PVUnion frompv = from[i+fromOffset];
        	if(frompv==null) {
        		value[i+offset] = null;
        		continue;
        	}
        	if(frompv.getUnion()!=union) {
        		throw new IllegalStateException("Element is not a compatible union");
        	}
        	value[i+offset] = frompv;
        }
        super.postPut();
        return len;      
    }     
	/* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVDoubleArray#shareData(double[])
     */
    @Override
    public void shareData(PVUnion[] from) {
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
    	final PVUnion[] value = this.value;
    	
		// check bounds
		if (offset < 0) offset = 0;
		else if (offset > length) offset = length;
		if (count < 0) count = length;

		final int maxCount = length - offset;
		if (count > maxCount)
			count = maxCount;
		
		// write
		SerializeHelper.writeSize(count, buffer, flusher);
		for(int i=0; i<count; i++) {
			if(buffer.remaining()<1) flusher.flushSerializeBuffer();
			PVUnion pvUnion = value[i+offset];
			if(pvUnion==null) {					// TODO !!!
				buffer.put((byte)0);
			} else {
				buffer.put((byte)1);
				pvUnion.serialize(buffer, flusher);
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
	 */
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {
		final int size = SerializeHelper.readSize(buffer, control);
		if (size >= 0) {
			// prepare array, if necessary
			if (size > capacity)
				setCapacity(size);
			for(int i=0; i<size; i++) {
				control.ensureData(1);
				byte temp = buffer.get();
				if(temp==0) {
					value[i] = null;
				} else {
					if(value[i]==null) {
						value[i] = pvDataCreate.createPVUnion(unionArray.getUnion());
					}
					value[i].deserialize(buffer, control);
				}
			}
			length = size;
		}
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// TODO anything else?
		if (obj instanceof PVUnionArray) {
			PVUnionArray b = (PVUnionArray)obj;
			if(b.getUnionArray()!=unionArray) return false;
			int len = b.get(0, b.getLength(), unionArrayData);
			if(len!=length) return false;
			PVUnion[]data = unionArrayData.data;
			if(data==value) return true;
			for(int i=0; i<length; i++) {
				if(data[i]!=null) {
					//just check object NOT contents
					if(value[i]==null) return false;
					if(!data[i].equals(value[i])) return false;
				} else {
					if(value[i]!=null) return false; 
				}
			}
			return true;
		}  else {
			return false;
		}
	}
    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(value);
	}
}
