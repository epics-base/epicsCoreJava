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
    protected PVUnion[] value;

	protected UnionArray unionArray;

    /**
     * Constructor.
     * @param unionArray The Introspection interface.
     */
    public BasePVUnionArray(UnionArray unionArray)
    {
        super(unionArray);
        this.unionArray = unionArray;
    }

    @Override
	public UnionArray getUnionArray() {
		return unionArray;
    }
    
    @Override
    protected void allocate(int newCapacity) {
    	value = new PVUnion[newCapacity];
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
    	value = (PVUnion[])array;
    }

    @Override
    public int get(int offset, int len, UnionArrayData data) {
    	return internalGet(offset, len, data);
    }
    
    @Override
    public int put(int offset, int len, PVUnion[] from, int fromOffset) {
    	
    	// first check if all the PVUnion-s are of the right type
    	Union elementField = unionArray.getUnion();
    	for (PVUnion pvu : from)
    		if (pvu != null && !pvu.getUnion().equals(elementField))
    			throw new IllegalStateException("Element is not a compatible union");
    	
    	return internalPut(offset, len, from, fromOffset);
    }

    @Override
    public void shareData(PVUnion[] from) {
    	internalShareData(from);
    }

    @Override
    protected boolean valueEquals(Object obj)
    {
		PVUnionArray b = (PVUnionArray)obj;
		UnionArrayData arrayData = new UnionArrayData();
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
	
}
