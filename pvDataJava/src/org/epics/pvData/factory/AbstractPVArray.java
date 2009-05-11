/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

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
    /**
     * Serialize array size.
     * @param s size to encode.
     * @param buffer serizalization buffer.
     */
    public final static void writeSize(final int s, ByteBuffer buffer) {
    	if (s == -1)					// null
    		buffer.put((byte)-1);
    	else if (s < 254)
    		buffer.put((byte)s);
    	else
    		buffer.put((byte)-2).putInt(s);	// (byte)-2 + size
    }
    /**
     * Deserializa array size.
     * @param buffer deserialization buffer.
     * @return array size.
     */
    public final static int readSize(ByteBuffer buffer)
    {
    	final byte b = buffer.get();
    	if (b == -1)
    		return -1;
    	else if (b == -2) {
    		final int s = buffer.getInt();
    		if (s < 0)
    			throw new RuntimeException("negative array size");
    		return s;
    	}
    	else
    		return (int)(b < 0 ? b + 256 : b);
    }
	/**
	 * String serializaton helper method.
	 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
	 */
	public final static void serializeString(final String value, ByteBuffer buffer) {
		if (value == null)
			writeSize(-1, buffer);
		else {
			writeSize(value.length(), buffer);
			buffer.put(value.getBytes());	// UTF-8
		}
	}
	/**
	 * String serializaton helper method.
	 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
	 */
	public final static String deserializeString(ByteBuffer buffer) {
		int size = AbstractPVArray.readSize(buffer);
		if (size >= 0) {
			byte[] bytes = new byte[size];
			buffer.get(bytes);
			return new String(bytes);
		}
		else
			return null;
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
	 */
	public void serialize(ByteBuffer buffer) {
		serialize(buffer, 0, -1);
	}
	
}
