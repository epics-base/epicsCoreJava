/**
 * 
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVShort;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.PVUShort;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.SerializableControl;

/**
 * Base class for PVByte.
 * It provides a complete implementation but can be extended.
 * @author mrk
 *
 */
public class BasePVShort extends AbstractPVScalar implements PVShort,PVUShort
{
    protected short value;
    
    public BasePVShort(PVStructure parent,Scalar scalar) {
        super(parent,scalar);
        value = 0;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVShort#get()
     */
    @Override
    public short get() {
        return value;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVShort#put(short)
     */
    @Override
    public void put(short value) {
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
    	flusher.ensureBuffer(Short.SIZE/Byte.SIZE);
    	buffer.putShort(value);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvData.pv.DeserializableControl)
     */
    @Override
    public void deserialize(ByteBuffer buffer, DeserializableControl control) {
    	control.ensureData(Short.SIZE/Byte.SIZE);
        value = buffer.getShort();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        // TODO anything else?
        if (obj instanceof PVShort) {
            PVShort b = (PVShort)obj;
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
		return value;
	}
}
