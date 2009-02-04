/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.Type;

/**
 * Base class for creating a Field.
 * It can also be a complete implementation.
 * @author mrk
 *
 */
public abstract class BaseField implements Field
{
    protected String fieldName;
    private Type type;
    private static Convert convert = ConvertFactory.getConvert();

    /**
     * Constructor for BaseField.
     * @param fieldName The field fieldName.
     * @param type The field type.
     * @throws IllegalArgumentException if type is null;
     */
    public BaseField(String fieldName, Type type) {
        if(type==null) {
            throw new IllegalArgumentException("type is null");
        }
        this.fieldName = fieldName;
        this.type = type;
    }   
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Field#getFieldName()
     */
    public String getFieldName() {
        return(fieldName);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Field#getType()
     */
    public Type getType() {
        return type;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() { return toString(0);}
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Field#toString(int)
     */
    public String toString(int indentLevel) {
        StringBuilder builder = new StringBuilder();
        convert.newLine(builder,indentLevel);
        builder.append(String.format("field %s type %s",
                fieldName,type.toString()));
        return builder.toString();
    }
}
