/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

import java.util.Map;

/**
 * Reflection interface for field attributes.
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
     * Add a single attribute to the map of attributes.
     * @param key The key.
     * @param scalarType The scalarType.
     * @return The previous value for the key or null if none existed.
     */
    PVScalar createInfo(String key,ScalarType scalarType);
    /**
     * Get a map of the current attributes.
     * @return The map.
     */
    Map<String,PVScalar> getInfos();
    /**
     * Get a single attribute value.
     * @param key The key.
     * @return The value or null of the key does not exist.
     */
    PVScalar getInfo(String key);
    /**
     * Generate a string describing the attributes.
     * @return The string describing the attributes.
     */
    String toString();
    /**
     * Generate a string describing the attributes.
     * @param indentLevel Indent level. Each level is four spaces.
     * @return The string describing the attributes.
     */
    String toString(int indentLevel);
    
}
