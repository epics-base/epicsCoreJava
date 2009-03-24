/**
 * 
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;

/**
 * Base class for PVInt.
 * It provides a complete implementation but can be extended.
 * @author mrk
 *
 */
public class BasePVInt extends AbstractPVScalar implements PVInt
{
    protected int value;

    public BasePVInt(PVStructure parent,Scalar scalar) {
        super(parent,scalar);
        value = 0;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVInt#get()
     */
    public int get() {
        return value;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVInt#put(int)
     */
    public void put(int value) {
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
        buffer.putInt(value);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
     */
    public void deserialize(ByteBuffer buffer) {
        value = buffer.getInt();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        // TODO anything else?
        if (obj instanceof PVInt) {
            PVInt b = (PVInt)obj;
            return b.get() == value;
        }
        else
            return false;
    }
}
