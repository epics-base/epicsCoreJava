/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import org.epics.pvData.pv.Array;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Type;

/**
 * Base class for implementing a Array.
 * It is also a complete implementation.
 * @author mrk
 *
 */
public class BaseArray extends BaseField implements Array {
    
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

}
