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

}
