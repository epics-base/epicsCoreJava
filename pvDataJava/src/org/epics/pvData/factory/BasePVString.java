/**
 * 
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

import org.epics.pvData.pv.*;

/**
 * Base class for PVString.
 * It provides a complete implementation can be extended.
 * @author mrk
 *
 */
public class BasePVString extends AbstractPVScalar implements PVString
{
    protected String value;
    
    public BasePVString(PVStructure parent,Scalar scalar) {
        super(parent,scalar);
        value = null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVString#get()
     */
    public String get() {
        return value;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVString#put(java.lang.String)
     */
    public void put(String value) {
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
        return AbstractPVArray.getStringSerializationSize(get());
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
     */
    public void serialize(ByteBuffer buffer) {
        AbstractPVArray.serializeString(get(), buffer);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
     */
    public void deserialize(ByteBuffer buffer) {
        value = AbstractPVArray.deserializeString(buffer);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        // TODO anything else?
        if (obj instanceof PVString) {
            PVString b = (PVString)obj;
            final String bv = b.get();
            if (bv != null)
                return bv.equals(value);
            else
                return bv == value;
        }
        else
            return false;
    }
}
