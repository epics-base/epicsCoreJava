/**
 * 
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVBoolean;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.SerializableControl;

/**
 * Base class for PVBoolean.
 * It provides a complete implementation but can be extended.
 * @author mrk
 *
 */
public class BasePVBoolean extends AbstractPVScalar implements PVBoolean
{
    protected boolean value = false;

    public BasePVBoolean(PVStructure parent,Scalar scalar) {
        super(parent,scalar);
    }        
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVBoolean#get()
     */
    @Override
    public boolean get() {
        return value;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVBoolean#put(boolean)
     */
    @Override
    public void put(boolean value) {
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
    public void serialize(ByteBuffer buffer, SerializableControl flush) {
    	flush.ensureBuffer(1);
        buffer.put(value ? (byte)1 : (byte)0);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvData.pv.DeserializableControl)
     */
    @Override
    public void deserialize(ByteBuffer buffer, DeserializableControl control) {
    	control.ensureData(1);
        value = buffer.get() != 0;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        // TODO anything else?
        if (obj instanceof PVBoolean) {
            PVBoolean b = (PVBoolean)obj;
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
		return (value ? 1231 : 1237);
	}    
    
}
