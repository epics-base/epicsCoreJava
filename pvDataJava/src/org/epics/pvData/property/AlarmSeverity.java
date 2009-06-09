/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.property;

import org.epics.pvData.misc.Enumerated;
import org.epics.pvData.misc.EnumeratedFactory;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVStringArray;
import org.epics.pvData.pv.StringArrayData;

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
    
    private static final String[] alarmSeverityChoices = {
        "none","minor","major","invalid"
    };
    /**
     * Convenience method for code the raises alarms.
     * @param pvField A field which is potentially an alarmSeverity structure.
     * @return The Enumerated interface only if dbField has an Enumerated interface and defines
     * the alarmSeverity choices.
     */
    public static Enumerated getAlarmSeverity(PVField pvField) {
        Enumerated enumerated = EnumeratedFactory.getEnumerated(pvField);
        if(enumerated==null) {
            pvField.message("interface Enumerated not found", MessageType.error);
            return null;
        }
        PVStringArray pvChoices = enumerated.getChoices();
        int len = pvChoices.getLength();
        if(len!=alarmSeverityChoices.length) {
            pvField.message("not an alarmSeverity structure", MessageType.error);
            return null;
        }
        StringArrayData data = new StringArrayData();
        pvChoices.get(0, len, data);
        String[] choices = data.data;
        for (int i=0; i<len; i++) {
            if(!choices[i].equals(alarmSeverityChoices[i])) {
                pvField.message("not an alarmSeverity structure", MessageType.error);
                return null;
            }
        }
        return enumerated;
    }
}
