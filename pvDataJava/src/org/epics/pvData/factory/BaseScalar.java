/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

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
    
    private final ScalarType scalarType;
    
    /**
     * Constructor for BaseScalar.
     * @param scalarType The scalar Type.
     */
    public BaseScalar(ScalarType scalarType) {
        super(Type.scalar);
        if (scalarType==null)
        	throw new NullPointerException("scalarType is null");
        this.scalarType = scalarType;
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
		return scalarType.ordinal();
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
		if (!scalarType.equals(other.scalarType))
			return false;
		return true;
	}
	
	public static final byte[] typeCodeLUT = {
		0x00, // pvBoolean
		0x20, // pvByte
		0x21, // pvShort
		0x22, // pvInt
		0x23, // pvLong
		0x28, // pvUByte
		0x29, // pvUShort
		0x2A, // pvUInt
		0x2B, // pvULong
		0x42, // pvFloat
		0x43, // pvDouble
		0x60  // pvString
	};
	
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvData.pv.SerializableControl)
	 */
	@Override
	public void serialize(ByteBuffer buffer, SerializableControl control) {
		control.ensureBuffer(1);
		buffer.put(typeCodeLUT[scalarType.ordinal()]);
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvData.pv.DeserializableControl)
	 */
	@Override
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {
		// must be done via FieldCreate
		throw new RuntimeException("not valid operation, use FieldCreate.deserialize instead");
	}
	
	
}
