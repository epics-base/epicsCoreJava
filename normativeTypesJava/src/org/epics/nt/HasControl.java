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
 * The control field should be a PVStructure conformant to the control
 * type control_t described in the NormativeTypes specification, which may or
 * may not have field name "control".
 * @author dgh
 */
public interface HasControl
{
     /**
      * Attach a PVControl.
      *
      * Will return false if no control field.
      * @param pvControl The PVControl that will be attached.
      * @return true if the operation was successfull, otherwise false.
      */
    public boolean attachControl(PVControl pvControl);

    /**
     * Get the control field.
     * @return PVStructure which may be null.
     */
    public PVStructure getControl();
}
