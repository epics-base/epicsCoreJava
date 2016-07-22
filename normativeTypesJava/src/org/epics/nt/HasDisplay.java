/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.nt;

import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.property.PVDisplay;

/**
 * Interface for pvData type wrappers with, possibly optional, display field.
 * <p>
 * The display field should be a PVStructure conformant to the display
 * type display_t described in the NormativeTypes specification, which may or
 * may not have field name "display".
 * @author dgh
 */
public interface HasDisplay
{
    /**
     * Attaches a PVDisplay to the display field.
     * Will return false if there is no display field.
     *
     * @param pvDisplay the PVDisplay that will be attached
     * @return true if the operation was successfull, otherwise false
     */
    public boolean attachDisplay(PVDisplay pvDisplay);

    /**
     * Returns the display field.
     *
     * @return the display field or null if there is no display field
     */
    public PVStructure getDisplay();
}
