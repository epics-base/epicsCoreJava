/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.property;

import org.epics.pvData.pv.PVField;

/**
 * Interface for attaching a TimeStamp to a timeStamp structure.
 * @author mrk
 *
 */
public interface PVTimeStamp {
    /**
     * Attempt to attach to a timeStamp field.
     * The field must either be an timeStamp field itself
     * or the field must be named value and a timeStamp is found somewhere up the parent tree.
     * @param pvField The field for which to find a timeStamp field,
     * @return (false,true) if timeStamp field (not found, found).
     */
    boolean attach(PVField pvField);
    /**
     * Remove attachment to timeStamp field.
     */
    void detach();
    /**
     * Is this attached to a timeStamp structure.
     * @return (false,true) is (not, is) attached to a timeStamp structure.
     */
    boolean isAttached();
    /**
     * Get the TimeStamp. A logic error exception will be thrown if not attached to a timeStamp field.
     */
    void get(TimeStamp timeStamp);
    /**
     * Set the timeStamp.
     * @param timeStamp The new value.
     * @return (false,true) if the timeStamp field is (immutable,updated).
     */
    boolean set(TimeStamp timeStamp);
}
