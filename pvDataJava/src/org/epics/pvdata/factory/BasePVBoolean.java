/**
 * 
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVBoolean;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.SerializableControl;

/**
 * Base class for PVBoolean.
 * It provides a complete implementation but can be extended.
 * @author mrk
 *
 */
public class BasePVBoolean extends AbstractPVScalar implements PVBoolean
{
    protected boolean value = false;

    public BasePVBoolean(Scalar scalar) {
        super(scalar);
    }        
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVBoolean#get()
     */
    @Override
    public boolean get() {
        return value;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVBoolean#put(boolean)
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
     * @see org.epics.pvdata.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
     */
    @Override
    public void serialize(ByteBuffer buffer, SerializableControl flush) {
    	flush.ensureBuffer(1);
        buffer.put(value ? (byte)1 : (byte)0);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
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
