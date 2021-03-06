/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.property;


/**
 * AlarmSeverity definitions.
 * @author mrk
 *
 */
public enum AlarmSeverity {
    /**
     * Not in alarm.
     */
    NONE,
    /**
     * Minor alarm.
     */
    MINOR,
    /**
     * Major Alarm.
     */
    MAJOR,
    /**
     * The value is invalid.
     */
    INVALID,
    /**
     * The value is undefined.
     */
    UNDEFINED;
    
    /**
     * Get the alarm severity.
     *
     * @param value the integer value
     * @return the alarm severity
     */
    public static AlarmSeverity getSeverity(int value) {
        switch(value) {
        case 0: return AlarmSeverity.NONE;
        case 1: return AlarmSeverity.MINOR;
        case 2: return AlarmSeverity.MAJOR;
        case 3: return AlarmSeverity.INVALID;
        case 4: return AlarmSeverity.UNDEFINED;
        }
        throw new IllegalArgumentException("AlarmSeverity.getSeverity("
            + ((Integer)value).toString() + ") is not a valid AlarmSeverity");
    }
    
    private static final String[] alarmSeverityNames = {
        "NONE","MINOR","MAJOR","INVALID","UNDEFINED"
    };

    /**
     * Get the names associated with each severity.
     *
     * @return the array of names
     */
    public static String[] getSeverityNames() { return alarmSeverityNames;}
}
