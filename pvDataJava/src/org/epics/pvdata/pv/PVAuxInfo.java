/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;

import java.util.Map;

/**
 * Auxiliary information for a field.
 * Each item is stored as a PVScalar.
 * A map (key,value) is provided for accessing the items where the key is a String.
 * @author mrk
 * @deprecated
 *
 */
public interface PVAuxInfo {
    /**
     * Get the PVField with which this PVAuxInfo is associated.
     *
     * @return the PVField
     */
    PVField getPVField();

    /**
     * Add a new auxiliary item or retrieve the interface to an existing item. 
     *
     * @param key the key
     * @param scalarType the ScalarType
     * @return the PVScalar for the auxiliary item
     */
    PVScalar createInfo(String key,ScalarType scalarType);

    /**
     * Get a map of the current auxiliary items.
     * @return The map.
     */
    Map<String,PVScalar> getInfos();

    /**
     * Get a single auxiliary item.
     *
     * @param key the key
     * @return the value or null of the key does not exist
     */
    PVScalar getInfo(String key);

    /**
     * Generate a string describing the auxiliary information.
     *
     * @param buf the buffer for the result
     */
    void toString(StringBuilder buf);

    /**
     * Generate a string describing the auxiliary information.
     *
     * @param buf the buffer for the result
     * @param indentLevel the indentation level. Each level is four spaces.
     */
    void toString(StringBuilder buf,int indentLevel);

}
