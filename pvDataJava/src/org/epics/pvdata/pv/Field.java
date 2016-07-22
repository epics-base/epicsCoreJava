/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
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
     *
     * @return the identification string, can be empty
     */
    String getID();

    /**
     * Get the field type.
     *
     * @return the field type
     */
    Type getType();

    /**
     * Convert to a string
     *
     * @param buf the buffer for the result
     */
    void toString(StringBuilder buf);

    /**
     * Convert to a string
     *
     * @param buf buffer for the result
     * @param indentLevel the indentation level
     */
    void toString(StringBuilder buf, int indentLevel);

    /**
     * Implement standard toString().
     *
     * @return The field as a String
     */
    String toString();
}
