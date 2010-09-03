/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.property;


/**
 * AlarmSeverity definitions.
 * @author mrk
 *
 */
public enum AlarmSeverity {
    /**
     * Not in alarm.
     */
    none,
    /**
     * Minor alarm.
     */
    minor,
    /**
     * Major Alarm.
     */
    major,
    /**
     * The value is invalid.
     */
    invalid;
    
    /**
     * get the alarm severity.
     * @param value the integer value.
     * @return The alarm severity.
     */
    public static AlarmSeverity getSeverity(int value) {
        switch(value) {
        case 0: return AlarmSeverity.none;
        case 1: return AlarmSeverity.minor;
        case 2: return AlarmSeverity.major;
        case 3: return AlarmSeverity.invalid;
        }
        throw new IllegalArgumentException("AlarmSeverity.getSeverity("
            + ((Integer)value).toString() + ") is not a valid AlarmSeverity");
    }
    
    private static final String[] alarmSeverityNames = {
        "none","minor","major","invalid"
    };
    public static String[] getSeverityNames() { return alarmSeverityNames;}
}
