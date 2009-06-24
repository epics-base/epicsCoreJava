/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.property;

import org.epics.pvData.pv.PVBoolean;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStringArray;

/**
 * Convenience interface for an alarm structure.
 * @author mrk
 *
 */
public interface Alarm {
    /**
     * Get the interface for the alarm message..
     * @return The interface.
     */
    PVString getAlarmMessage();
    /**
     * Get the interface for the severity index.
     * @return The interface.
     */
    PVInt getAlarmSeverityIndex();
    /**
     * Get the value of the alarm severity.
     * @return The string value for the severity.
     */
    String getAlarmSeverityChoice();
    /**
     * Get the interface for the alarm severity choices.
     * @return The interface.
     */
    PVStringArray getAlarmSeverityChoices();
    /**
     * Get the interface for acknowledge transient alarm.
     * @return The interface.
     */
    PVBoolean getAckTransient();
    /**
     * Get the interface for the acknowledge severity index.
     * @return The interface.
     */
    PVInt getAckAlarmSeverityIndex();
    /**
     * Get the value of the acknowledge severity.
     * @return The string value.
     */
    String getAckAlarmSeverityChoice();
    /**
     * Get the interface for the acknowledge alarm severity choices.
     * @return The interface.
     */
    PVStringArray getAckAlarmSeverityChoices();
}
