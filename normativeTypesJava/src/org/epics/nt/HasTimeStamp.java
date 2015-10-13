/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.property.PVTimeStamp;

/**
 * Interface for pvData type wrappers with, possibly optional, time stamp field.
 * The time stamp field should be a PVStructure conformant to the time stamp
 * type time_t described in the NormativeTypes specification, which may or
 * may not have field name "timeStamp"
 * @author dgh
 */
public interface HasTimeStamp
{
     /**
      * Attach a PVTimeStamp.
      * Will return false if no time stamp field.
      *
      * @param pvTimeStamp the PVTimeStamp that will be attached
      * @return true if the operation was successfull, otherwise false
      */
    public boolean attachTimeStamp(PVTimeStamp pvTimeStamp);

    /**
     * Get the timeStamp.
     *
     * @return the time stamp PVStructure, which may be null
     */
    public PVStructure getTimeStamp();
}
