/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.PVULong;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.SerializableControl;

/**
 * Base class for PVLong.
 * It provides a complete implementation but cal also be extended.
 * @author mrk
 *
 */
public class BasePVULong extends AbstractPVScalar implements PVULong
{
    protected long value;

    /**
     * Constructor
     * @param scalar The introspection  interface.
     */
    public BasePVULong(Scalar scalar) {
        super(scalar);
        value = 0;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVLong#get()
     */
    public long get() {
        return value;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVLong#put(long)
     */
    public void put(long value) {
        if(super.isImmutable()) {
            throw new IllegalArgumentException("field is immutable");
        }
        this.value = value;
        super.postPut();
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
     */
    public void serialize(ByteBuffer buffer, SerializableControl flusher) {
    	flusher.ensureBuffer(Long.SIZE/Byte.SIZE);
        buffer.putLong(value);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
     */
    public void deserialize(ByteBuffer buffer, DeserializableControl control) {
    	control.ensureData(Long.SIZE/Byte.SIZE);
        value = buffer.getLong();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        // TODO anything else?
        if (obj instanceof PVULong) {
            PVULong b = (PVULong)obj;
            return b.get() == value;
        }
        else
            return false;
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int)value;
	}
}
