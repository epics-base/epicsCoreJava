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
	public static int UNDEFINED_INDEX = -1;
	
    /**
     * Get the union introspection interface.
     * @return The introspection interface.
     */
    Union getUnion();
    /**
     * Get the <code>PVField</code> value stored in the field.
     * @return <code>PVField</code> value of field.
     */
    PVField get();
    
    PVField select(int index);
    PVField select(String fieldName);
    
    int getSelectedIndex();
    String getSelectedFieldName();
    
    /**
     * Put the <code>PVField</code> value into the field.
     * If the field is immutable a message is generated and the field not modified.
     * If a value is not a valid union field an <code>IllegalArgumentException</code> exception is thrown.
     * @param index index of a field to put.
     * @param value New value.
     */
    void put(int index, PVField value);
    /**
     * Put the <code>PVField</code> value into the field.
     * If the field is immutable a message is generated and the field not modified.
     * If a value is not a valid union field an <code>IllegalArgumentException</code> exception is thrown.
     * @param fieldName Name of the field to put.
     * @param value New value.
     */
    void put(String fieldName, PVField value);
}
