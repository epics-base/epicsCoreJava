/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

/**
 * Get/put float data.
 * @author mrk
 *
 */
public interface PVFloat extends PVScalar{
    /**
     * Get the <i>float</i> value stored in the field.
     * @return float Value of field.
     */
    float get();
    /**
     * Put the <i>float</i> value into the field.
     * @param value New value.
     * @throws IllegalStateException if the field is not mutable.
     */
    void put(float value);
}
