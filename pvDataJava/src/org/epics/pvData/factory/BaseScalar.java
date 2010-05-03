/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Type;

/**
 * Base class for implementing a Scalar.
 * It is also a complete implementation.
 * @author mrk
 *
 */
public class BaseScalar extends BaseField implements Scalar {
    
    private ScalarType scalarType;
    
    /**
     * Constructor for BaseScalar.
     * @param fieldName The field name.
     * @param scalarType The scalar Type.
     */
    public BaseScalar(String fieldName,ScalarType scalarType) {
        super(fieldName, Type.scalar);
        this.scalarType = scalarType;
        if(scalarType==null) {
        	throw new NullPointerException("scalarType is null");
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Scalar#getScalarType()
     */
    public ScalarType getScalarType() {
        return scalarType;
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
        builder.append(" scalarType " + scalarType.toString());
        return builder.toString();
    }
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + ((scalarType == null) ? 0 : scalarType.hashCode());
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
		final BaseScalar other = (BaseScalar) obj;
		if (scalarType == null) {
			if (other.scalarType != null)
				return false;
		} else if (!scalarType.equals(other.scalarType))
			return false;
		return true;
	}
}
