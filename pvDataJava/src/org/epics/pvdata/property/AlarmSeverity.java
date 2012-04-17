/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
     * get the alarm severity.
     * @param value the integer value.
     * @return The alarm severity.
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
    public static String[] getSeverityNames() { return alarmSeverityNames;}
}
