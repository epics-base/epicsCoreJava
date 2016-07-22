/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

/**
 * Get/put short data.
 * @author mrk
 *
 */
public interface PVShort extends PVScalar{
    /**
     * Get the <i>short</i> value stored in the field.
     *
     * @return the short value of field
     */
    short get();

    /**
     * Put the <i>short</i> value into the field.
     * If the field is immutable a message is generated and the field not modified.
     *
     * @param value the new value
     */
    void put(short value);
}
