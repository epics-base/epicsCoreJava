/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;

/**
 * Reflection interface for an union field.
 * @author mse
 *
 */
public interface Union extends Field{
	/**
	 * Default union ID.
	 */
    public static final String DEFAULT_ID = "union";
    
    /**
     * Default variant union ID.
     */
    public static final String ANY_ID = "any";
    
    /**
     * Get the Field for the specified fieldName
     * @param fieldName The name of the field.
     * @return The Field that describes the field.
     */
    Field getField(String fieldName);
    /**
     * Get the index of the specified field.
     * @param fieldName The name of the field.
     * @return The index or -1 if fieldName is not a field in the union.
     */
    int getFieldIndex(String fieldName);
    /**
     * Get a list of union fields (members).
     * @return an array of fields (can be empty), non-<code>null</code>.
     */
    Field[] getFields();
    /**
     * Get a sub fields by index.
     * @param fieldIndex The field index.
     * @return The field at specified index.
     */
    Field getField(int fieldIndex);
    /**
     * Get the Field for the specified fieldName
     * @param c expected class of a requested field.
     * @param fieldName The fieldName.
     * @return The Field or null if the field does not exist, or the field is not of <code>c</code> type.
     */
    <T extends Field> T getField(Class<T> c, String fieldName);

    /**
     * Get a subfield by index.
     * @param c expected class of a requested field.
     * @param fieldOffset The offset.
     * @return The Field or null if the index is invalid, or the field is not of <code>c</code> type.
     */
    <T extends Field> T getField(Class<T> c, int fieldIndex);
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
    /**
     * Check if this union is variant union (aka any type).
     * @return <code>true</code> if this union is variant union, otherwise <code>false</code>.
     */
    boolean isVariant();
}
