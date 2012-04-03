/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.ScalarArray;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.SerializableControl;
import org.epics.pvData.pv.Type;

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
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.ScalarArray#getElementType()
     */
    public ScalarType getElementType() {
        return elementType;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.factory.BaseField#toString(java.lang.StringBuilder, int)
     */
    @Override
    public void toString(StringBuilder buf, int indentLevel) {
        buf.append(elementType.toString());
        buf.append("[]");
    }
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 0x10 | elementType.ordinal();
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
	 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvData.pv.SerializableControl)
	 */
	@Override
	public void serialize(ByteBuffer buffer, SerializableControl control) {
		control.ensureBuffer(1);
		buffer.put((byte)(0x10 | BaseScalar.typeCodeLUT[elementType.ordinal()]));
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
