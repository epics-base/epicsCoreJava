/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

/**
 * Get/put int data.
 * @author mrk
 *
 */
public interface PVInt extends PVScalar{
    /**
     * Get the <i>int</i> value stored in the field.
     * @return int value of field.
     */
    int get();
    /**
     * Put the <i>int</i> value into the field.
     * If the field is immutable a message is generated and the field not modified.
     * @param value New value.
     */
    void put(int value);
}
