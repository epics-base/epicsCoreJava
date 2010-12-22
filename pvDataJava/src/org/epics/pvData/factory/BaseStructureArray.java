/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import org.epics.pvData.pv.Convert;
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
	 * @param fieldName The fieldName.
	 * @param elementStructure The structure introspection interface for each element
	 */
	public BaseStructureArray(String fieldName,Structure elementStructure) {
		super(fieldName, Type.structureArray);
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
        buf.append("structure[]");
        super.toString(buf, indentLevel +1);
		convert.newLine(buf, indentLevel+1);
		structure.toString(buf, indentLevel+1);
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
}
