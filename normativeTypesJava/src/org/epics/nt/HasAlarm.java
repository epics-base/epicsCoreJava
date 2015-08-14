/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.nt;

import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.property.PVAlarm;

/**
 * Interface for pvData type wrappers with, possibly optional, alarm field.
 * The alarm field should be a PVStructure conformant to the alarm
 * type alarm_t described in the NormativeTypes specification, which may or
 * may not have field name "alarm".
 * @author dgh
 */
public interface HasAlarm
{
     /**
      * Attach a PVAlarm.
      *
      * Will return false if no alarm field.
      * @param pvAlarm The PVAlarm that will be attached.
      * @return true if the operation was successfull, otherwise false.
      */
    public boolean attachAlarm(PVAlarm pvAlarm);

    /**
     * Get the alarm field.
     * @return PVStructure which may be null.
     */
    public PVStructure getAlarm();
}
