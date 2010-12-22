/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.property;

import org.epics.pvData.pv.PVField;

/**
 * Interface for attaching a Display to a display structure.
 * @author mrk
 *
 */
public interface PVDisplay {
    /**
     * Attempt to attach to a display field.
     * The field must either be an display field itself
     * or the field must be named value and a display is found somewhere up the parent tree.
     * @param pvField The field for which to find a display field,
     * @return (false,true) if display field (not found, found).
     */
    boolean attach(PVField pvField);
    /**
     * Remove attachment to display field.
     */
    void detach();
    /**
     * Is this attached to a display structure.
     * @return (false,true) is (not, is) attached to a display structure.
     */
    boolean isAttached();
    /**
     * Get the Display. A logic error exception will be thrown if not attached to a display field.
     */
    void get(Display display);
    /**
     * Set the display.
     * @param display The new value.
     * @return (false,true) if the display field is (immutable,updated).
     */
    boolean set(Display display);
}
