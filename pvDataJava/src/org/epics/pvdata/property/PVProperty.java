/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.property;

import org.epics.pvdata.pv.PVField;
/**
 * An interface for locating property fields.
 * @author mrk
 *
 */
public interface PVProperty {
    /**
     * Find a property by looking for a field by name.
     *  
     * @param pvField The pvField.
     * @param fieldName A string of the form name.name...
     * @return The PVField interface for the property or null if not found. 
     */
    PVField findProperty(PVField pvField,String fieldName);
    /**
     * Find a property by searching up the parent tree.
     * @param pvField The pvField.
     * @param propertyName The property name which is expected to match the name of a field.
     * @return The interface to the first field found that is not a null structure or null if not found.
     */
    PVField findPropertyViaParent(PVField pvField,String propertyName);
    /**
     * Get the names of all the properties for this PVField.
     * A property name is the field name.
     * If this PVfield is a structure then every field except null structures is a property.
     * If this PVField is the value field the parent is the starting point and the properties will
     * not include the value field itself. In addition a search up the parent tree is made for the timeStamp.
     * @param pvField The pvField.
     * @return The String array for the names of the properties.
     */
    String[] getPropertyNames(PVField pvField);
}
