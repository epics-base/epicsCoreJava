/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.property.PVControl;

/**
 * Interface for pvData type wrappers with, possibly optional, control field.
 * <p>
 * The control field should be a PVStructure conformant to the control
 * type control_t described in the NormativeTypes specification, which may or
 * may not have field name "control".
 * @author dgh
 */
public interface HasControl
{
    /**
     * Attaches a PVControl to the control field.
     * Will return false if there is no control field.
     *
     * @param pvControl the PVControl to be attached
     * @return true if the operation was successfull, otherwise false
     */
    public boolean attachControl(PVControl pvControl);

    /**
     * Returns the control field.
     *
     * @return the control field or null if there is no control field
     */
    public PVStructure getControl();
}
