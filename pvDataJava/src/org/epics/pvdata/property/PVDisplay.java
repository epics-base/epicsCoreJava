/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.property;

import org.epics.pvdata.pv.PVField;

/**
 * Interface for attaching a Display to a display structure.
 * @author mrk
 *
 */
public interface PVDisplay {
    /**
     * Attempt to attach to a display field.
     * The field must either be an display field itself
     * or the field must be named value and a display is found somewhere up
     * the parent tree.
     *
     * @param pvField the field for which to find a display field
     * @return (false,true) if display field (not found, found)
     */
    boolean attach(PVField pvField);

    /**
     * Remove attachment to display field.
     */
    void detach();

    /**
     * Is this attached to a display structure.
     *
     * @return (false,true) is (not, is) attached to a display structure.
     */
    boolean isAttached();

    /**
     * Get the display values from the attached display field and write to the specified Display.
     *
     * @param display the Display to be updated
     * @throws IllegalStateException if this PVDisplay not attached to a display field
     */
    void get(Display display);

    /**
     * Set the display.
     *
     * @param display the new value
     * @return (false,true) if the display field is (immutable,updated).
     */
    boolean set(Display display);
}
