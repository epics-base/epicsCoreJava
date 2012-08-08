/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
     * @return string value of field.
     */
    String get();
    /**
     * Put the <i>String</i> value into the field.
     * If the field is immutable a message is generated and the field not modified.
     * @param value New value.
     */
    void put(String value);
}
