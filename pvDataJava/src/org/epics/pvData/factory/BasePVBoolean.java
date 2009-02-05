/**
 * 
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVBoolean;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;

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
    public boolean get() {
        return value;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVBoolean#put(boolean)
     */
    public void put(boolean value) {
        if(super.isMutable()) {
            this.value = value;
            super.postPut();
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
     * @see org.epics.pvData.pv.Serializable#getSerializationSize()
     */
    public int getSerializationSize() {
        return 1;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
     */
    public void serialize(ByteBuffer buffer) {
        buffer.put(value ? (byte)1 : (byte)0);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
     */
    public void deserialize(ByteBuffer buffer) {
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
}
