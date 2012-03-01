/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

import org.epics.pvData.misc.SerializeHelper;
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
    
    private ScalarType elementType;
    
    /**
     * Constructor for BaseArray.
     * @param fieldName The field name.
     * @param elementType The element Type.
     */
    public BaseScalarArray(String fieldName,ScalarType elementType) {
        super(fieldName, Type.scalarArray);
        this.elementType = elementType;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Array#getElementType()
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
        super.toString(buf, indentLevel);
    }
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + ((elementType == null) ? 0 : elementType.hashCode());
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
		final BaseScalarArray other = (BaseScalarArray) obj;
		if (elementType == null) {
			if (other.elementType != null)
				return false;
		} else if (!elementType.equals(other.elementType))
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvData.pv.SerializableControl)
	 */
	@Override
	public void serialize(ByteBuffer buffer, SerializableControl control) {
		control.ensureBuffer(1);
		buffer.put((byte)(Type.scalarArray.ordinal() << 4 | elementType.ordinal()));
		SerializeHelper.serializeString(getFieldName(), buffer, control);
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvData.pv.DeserializableControl)
	 */
	@Override
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {
		// TODO Auto-generated method stub
		
	}
	
	
}
