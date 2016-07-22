/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

/**
 * Get/put float data.
 * @author mrk
 *
 */
public interface PVFloat extends PVScalar{
    /**
     * Get the <i>float</i> value stored in the field.
     *
     * @return the float value of field
     */
    float get();
    /**
     * Put the <i>float</i> value into the field.
     * If the field is immutable a message is generated and the field not modified.
     *
     * @param value the new value
     */
    void put(float value);
}
