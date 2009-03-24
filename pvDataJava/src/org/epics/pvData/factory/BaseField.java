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
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((fieldName == null) ? 0 : fieldName.hashCode());
		result = PRIME * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final BaseField other = (BaseField) obj;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
    
}
