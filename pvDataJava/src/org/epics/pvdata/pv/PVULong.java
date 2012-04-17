/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;

/**
 * Get/put long data.
 * Since Java does not support unsigned the actual arguments are signed.
 * Code that calls methods of the class is responsible for integer overflow problems.
 * @author mrk
 *
 */
public interface PVULong extends PVScalar{
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
