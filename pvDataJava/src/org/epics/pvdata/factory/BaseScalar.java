/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.Scalar;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.Type;

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
    
	private static final String[] idLUT = {
		"boolean", // pvBoolean
		"byte",    // pvByte
		"short",   // pvShort
		"int",     // pvInt
		"long",    // pvLong
		"ubyte",   // pvUByte
		"ushort",  // pvUShort
		"uint",    // pvUInt
		"ulong",   // pvULong
		"float",   // pvFloat
		"double",  // pvDouble
		"string"   // pvString
	};

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Field#getID()
	 */
	@Override
	public String getID() {
		return idLUT[scalarType.ordinal()];
	}
	/* (non-Javadoc)
     * @see org.epics.pvdata.pv.Scalar#getScalarType()
     */
    public ScalarType getScalarType() {
        return scalarType;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.factory.BaseField#toString(java.lang.StringBuilder, int)
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
	 * @see org.epics.pvdata.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
	 */
	@Override
	public void serialize(ByteBuffer buffer, SerializableControl control) {
		control.ensureBuffer(1);
		buffer.put(typeCodeLUT[scalarType.ordinal()]);
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
	 */
	@Override
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {
		// must be done via FieldCreate
		throw new RuntimeException("not valid operation, use FieldCreate.deserialize instead");
	}
	
	
}
