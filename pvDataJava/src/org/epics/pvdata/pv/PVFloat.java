/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
     * @return float Value of field.
     */
    float get();
    /**
     * Put the <i>float</i> value into the field.
     * If the field is immutable a message is generated and the field not modified.
     * @param value New value.
     */
    void put(float value);
}
