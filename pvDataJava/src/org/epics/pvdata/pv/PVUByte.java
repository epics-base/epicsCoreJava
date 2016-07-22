/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

/**
 * get/put ubyte data.
 * Since Java does not support unsigned the actual arguments are signed.
 * Code that calls methods of the class is responsible for integer overflow problems.
 * @author mrk
 *
 */
public interface PVUByte extends PVScalar{
    /**
     * Get the <i>ubyte</i> value stored in the field.
     *
     * @return the byte value of field
     */
    byte get();

    /**
     * Put the <i>byte</i> value into the field.
     * If the field is immutable a message is generated and the field not modified.
     *
     * @param value the new byte value for field
     */
    void put(byte value);
}
