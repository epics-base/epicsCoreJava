/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
     * @return byte value of field.
     */
    byte get();
    /**
     * Put the <i>byte</i> value into the field.
     * If the field is immutable a message is generated and the field not modified.
     * @param value new byte value for field.
     */
    void put(byte value);
}
