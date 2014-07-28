/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.misc.SerializeHelper;
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
public class BaseScalarFixedArray extends BaseField implements ScalarArray {
    
    private final ScalarType elementType;
    private final int size;
    private final String id;
    
    /**
     * Constructor for fixed size scalar array.
     * @param elementType The element Type.
     * @param size fixed size.
     */
    public BaseScalarFixedArray(ScalarType elementType, int size) {
        super(Type.scalarArray);
        if (elementType==null)
        	throw new NullPointerException("elementType is null");
        if (size<=0)
        	throw new IllegalArgumentException("size <= 0");
        this.elementType = elementType;
        this.size = size;
        this.id = BaseScalar.idLUT[this.elementType.ordinal()] + "[" + size + "]";
    }
    
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Field#getID()
	 */
	@Override
	public String getID() {
		return id;
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
		return 0x18 | elementType.ordinal();
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
		final BaseScalarFixedArray other = (BaseScalarFixedArray) obj;
		if (!elementType.equals(other.elementType))
			return false;
		if (size != other.size)
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
	 */
	@Override
	public void serialize(ByteBuffer buffer, SerializableControl control) {
		control.ensureBuffer(1);
		buffer.put((byte)(0x18 | BaseScalar.typeCodeLUT[elementType.ordinal()]));
		SerializeHelper.writeSize(size, buffer, control);
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
	 */
	@Override
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {
		// must be done via FieldCreate
		throw new RuntimeException("not valid operation, use FieldCreate.deserialize instead");
	}

	@Override
	public ArraySizeType getArraySizeType() {
		return ArraySizeType.fixed;
	}
	
	@Override
	public int getMaximumCapacity() {
		return size;
	}
	
}
