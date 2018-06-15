/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.nt;

import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.property.PVAlarm;

/**
 * Interface for pvData type wrappers with, possibly optional, alarm field.
 * <p>
 * The alarm field should be a PVStructure conformant to the alarm
 * type alarm_t described in the NormativeTypes specification, which may or
 * may not have field name "alarm".
 * @author dgh
 */
public interface HasAlarm
{
     /**
      * Attaches a PVAlarm to an alarm field.
      * Will return false if there is no alarm field.
      *
      * @param pvAlarm the PVAlarm to be attached
      * @return true if the operation was successfull, otherwise false
      */
    public boolean attachAlarm(PVAlarm pvAlarm);

    /**
     * Returns the alarm field.
     *
     * @return the alarm field or null if there is no alarm field
     */
    public PVStructure getAlarm();
}
