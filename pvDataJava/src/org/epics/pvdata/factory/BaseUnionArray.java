/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.Type;
import org.epics.pvdata.pv.Union;
import org.epics.pvdata.pv.UnionArray;

/**
 * Base class for implementing a UnionArray.
 * It is also a complete implementation.
 * @author mrk
 *
 */
public class BaseUnionArray extends BaseField implements UnionArray {
    private static Convert convert = ConvertFactory.getConvert();
	private final Union union;

	/**
	 * Constructor for BaseUnionArray
	 * @param elementUnion The union introspection interface for each element
	 */
	public BaseUnionArray(Union elementUnion) {
		super(Type.unionArray);
        if (elementUnion==null)
        	throw new NullPointerException("elementUnion is null");
		this.union = elementUnion;
	}
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Field#getID()
	 */
	public String getID() {
		return union.getID() + "[]";
	}
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.UnionArray#getUnion()
	 */
	public Union getUnion() {
		return union;
	}
	@Override
	public void toString(StringBuilder buf, int indentLevel) {
	    buf.append(getID());
	    convert.newLine(buf,indentLevel+1);
	    union.toString(buf,indentLevel+1);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 0x80 | union.hashCode();
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
		final BaseUnionArray other = (BaseUnionArray) obj;
		if (!union.equals(other.union))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
	 */
	public void serialize(ByteBuffer buffer, SerializableControl control) {
		control.ensureBuffer(1);
		if (union.isVariant())
		{
			// unrestricted/variant union
			buffer.put((byte)0x8A);
		}
		else
		{
			buffer.put((byte)0x89);
			control.cachedSerialize(union, buffer);
		}
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
