/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.property;


/**
 * Convenience interface for an alarm structure.
 * @author mrk
 *
 */
public interface Alarm {
    /**
     * Get the alarm message.
     * @return The value.
     */
    String getMessage();
    /**
     * Set the alarm message.
     * @param message The message.
     */
    void setMessage(String message);
    /**
     * Get the alarm severity.
     * @return The value.
     */
    int getSeverity();
    /**
     * Set the alarm severity.
     * If an invalid value is given than an exception is thrown.
     * @param alarmSeverity The severity.
     */
    void setSeverity(AlarmSeverity alarmSeverity);
}
