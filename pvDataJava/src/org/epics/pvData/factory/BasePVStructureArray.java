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
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.PVStructureArray;
import org.epics.pvData.pv.SerializableControl;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.StructureArray;
import org.epics.pvData.pv.StructureArrayData;


/**
 * Base class for implementing PVDoubleArray.
 * @author mrk
 *
 */
public class BasePVStructureArray  extends AbstractPVArray implements PVStructureArray
{
	private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
	protected StructureArray structureArray;
    protected PVStructure[] value;
    private StructureArrayData structureArrayData = new StructureArrayData();
    
    /**
     * Constructor.
     * @param parent The parent.
     * @param structureArray The Introspection interface.
     */
    public BasePVStructureArray(PVStructure parent,StructureArray structureArray)
    {
        super(parent,structureArray);
        this.structureArray = structureArray;
        value = new PVStructure[capacity];
    }
    @Override
	public StructureArray getStructureArray() {
		return structureArray;
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
        if(length==len) return;
        PVStructure[]newarray = new PVStructure[len];
        int num = length;
        if(len<length) num = len;
        for(int i=0; i<num; i++) newarray[i] = value[i];
        for(int i=num; i<len; i++) newarray[i] = null;
        value = newarray;
        capacity = len;
    }       
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructureArray#get(int, int, org.epics.pvData.pv.StructureArrayData)
     */
    @Override
    public int get(int offset, int len, StructureArrayData data) {
        int n = len;
        if(offset+len > length) n = Math.max(0, length-offset);
        data.data = value;
        data.offset = offset;
        return n;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVStructureArray#put(int, int, org.epics.pvData.pv.PVStructure[], int)
     */
    @Override
    public int put(int offset, int len, PVStructure[]from, int fromOffset) {
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
        Structure structure = structureArray.getStructure();
        for(int i=0; i<len; i++) {
        	PVStructure frompv = from[i+fromOffset];
        	if(frompv==null) {
        		value[i+offset] = null;
        		continue;
        	}
        	if(frompv.getStructure()!=structure) {
        		throw new IllegalStateException("Element is not a compatible structure");
        	}
        	value[i+offset] = frompv;
        }
        super.postPut();
        return len;      
    }     
	/* (non-Javadoc)
     * @see org.epics.pvData.pv.PVDoubleArray#shareData(double[])
     */
    @Override
    public void shareData(PVStructure[] from) {
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
    	final PVStructure[] value = this.value;
    	
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
			PVStructure pvStructure = value[i+offset];
			if(pvStructure==null) {
				buffer.put((byte)0);
			} else {
				buffer.put((byte)1);
				pvStructure.serialize(buffer, flusher);
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvData.pv.DeserializableControl)
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
						value[i] = pvDataCreate.createPVStructure(null,structureArray.getStructure());
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
		if (obj instanceof PVStructureArray) {
			PVStructureArray b = (PVStructureArray)obj;
			if(b.getStructureArray()!=structureArray) return false;
			int len = b.get(0, b.getLength(), structureArrayData);
			if(len!=length) return false;
			PVStructure[]data = structureArrayData.data;
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