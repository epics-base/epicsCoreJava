/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

/**
 * Interface for field reflection.
 * @author mrk
 *
 */
public interface Field {
    /**
     * Get the field name
     * @return the field name
     */
    String getFieldName();
    /**
     * Get the field type.
     * @return The field type.
     */
    Type getType();
    /**
     * Convert to a string
     * @return The field as a string
     */
    String toString();
    /**
     * Convert to a string
     * @param indentLevel Indentation level
     * @return The field as a string
     */
    String toString(int indentLevel);   
}
