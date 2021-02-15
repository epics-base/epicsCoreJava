/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
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

	/**
	 * ID for each scalarType
	 */
	public static final String[] idLUT = {
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
        buf.append(getID());
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

	/**
	 * Type code for each scalarType.
	 */
	public static final byte[] typeCodeLUT = {
		0x00, // pvBoolean
		0x20, // pvByte
		0x21, // pvShort
		0x22, // pvInt
		0x23, // pvLong
		0x24, // pvUByte
		0x25, // pvUShort
		0x26, // pvUInt
		0x27, // pvULong
		0x42, // pvFloat
		0x43, // pvDouble
		0x60  // pvString
	};

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
	 */
	public void serialize(ByteBuffer buffer, SerializableControl control) {
		control.ensureBuffer(1);
		buffer.put(typeCodeLUT[scalarType.ordinal()]);
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
	 */
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {
		// must be done via FieldCreate
		throw new RuntimeException("not valid operation, use FieldCreate.deserialize instead");
	}


}
