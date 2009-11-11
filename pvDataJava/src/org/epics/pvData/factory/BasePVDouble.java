/**
 * 
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVDouble;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.SerializableControl;

/**
 * Base class for PVDouble.
 * It provides a complete implementation but can be extended.
 * @author mrk
 *
 */
public class BasePVDouble extends AbstractPVScalar implements PVDouble
{
    protected double value;
    
    public BasePVDouble(PVStructure parent,Scalar scalar) {
        super(parent,scalar);
        value = 0;
    }        
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVDouble#get()
     */
    @Override
    public double get() {
        return value;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVDouble#put(double)
     */
    @Override
    public void put(double value) {
        if(super.isImmutable()) {
            super.message("field is immutable", MessageType.error);
            return;
        }
        this.value = value;
        super.postPut();
    }        
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVField#toString(int)
     */
    @Override
    public String toString(int indentLevel) {
        return convert.getString(this, indentLevel)
        + super.toString(indentLevel);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvData.pv.SerializableControl)
     */
    @Override
    public void serialize(ByteBuffer buffer, SerializableControl flusher) {
    	flusher.ensureBuffer(Double.SIZE/Byte.SIZE);
        buffer.putDouble(value);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
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
}
