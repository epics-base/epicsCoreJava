/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.property;


/**
 * Convenience class for an alarm.
 * @author mrk
 *
 */
public final class Alarm {
    /**
     * Constructor
     */
    public Alarm() {}

    /**
     * Get the alarm message.
     *
     * @return the value
     */
    public String getMessage() {return message;}

    /**
     * Set the alarm message.
     *
     * @param message the message
     */
    public void setMessage(String message) {this.message = message;}

    /**
     * Get the alarm severity.
     *
     * @return the value
     */
    public AlarmSeverity getSeverity() {return severity;}

    /**
     * Set the alarm severity.
     *
     * If an invalid value is given than an exception is thrown.
     * @param alarmSeverity the severity
     */
    public void setSeverity(AlarmSeverity alarmSeverity) {this.severity = alarmSeverity;}

    /**
     * Get the alarm status.
     *
     * @return the value
     */
    public AlarmStatus getStatus() {return status;}

    /**
     * Set the alarm status.
     * If an invalid value is given than an exception is thrown.
     *
     * @param alarmStatus the status
     */
    public void setStatus(AlarmStatus alarmStatus) {this.status = alarmStatus;}
    
    private String message = "";
    private AlarmSeverity severity = AlarmSeverity.NONE;
    private AlarmStatus status = AlarmStatus.NONE;
}
