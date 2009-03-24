/**
 * 
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVLong;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;

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
    public long get() {
        return value;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVLong#put(long)
     */
    public void put(long value) {
        if(super.isMutable()) {
            this.value = value;
            return ;
        }
        super.message("not isMutable", MessageType.error);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return toString(0);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.AbstractPVField#toString(int)
     */
    public String toString(int indentLevel) {
        return convert.getString(this, indentLevel)
        + super.toString(indentLevel);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
     */
    public void serialize(ByteBuffer buffer) {
        buffer.putLong(value);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
     */
    public void deserialize(ByteBuffer buffer) {
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
}
