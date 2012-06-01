/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.MessageType;
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
    }
	/* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVArray#setCapacity(int)
     */
    abstract public void setCapacity(int capacity);
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
        if(isMutable && super.isImmutable()) {
            super.message("field is immutable", MessageType.error);
            return;
        }
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
    	if(len==length) return;
        if(super.isImmutable()) {
            super.message("field is immutable", MessageType.error);
            return;
        }
        if(len>capacity) setCapacity(len);
        if(len>capacity) len = capacity;
        length = len;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
     */
    @Override
	public void serialize(ByteBuffer buffer, SerializableControl flusher) {
		serialize(buffer, flusher, 0, -1);
	}
	
}
