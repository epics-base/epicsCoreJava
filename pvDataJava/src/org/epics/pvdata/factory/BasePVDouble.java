/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */

package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.SerializableControl;

/**
 * Base class for PVDouble.
 * It provides a complete implementation but can be extended.
 * @author mrk
 *
 */
public class BasePVDouble extends AbstractPVScalar implements PVDouble
{
    protected double value;
    
    /**
     * Constructor
     * @param scalar The introspection interface.
     */
    public BasePVDouble(Scalar scalar) {
        super(scalar);
        value = 0;
    }        
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVDouble#get()
     */
    @Override
    public double get() {
        return value;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVDouble#put(double)
     */
    @Override
    public void put(double value) {
        if(super.isImmutable()) {
            throw new IllegalArgumentException("field is immutable");
        }
        this.value = value;
        super.postPut();
    }        
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
     */
    @Override
    public void serialize(ByteBuffer buffer, SerializableControl flusher) {
    	flusher.ensureBuffer(Double.SIZE/Byte.SIZE);
        buffer.putDouble(value);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer)
     */
    @Override
    public void deserialize(ByteBuffer buffer, DeserializableControl control) {
    	control.ensureData(Double.SIZE/Byte.SIZE);
        value = buffer.getDouble();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        // TODO anything else?
        if (obj instanceof PVDouble) {
            PVDouble b = (PVDouble)obj;
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
