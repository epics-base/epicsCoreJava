/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.property;

import org.epics.pvdata.pv.PVField;

/**
 * Interface for attaching a Control to a control structure.
 * @author mrk
 *
 */
public interface PVControl {
    /**
     * Attempt to attach to a control field.
     * The field must either be an control field itself
     * or the field must be named value and a control is found somewhere up
     * the parent tree.
     *
     * @param pvField The field for which to find a control field,
     * @return (false,true) if control field (not found, found).
     */
    boolean attach(PVField pvField);

    /**
     * Remove attachment to control field.
     */
    void detach();

    /**
     * Is this attached to a control structure.
     * @return (false,true) is (not, is) attached to a control structure
     */
    boolean isAttached();
    /**
     * Get the control values from the attached control field and write to the
     * specified Control.
     *
     * @param control the Control to be updated
     * @throws IllegalStateException if this PVControl not attached to a control field 
     */
    void get(Control control);

    /**
     * Set the control.
     *
     * @param control the new value
     * @return (false,true) if the control field is (immutable,updated)
     */
    boolean set(Control control);

}
