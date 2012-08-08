/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
     * @return short value of field.
     */
    short get();
    /**
     * Put the <i>short</i> value into the field.
     * If the field is immutable a message is generated and the field not modified.
     * @param value New value.
     */
    void put(short value);
}
