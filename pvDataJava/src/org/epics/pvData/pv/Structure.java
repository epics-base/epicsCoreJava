/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

/**
 * Reflection interface for structure field.
 * @author mrk
 *
 */
public interface Structure extends Field{
    /**
     * Get the array of field names for the structure..
     * @return The names of the nodes.
     */
    String[] getFieldNames();
    /**
     * Get the <i>Field</i> for the specified field.
     * @param fieldName The name of the field.
     * @return The <i>Field</i> that describes the field.
     */
    Field getField(String fieldName);
    /**
     * Get the index of the specified field.
     * @param fieldName The name of the field.
     * @return The index or -1 if fieldName is not a field in the structure.
     */
    int getFieldIndex(String fieldName);
    /**
     * Get all the <i>Field</i>s for the structure.
     * @return An array of <i>Field</i> that describes
     * each of the nodes in the structure.
     */
    Field[] getFields();
}
