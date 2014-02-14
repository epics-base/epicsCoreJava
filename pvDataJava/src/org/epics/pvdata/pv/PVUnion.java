/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;

/**
 * PVUnion interface.
 * @author mse
 *
 */
public interface PVUnion extends PVField, Serializable {
	
	/**
	 * Undefined index.
	 * Default value upon PVUnion construction. Can be set by the user.
	 * Corresponds to <code>null</code> value.
	 */
	public static int UNDEFINED_INDEX = -1;
	
    /**
     * Get the union introspection interface.
     * @return The introspection interface.
     */
    Union getUnion();
    
    /**
     * Get the <code>PVField</code> value stored in the field.
     * @return <code>PVField</code> value of field, <code>null</code> if <code>getSelectedIndex() == UNDEFINED_INDEX</code>.
     */
    PVField get();
    
    /**
     * Get the <code>PVField</code> value stored in the field.
     * @param c expected class of a requested field.
     * @return <code>PVField</code> value of field, <code>null</code> if <code>getSelectedIndex() == UNDEFINED_INDEX</code>.
     */
    <T extends PVField> T get(Class<T> c);

    /**
     * Select field (set index) and get the field at the index.
     * @param index index of the field to select.
     * @return corresponding PVField (of undetermined value), <code>null</code> if <code>index == UNDEFINED_INDEX</code>.
     * @throws <code>IllegalArgumentException</code> if index is invalid (out of range).
     */
    PVField select(int index);

    /**
     * Select field (set index) and get the field at the index.
     * @param c expected class of a requested field.
     * @param index index of the field to select.
     * @return corresponding PVField (of undetermined value), <code>null</code> if <code>index == UNDEFINED_INDEX</code>.
     * @throws <code>IllegalArgumentException</code> if index is invalid (out of range).
     */
    <T extends PVField> T select(Class<T> c, int index);
    
    /**
     * Select field (set index) and get the field by given name.
     * @param fieldName the name of the field to select.
     * @return corresponding PVField (of undetermined value).
     * @throws <code>IllegalArgumentException</code> if field does not exist.
     */
    PVField select(String fieldName);
    
    /**
     * Select field (set index) and get the field by given name.
     * @param c expected class of a requested field.
     * @param fieldName the name of the field to select.
     * @return corresponding PVField (of undetermined value).
     * @throws <code>IllegalArgumentException</code> if field does not exist.
     */
    <T extends PVField> T select(Class<T> c, String fieldName);

    /**
     * Get selected field index.
     * @return selected field index.
     */
    int getSelectedIndex();
    
    /**
     * Get selected field name.
     * @return selected field name.
     */
    String getSelectedFieldName();
    
    /**
     * Set the <code>PVField</code> (by reference!) as selected field.
     * If a value is not a valid union field an <code>IllegalArgumentException</code> exception is thrown.
     * @param value the field to set.
     */
    void set(PVField value);
    /**
     * Set the <code>PVField</code> (by reference!) as field at given index.
     * If a value is not a valid union field an <code>IllegalArgumentException</code> exception is thrown.
     * Use <code>select(int)</code> to put by value.
     * @param index index of a field to put.
     * @param value the field to set.
     * @see #select(int)
     */
    void set(int index, PVField value);
    /**
     * Set the <code>PVField</code> (by reference!) as field by given name.
     * If a value is not a valid union field an <code>IllegalArgumentException</code> exception is thrown.
     * Use <code>select(String)</code> to put by value.
     * @param fieldName Name of the field to put.
     * @param value the field to set.
     * @see #select(String)
     */
    void set(String fieldName, PVField value);
}
