/**
 * 
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVDouble;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;

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
    public double get() {
        return value;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVDouble#put(double)
     */
    public void put(double value) {
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
     * @see org.epics.pvData.pv.Serializable#getSerializationSize()
     */
    public int getSerializationSize() {
        return 8;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer)
     */
    public void serialize(ByteBuffer buffer) {
        buffer.putDouble(value);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer)
     */
    public void deserialize(ByteBuffer buffer) {
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
