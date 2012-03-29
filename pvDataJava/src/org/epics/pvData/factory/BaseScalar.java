/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

import org.epics.pvData.misc.SerializeHelper;
import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.SerializableControl;
import org.epics.pvData.pv.Type;

/**
 * Base class for implementing a Scalar.
 * It is also a complete implementation.
 * @author mrk
 *
 */
public class BaseScalar extends BaseField implements Scalar {
    
    private ScalarType scalarType;
    
    /**
     * Constructor for BaseScalar.
     * @param scalarType The scalar Type.
     */
    public BaseScalar(ScalarType scalarType) {
        super(Type.scalar);
        this.scalarType = scalarType;
        if(scalarType==null) {
        	throw new NullPointerException("scalarType is null");
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Scalar#getScalarType()
     */
    public ScalarType getScalarType() {
        return scalarType;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.BaseField#toString(java.lang.StringBuilder, int)
     */
    @Override
    public void toString(StringBuilder buf, int indentLevel) {
        buf.append(scalarType.toString());
    }
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + ((scalarType == null) ? 0 : scalarType.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final BaseScalar other = (BaseScalar) obj;
		if (scalarType == null) {
			if (other.scalarType != null)
				return false;
		} else if (!scalarType.equals(other.scalarType))
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvData.pv.SerializableControl)
	 */
	@Override
	public void serialize(ByteBuffer buffer, SerializableControl control) {
		control.ensureBuffer(1);
		buffer.put((byte)(Type.scalar.ordinal() << 4 | scalarType.ordinal()));
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvData.pv.DeserializableControl)
	 */
	@Override
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {
		// TODO Auto-generated method stub
		
	}
	
	
}
