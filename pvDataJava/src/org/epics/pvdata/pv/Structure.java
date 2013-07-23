/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;

/**
 * Reflection interface for structure field.
 * @author mrk
 *
 */
public interface Structure extends Field{
	/**
	 * Default structure ID.
	 */
    public static final String DEFAULT_ID = "structure";
    /**
     * Get the Field for the specified fieldName
     * @param fieldName The name of the field.
     * @return The Field that describes the field.
     */
    Field getField(String fieldName);
    /**
     * Get the index of the specified field.
     * @param fieldName The name of the field.
     * @return The index or -1 if fieldName is not a field in the structure.
     */
    int getFieldIndex(String fieldName);
    /**
     * Get all the subfields of the structure.
     * @return An array of Field that describes
     * each of the subfields in the structure.
     */
    Field[] getFields();
    /**
     * Get a sub fields by index.
     * @param fieldIndex The field index.
     * @return The field at specified index.
     */
    Field getField(int fieldIndex);
    /**
     * Get the array of the subfield names.
     * @return The array.
     */
    String[] getFieldNames();
    /**
     * Get the name of the subfield by index.
     * @param fieldIndex The field index.
     * @return The name of the field.
     */
    String getFieldName(int fieldIndex);
}
