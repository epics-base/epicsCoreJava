/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.property;


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
     * @return The value.
     */
    public String getMessage() {return message;}
    /**
     * Set the alarm message.
     * @param message The message.
     */
    public void setMessage(String message) {this.message = message;}
    /**
     * Get the alarm severity.
     * @return The value.
     */
    public AlarmSeverity getSeverity() {return severity;}
    /**
     * Set the alarm severity.
     * If an invalid value is given than an exception is thrown.
     * @param alarmSeverity The severity.
     */
    public void setSeverity(AlarmSeverity alarmSeverity) {this.severity = alarmSeverity;}
    /**
     * Get the alarm status.
     * @return The value.
     */
    public AlarmStatus getStatus() {return status;}
    /**
     * Set the alarm status.
     * If an invalid value is given than an exception is thrown.
     * @param alarmStatus The status.
     */
    public void setStatus(AlarmStatus alarmStatus) {this.status = alarmStatus;}
    
    private String message = "";
    private AlarmSeverity severity = AlarmSeverity.NONE;
    private AlarmStatus status = AlarmStatus.NONE;
}
