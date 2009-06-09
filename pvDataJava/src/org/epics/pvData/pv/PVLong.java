/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

/**
 * Get/put long data.
 * @author mrk
 *
 */
public interface PVLong extends PVScalar{
    /**
     * Get the <i>long</i> value stored in the field.
     * @return long value of field.
     */
    long get();
    /**
     * Put the <i>long</i> value into the field.
     * If the field is immutable a message is generated and the field not modified.
     * @param value New value.
     */
    void put(long value);
}
