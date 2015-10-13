/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.property.PVDisplay;

/**
 * Interface for pvData type wrappers with, possibly optional, display field.
 * The display field should be a PVStructure conformant to the display
 * type display_t described in the NormativeTypes specification, which may or
 * may not have field name "display".
 * @author dgh
 */
public interface HasDisplay
{
     /**
      * Attach a PVDisplay.
      * Will return false if no display field.
      *
      * @param pvDisplay the PVDisplay that will be attached
      * @return true if the operation was successfull, otherwise false
      */
    public boolean attachDisplay(PVDisplay pvDisplay);

    /**
     * Get the display field.
     *
     * @return PVStructure which may be null
     */
    public PVStructure getDisplay();
}
