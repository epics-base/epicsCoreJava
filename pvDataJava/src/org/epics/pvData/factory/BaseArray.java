/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import org.epics.pvData.pv.ScalarArray;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Type;

/**
 * Base class for implementing a Array.
 * It is also a complete implementation.
 * @author mrk
 *
 */
public class BaseArray extends BaseField implements ScalarArray {
    
    private ScalarType elementType;
    
    /**
     * Constructor for BaseArray.
     * @param fieldName The field name.
     * @param elementType The element Type.
     */
    public BaseArray(String fieldName,ScalarType elementType) {
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
        builder.append(" elementType " + elementType.toString());
        return builder.toString();
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
		final BaseArray other = (BaseArray) obj;
		if (elementType == null) {
			if (other.elementType != null)
				return false;
		} else if (!elementType.equals(other.elementType))
			return false;
		return true;
	}
}
