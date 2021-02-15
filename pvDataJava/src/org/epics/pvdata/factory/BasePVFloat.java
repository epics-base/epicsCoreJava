/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.PVFloat;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.SerializableControl;

/**
 * Base class for PVFloat.
 * It provides a complete implementation but can be extended.
 * @author mrk
 *
 */
public class BasePVFloat extends AbstractPVScalar implements PVFloat
{
    protected float value;

    /**
     * Constructor
     * @param scalar The introspection interface.
     */
    public BasePVFloat(Scalar scalar) {
        super(scalar);
        value = 0;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVFloat#get()
     */
    public float get() {
        return value;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVFloat#put(float)
     */
    public void put(float value) {
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
    	flusher.ensureBuffer(Float.SIZE/Byte.SIZE);
        buffer.putFloat(value);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
     */
    public void deserialize(ByteBuffer buffer, DeserializableControl control) {
    	control.ensureData(Float.SIZE/Byte.SIZE);
        value = buffer.getFloat();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        // TODO anything else?
        if (obj instanceof PVFloat) {
            PVFloat b = (PVFloat)obj;
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
