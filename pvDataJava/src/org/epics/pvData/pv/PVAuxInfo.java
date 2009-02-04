/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

import java.util.Map;

/**
 * Auxiliary information for a field.
 * Each item is stored as a PVScalar.
 * A map (key,value) is provided for accessing the items where the key is a String.
 * @author mrk
 *
 */
public interface PVAuxInfo {
	
	/**
	 * Get the PVField with which this PVAuxInfo is associated
	 * @return The PVField.
	 */
	PVField getPVField();
    /**
     * Add a new auxiliary item or retrieve the interface to an existing item. 
     * @param key The key.
     * @param scalarType The scalarType.
     * @return The PVScalar for the auxiliary item.
     */
    PVScalar createInfo(String key,ScalarType scalarType);
    /**
     * Get a map of the current auxiliary items.
     * @return The map.
     */
    Map<String,PVScalar> getInfos();
    /**
     * Get a single auxiliary item..
     * @param key The key.
     * @return The value or null of the key does not exist.
     */
    PVScalar getInfo(String key);
    /**
     * Generate a string describing the auxiliary information.
     * @return The string describing the auxiliary information..
     */
    String toString();
    /**
     * Generate a string describing the auxiliary information.
     * @param indentLevel Indent level. Each level is four spaces.
     * @return The string describing the attributes.
     */
    String toString(int indentLevel);
    
}
