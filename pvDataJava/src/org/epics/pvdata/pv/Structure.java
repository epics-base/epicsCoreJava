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
     * 
     * @param fieldName the name of the field
     * @return the Field that describes the field
     */
    Field getField(String fieldName);

    /**
     * Get the index of the specified field.
     * 
     * @param fieldName the name of the field
     * @return the index or -1 if fieldName is not a field in the structure
     */
    int getFieldIndex(String fieldName);

    /**
     * Get all the subfields of the structure.
     * 
     * @return an array of Field that describes
     * each of the subfields in the structure
     */
    Field[] getFields();

    /**
     * Get a sub fields by index.
     * 
     * @param fieldIndex the field index
     * @return the field at specified index
     */
    Field getField(int fieldIndex);

    /**
     * Get the Field for the specified fieldName
     * 
     * @param <T> the expected type of the Field of the requested field
     * @param c class object modeling the class T of expected type of the requested field
     * @param fieldName the fieldName
     * @return The Field or null if the field does not exist, or the field is not of type <code>T</code>
     */
    <T extends Field> T getField(Class<T> c, String fieldName);

    /**
     * Get a subfield by index.
     * 
     * @param <T> the expected type of the Field of the requested field
     * @param c class object modeling the class T of expected type of the requested field
     * @param fieldIndex the index
     * @return the Field or null if the field does not exist, or the field is not of type <code>T</code>
     */
    <T extends Field> T getField(Class<T> c, int fieldIndex);
    /**
     * Get the array of the subfield names.
     * @return the array
     */
    String[] getFieldNames();
    /**
     * Get the name of the subfield by index.
     * 
     * @param fieldIndex the field index
     * @return the name of the field
     */
    String getFieldName(int fieldIndex);
}
