/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.StructureArray;

/**
 * Base class for implementing a Array.
 * It is also a complete implementation.
 * @author mrk
 *
 */
public class BaseStructureArray extends BaseArray implements StructureArray {
	private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	private Structure structure;

	/**
	 * Constructor for BaseArray.
	 * @param fieldName The field name.
	 * @param fields The introspection interfaces for the subfields of structure.
	 */
	public BaseStructureArray(String fieldName,Field[] fields) {
		super(fieldName, ScalarType.pvStructure);
		structure = fieldCreate.createStructure("", fields);
	}
	public BaseStructureArray(String fieldName,Structure elementStructure) {
		super(fieldName, ScalarType.pvStructure);
		this.structure = elementStructure;
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.pv.StructureArray#getStructure()
	 */
	@Override
	public Structure getStructure() {
		return structure;
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.factory.BaseField#toString()
	 */
	public String toString() { return getString(0);}
	/* (non-Javadoc)
	 * @see org.epics.pvData.factory.BaseField#toString(int)
	 */
	public String toString(int indentLevel) {
		return getString(indentLevel);
	}

	private String getString(int indentLevel) {
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString(indentLevel));
		builder.append(" structure " + structure.toString());
		return builder.toString();
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
