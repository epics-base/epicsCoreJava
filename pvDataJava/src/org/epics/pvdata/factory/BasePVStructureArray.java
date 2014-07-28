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
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStructureArray;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.StructureArray;
import org.epics.pvdata.pv.StructureArrayData;


/**
 * Base class for implementing PVDoubleArray.
 * @author mrk
 *
 */
public class BasePVStructureArray extends AbstractPVArray implements PVStructureArray
{
	private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    protected PVStructure[] value;

    protected StructureArray structureArray;
    
    /**
     * Constructor.
     * @param structureArray The Introspection interface.
     */
    public BasePVStructureArray(StructureArray structureArray)
    {
        super(structureArray);
        this.structureArray = structureArray;
    }

    @Override
	public StructureArray getStructureArray() {
		return structureArray;
    }

    @Override
    protected void allocate(int newCapacity) {
    	value = new PVStructure[newCapacity];
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
    	value = (PVStructure[])array;
    }

    @Override
    public int get(int offset, int len, StructureArrayData data) {
    	return internalGet(offset, len, data);
    }
    
    @Override
    public int put(int offset, int len, PVStructure[] from, int fromOffset) {
    	
    	// first check if all the PVStructure-s are of the right type
    	Structure elementField = structureArray.getStructure();
    	for (PVStructure pvs : from)
    		if (pvs != null && !pvs.getStructure().equals(elementField))
    			throw new IllegalStateException("Element is not a compatible structure");
    	
    	return internalPut(offset, len, from, fromOffset);
    }


    @Override
    public void shareData(PVStructure[] from) {
    	internalShareData(from);
    }

    @Override
    protected boolean valueEquals(Object obj)
    {
		PVStructureArray b = (PVStructureArray)obj;
		StructureArrayData arrayData = new StructureArrayData();
    	// NOTE: this assumes entire array set to arrayData
	    b.get(0, b.getLength(), arrayData);
		return Arrays.equals(arrayData.data, value);
    }
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(value);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.SerializableArray#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl, int, int)
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
						value[i] = pvDataCreate.createPVStructure(structureArray.getStructure());
					}
					value[i].deserialize(buffer, control);
				}
			}
			length = size;
		}
	}
}
