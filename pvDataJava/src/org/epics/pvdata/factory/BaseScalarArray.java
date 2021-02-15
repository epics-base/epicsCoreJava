/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.ScalarArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.Type;

/**
 * Base class for implementing a ScalarArray.
 * It is also a complete implementation.
 * @author mrk
 *
 */
public class BaseScalarArray extends BaseField implements ScalarArray {

    private final ScalarType elementType;

    /**
     * Constructor for BaseArray.
     * @param elementType The element Type.
     */
    public BaseScalarArray(ScalarType elementType) {
        super(Type.scalarArray);
        if (elementType==null)
        	throw new NullPointerException("elementType is null");
        this.elementType = elementType;
    }
	/**
	 * ID for each scalarArray type.
	 */
	private static final String[] idLUT = {
		"boolean[]", // pvBoolean
		"byte[]",    // pvByte
		"short[]",   // pvShort
		"int[]",     // pvInt
		"long[]",    // pvLong
		"ubyte[]",   // pvUByte
		"ushort[]",  // pvUShort
		"uint[]",    // pvUInt
		"ulong[]",   // pvULong
		"float[]",   // pvFloat
		"double[]",  // pvDouble
		"string[]"   // pvString
	};

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Field#getID()
	 */
	public String getID() {
		return idLUT[elementType.ordinal()];
	}
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.ScalarArray#getElementType()
     */
    public ScalarType getElementType() {
        return elementType;
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
		return 0x08 | elementType.ordinal();
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
		final BaseScalarArray other = (BaseScalarArray) obj;
		if (!elementType.equals(other.elementType))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
	 */
	public void serialize(ByteBuffer buffer, SerializableControl control) {
		control.ensureBuffer(1);
		buffer.put((byte)(0x08 | BaseScalar.typeCodeLUT[elementType.ordinal()]));
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
	 */
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {
		// must be done via FieldCreate
		throw new RuntimeException("not valid operation, use FieldCreate.deserialize instead");
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Array#getArraySizeType()
	 */
	public ArraySizeType getArraySizeType() {
		return ArraySizeType.variable;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Array#getMaximumCapacity()
	 */
	public int getMaximumCapacity() {
		return 0;
	}

}
