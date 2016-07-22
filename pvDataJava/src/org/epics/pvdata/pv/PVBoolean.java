/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

/**
 * get/put boolean data
 * @author mrk
 *
 */
public interface PVBoolean extends PVScalar{
    /**
     * Get the <i>boolean</i> value stored in the field.
     *
     * @return boolean the value of field
     */
    boolean get();

    /**
     * Put the field from a <i>boolean</i> value.
     * If the field is immutable a message is generated and the field not modified.
     *
     * @param value the new boolean value for field.
     */
    void put(boolean value);
}
