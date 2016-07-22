/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

/**
 * get/put byte data.
 * @author mrk
 *
 */
public interface PVByte extends PVScalar{
    /**
     * Get the <i>byte</i> value stored in the field.
     *
     * @return the byte value of field
     */
    byte get();

    /**
     * Put the <i>byte</i> value into the field.
     *
     * If the field is immutable a message is generated and the field not modified
     * @param value new byte value for field
     */
    void put(byte value);
}
