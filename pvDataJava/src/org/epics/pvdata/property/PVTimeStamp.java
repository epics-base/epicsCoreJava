/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.property;

import org.epics.pvdata.pv.PVField;

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
     * 
     * @param pvField the field for which to find a timeStamp field
     * @return (false,true) if timeStamp field (not found, found)
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
     * Get the time stamp values from the attached time stamp field and write to the specified TimeStamp.
     *
     * @param timeStamp the TimeStamp to be updated
     * @throws IllegalStateException if this PVTimeStamp not attached to a time stamp field 
     */
    void get(TimeStamp timeStamp);

    /**
     * Set the timeStamp.
     * 
     * @param timeStamp the new value
     * @return (false,true) if the timeStamp field is (immutable,updated)
     */
    boolean set(TimeStamp timeStamp);
}
