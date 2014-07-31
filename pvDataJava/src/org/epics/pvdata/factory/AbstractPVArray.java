/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.pv.Array;
import org.epics.pvdata.pv.ArrayData;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.PVArray;
import org.epics.pvdata.pv.SerializableControl;

/**
 * Abstract base class for any PVArray field.
 * Any code that implements a PVArray field for an IOC database should extend this class.
 * @author mrk
 *
 */
public abstract class AbstractPVArray extends AbstractPVField implements PVArray{
    /**
     * For use by derived classes.
     */
    protected int length = 0;
    /**
     * For use by derived classes.
     */
    protected int capacity = 0;
    /**
     * For use by derived classes.
     */
    protected boolean capacityMutable = true;
    /**
     * Constructor that derived classes must call.
     * @param field The reflection interface.
     */
    protected AbstractPVArray(Field field) {
        super(field);
        
        // if array is a fixed-size type
        // make array fixed-size and capacity immutable
        if (getArray().getArraySizeType() == Array.ArraySizeType.fixed)
        {
        	capacity = length = getArray().getMaximumCapacity();
        	setCapacityMutable(false);
        }

        allocate(capacity);
    }
    
	@Override
	public Array getArray() {
		return (Array)getField();
	}
	
	protected abstract Object getValue();
    protected abstract void setValue(Object array);
	protected abstract void allocate(int newCapacity);
    protected abstract boolean valueEquals(Object obj);
	
	/* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVArray#setCapacity(int)
     */
    public void setCapacity(int newCapacity)
    {
    	if (newCapacity == capacity) return;

    	if (!capacityMutable)
    		throw new IllegalStateException("not capacityMutable");
    	
    	checkLength(newCapacity);
        
        Object oldValue = getValue();
        allocate(newCapacity);

        if (length > newCapacity)
        	length = newCapacity;
        
        if (length > 0)
        	System.arraycopy(oldValue, 0, getValue(), 0, length);
    }
    
    protected int internalGet(int offset, int len, ArrayData<?> data) {
        int n = len;
        if (offset+len > length)
        	n = Math.max(0, length - offset);
        data.set(getValue(), offset);
        return n;
    }

    protected int internalPut(int offset, int len, Object from, int fromOffset) {

    	if (super.isImmutable())
        	throw new IllegalStateException("field is immutable");
 
    	Object value = getValue();
        if (from == value)
        	return len;
        
        int newLength = offset + len;
        if (newLength > length)
        {
        	checkLength(newLength);
        	setCapacity(newLength);
        	value = getValue();
        	length = newLength;
        }
        
        System.arraycopy(from, fromOffset, value, offset, len);
        super.postPut();
        return len;      
    }

    private void checkLength(int len)
    {
    	Array.ArraySizeType type = getArray().getArraySizeType();
    	if (type != Array.ArraySizeType.variable)
    	{
    		int size = getArray().getMaximumCapacity();
	    	if (type == Array.ArraySizeType.fixed && len != size)
	    		throw new IllegalArgumentException("invalid length for a fixed size array");
	    	else if (type == Array.ArraySizeType.bounded && len > size)
	    		throw new IllegalArgumentException("new array capacity too large for a bounded size array");
    	}
    }
    
    protected void internalShareData(Object from) {
    	int len = java.lang.reflect.Array.getLength(from);

    	checkLength(len);
    	
    	setValue(from);
    	capacity = length = len;
    }

    /* (non-Javadoc)
     * @see org.epics.pvdata.factory.AbstractPVField#setImmutable()
     */
    @Override
    public void setImmutable() {
        capacityMutable = false;
        super.setImmutable();
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVArray#isCapacityMutable()
     */
    @Override
    public boolean isCapacityMutable() {
        return capacityMutable;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVArray#setCapacityMutable(boolean)
     */
    @Override
    public void setCapacityMutable(boolean isMutable) {
        if (isMutable && super.isImmutable())
        	throw new IllegalStateException("field is immutable");
        
        capacityMutable = isMutable;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVArray#getCapacity()
     */
    @Override
    public int getCapacity() {
        return capacity;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVArray#getLength()
     */
    @Override
    public int getLength() {
        return length;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVArray#setLength(int)
     */
    @Override
    public void setLength(int len) {
    	if (len == length)
    		return;
        
    	if (super.isImmutable())
        	throw new IllegalStateException("field is immutable");

    	checkLength(len);
    	
        if (len > capacity)
        	setCapacity(len);
        
        length = len;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
     */
    @Override
	public void serialize(ByteBuffer buffer, SerializableControl flusher) {
		serialize(buffer, flusher, 0, -1);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (this == obj)
			return true;
		
		if (obj instanceof PVArray)
		{
			final PVArray other = (PVArray)obj;
			if (other.getField().equals(getField()))
			{
				// check length, check capacity (done by valueEquals) and check value content
				return other.getLength() == getLength() &&
					   valueEquals(obj);
			}
		}
		
		return false;
	}
    
}
