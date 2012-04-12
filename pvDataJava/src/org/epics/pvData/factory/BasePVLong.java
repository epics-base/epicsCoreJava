/**
 * 
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVLong;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.PVULong;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.SerializableControl;

/**
 * Base class for PVLong.
 * It provides a complete implementation but cal also be extended.
 * @author mrk
 *
 */
public class BasePVLong extends AbstractPVScalar implements PVLong
{
    protected long value;
    
    public BasePVLong(PVStructure parent,Scalar scalar) {
        super(parent,scalar);
        value = 0;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVLong#get()
     */
    @Override
    public long get() {
        return value;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVLong#put(long)
     */
    @Override
    public void put(long value) {
        if(super.isImmutable()) {
            super.message("field is immutable", MessageType.error);
            return;
        }
        this.value = value;
        super.postPut();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvData.pv.SerializableControl)
     */
    @Override
    public void serialize(ByteBuffer buffer, SerializableControl flusher) {
    	flusher.ensureBuffer(Long.SIZE/Byte.SIZE);
        buffer.putLong(value);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvData.pv.DeserializableControl)
     */
    @Override
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
        if (obj instanceof PVLong) {
            PVLong b = (PVLong)obj;
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
