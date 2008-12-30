/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

/**
 * Get/put double data
 * @author mrk
 *
 */
public interface PVDouble extends PVScalar{
    /**
     * Get the <i>double</i> value stored in the field.
     * @return double The value.
     */
    double get();
    /**
     * Put the <i>double</i> value into the field.
     * @param value New value.
     * @throws IllegalStateException if the field is not mutable.
     */
    void put(double value);
}
