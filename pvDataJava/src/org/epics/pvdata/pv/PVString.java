/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

/**
 * Get/put string data.
 * @author mrk
 *
 */
public interface PVString extends PVScalar, SerializableArray{
    /**
     * Get the <i>String</i> value stored in the field.
     *
     * @return the string value of field
     */
    String get();
    /**
     * Put the <i>String</i> value into the field.
     * If the field is immutable a message is generated and the field not modified.
     *
     * @param value the new value
     */
    void put(String value);
}
