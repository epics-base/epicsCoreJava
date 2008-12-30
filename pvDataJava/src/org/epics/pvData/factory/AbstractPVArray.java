/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import org.epics.pvData.pv.Array;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVStructure;

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
     * @param parent The parent interface.
     * @param array The reflection interface for the PVArray data.
     */
    protected AbstractPVArray(PVStructure parent,Array array) {
        super(parent,array);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVArray#setCapacity(int)
     */
    abstract public void setCapacity(int capacity);
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVArray#getArray()
     */
    public Array getArray() {
        return (Array)getField();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVArray#isCapacityMutable()
     */
    public boolean isCapacityMutable() {
        return capacityMutable;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVArray#setCapacityMutable(boolean)
     */
    public void setCapacityMutable(boolean isMutable) {
        capacityMutable = isMutable;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVArray#getCapacity()
     */
    public int getCapacity() {
        return capacity;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVArray#getLength()
     */
    public int getLength() {
        return length;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVArray#setLength(int)
     */
    public void setLength(int len) {
        if(!super.isMutable())
            throw new IllegalStateException("PVField.isMutable is false");
        if(len>capacity) setCapacity(len);
        length = len;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return toString(0);
    }
}
