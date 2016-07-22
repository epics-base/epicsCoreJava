/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

/**
 * Get/put int data.
 * @author mrk
 *
 */
public interface PVInt extends PVScalar{
    /**
     * Get the <i>int</i> value stored in the field.
     *
     * @return the int value of field.
     */
    int get();
    /**
     * Put the <i>int</i> value into the field.
     * If the field is immutable a message is generated and the field not modified.
     *
     * @param value the new value
     */
    void put(int value);
}
