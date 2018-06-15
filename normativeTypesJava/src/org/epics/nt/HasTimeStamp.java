/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.nt;

import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.property.PVTimeStamp;

/**
 * Interface for pvData type wrappers with, possibly optional, time stamp field.
 * <p>
 * The time stamp field should be a PVStructure conformant to the time stamp
 * type time_t described in the NormativeTypes specification, which may or
 * may not have field name "timeStamp"
 * @author dgh
 */
public interface HasTimeStamp
{
    /**
     * Attaches a PVTimeStamp to the time stamp field.
     * Will return false if there is no time stamp field.
     *
     * @param pvTimeStamp the PVTimeStamp that will be attached
     * @return true if the operation was successfull, otherwise false
     */
    public boolean attachTimeStamp(PVTimeStamp pvTimeStamp);

    /**
     * Returns the time stamp field.
     *
     * @return the time tamp field or null if there is no time stamp field
     */
    public PVStructure getTimeStamp();
}
