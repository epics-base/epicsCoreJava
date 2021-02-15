/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.StructureArray;
import org.epics.pvdata.pv.Type;

/**
 * Base class for implementing a StructureArray.
 * It is also a complete implementation.
 * @author mrk
 *
 */
public class BaseStructureArray extends BaseField implements StructureArray {
    private static Convert convert = ConvertFactory.getConvert();
	private final Structure structure;

	/**
	 * Constructor for BaseStructureArray
	 * @param elementStructure The structure introspection interface for each element
	 */
	public BaseStructureArray(Structure elementStructure) {
		super(Type.structureArray);
        if (elementStructure==null)
        	throw new NullPointerException("elementStructure is null");
		this.structure = elementStructure;
	}
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Field#getID()
	 */
	public String getID() {
		return structure.getID() + "[]";
	}
	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.StructureArray#getStructure()
	 */
	public Structure getStructure() {
		return structure;
	}
	@Override
	public void toString(StringBuilder buf, int indentLevel) {
	    buf.append(getID());
	    convert.newLine(buf,indentLevel+1);
	    structure.toString(buf,indentLevel+1);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 0x08 | structure.hashCode();
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
		final BaseStructureArray other = (BaseStructureArray) obj;
		if (!structure.equals(other.structure))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvdata.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
	 */
	public void serialize(ByteBuffer buffer, SerializableControl control) {
		control.ensureBuffer(1);
		buffer.put((byte)0x88);
		control.cachedSerialize(structure, buffer);
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
