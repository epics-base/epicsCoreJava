/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;

/**
 * Interface for field reflection.
 * @author mrk
 *
 */
public interface Field extends Serializable {
    /**
     * Get the identification string.
     * @return The identification string, can be empty.
     */
    String getID();
    /**
     * Get the field type.
     * @return The field type.
     */
    Type getType();
    /**
     * Convert to a string
     * @param buf buffer for the result
     */
    void toString(StringBuilder buf);
    /**
     * Convert to a string
     * @param buf buffer for the result
     * @param indentLevel Indentation level
     */
    void toString(StringBuilder buf,int indentLevel);
    /**
     * Implement standard toString().
     * @return The field as a String.
     */
    String toString();
}
