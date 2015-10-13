/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
