/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

/**
 * PVField is the base class for each PVData field.
 * Each PVType has an interface that extends PVField.
 * @author mrk
 *
 */
public interface PVField extends Serializable {
    /**
     * Get the fieldName for this field.
     *
     * @return the name or the empty string if top-level field.
     */
    String getFieldName();

    /**
     * Fully expand the name of this field using the
     * names of its parent fields with a period '.' separating
     * each name.
     *
     * @return the full name or the empty string if top-level field
     */
    String getFullName();

    /**
     * Get offset of the PVField field within top level structure.
     * Every field within the PVStructure has a unique offset.
     * The top level structure has an offset of 0.
     * The first field within the structure has offset equal to 1.
     * The other offsets are determined by recursively traversing each structure of the tree.
     *
     * @return the offset
     */
    int getFieldOffset();

    /**
     * Get the next offset. If the field is a scalar or array field then this is just offset + 1.
     * If the field is a structure it is the offset of the next field after this structure.
     * Thus (nextOffset - offset) is always equal to the number of fields within the field.
     *
     * @return the offset.
     */
    int getNextFieldOffset();

    /**
     * Get the total number of fields in this field.
     * This is equal to nextFieldOffset - fieldOffset.
     *
     * @return the number of fields
     */
    int getNumberFields();

    /**
     * Is the field immutable - that is, does it not allow changes?
     *
     * @return (false,true) if it (is not, is) immutable
     */
    boolean isImmutable();

    /**
     * Set the field to be immutable - that is, it can no longer be modified.
     * This is permanent. Once done the field cannot be made mutable.
     */
    void setImmutable();

    /**
     * Get the <i>Field</i> that describes the field.
     *
     * @return the Field, which is the reflection interface
     */
    Field getField();

    /**
     * Get the parent of this field.
     *
     * @return the parent interface or null if top-level
     */
    PVStructure getParent();

    /**
     * postPut - called when the field is updated by the implementation.
     */
    void postPut();

    /**
     * Set the handler for postPut.
     * At most one handler can be set.
     *
     * @param postHandler the handler
     */
    void setPostHandler(PostHandler postHandler);

    /**
     * Convert the PVField to a string.
     *
     *  @param buf the buffer for the result
     */
    void toString(StringBuilder buf);

    /**
     * Convert the PVField to a string, indenting each line.
     *
     * @param buf the buffer for the result
     * @param indentLevel the indentation level
     */
    void toString(StringBuilder buf,int indentLevel);

    /**
     * Implement standard toString().
     *
     * @return the field as a String
     */
    String toString();
}
