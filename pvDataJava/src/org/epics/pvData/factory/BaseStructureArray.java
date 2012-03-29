/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

import org.epics.pvData.misc.SerializeHelper;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.SerializableControl;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.StructureArray;
import org.epics.pvData.pv.Type;

/**
 * Base class for implementing a StructureArray.
 * It is also a complete implementation.
 * @author mrk
 *
 */
public class BaseStructureArray extends BaseField implements StructureArray {
    private static final Convert convert = ConvertFactory.getConvert();
	private Structure structure;

	/**
	 * Constructor for BaseStructureArray
	 * @param elementStructure The structure introspection interface for each element
	 */
	public BaseStructureArray(Structure elementStructure) {
		super(Type.structureArray);
		this.structure = elementStructure;
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.StructureArray#getStructure()
	 */
	@Override
	public Structure getStructure() {
		return structure;
	}
	@Override
    public void toString(StringBuilder buf, int indentLevel) {
		structure.toString(buf, indentLevel);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + ((structure == null) ? 0 : structure.hashCode());
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
		final BaseStructureArray other = (BaseStructureArray) obj;
		if (structure == null) {
			if (other.structure != null)
				return false;
		} else if (!structure.equals(other.structure))
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvData.pv.SerializableControl)
	 */
	@Override
	public void serialize(ByteBuffer buffer, SerializableControl control) {
		control.ensureBuffer(1);
		buffer.put((byte)(Type.structureArray.ordinal() << 4));
		// we also need to serialize element (structure) introspection data...
		BaseStructure.serializeStructureField(structure, buffer, control);
	}
	
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvData.pv.DeserializableControl)
	 */
	@Override
	public void deserialize(ByteBuffer buffer, DeserializableControl control) {
		// TODO Auto-generated method stub
		
	}
	
	
}
