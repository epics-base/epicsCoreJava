/**
 * 
 */
package org.epics.pvData.property;
import java.util.TreeMap;

import org.epics.pvData.misc.Enumerated;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStringArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Type;

/**
 * Factory that creates Alarm
 * @author mrk
 *
 */
public class AlarmFactory {
    /**
     * If pvField is an alarm structure then create and return 
     * @param pvField
     * @return interface
     */
    public static Alarm getAlarm(PVField pvField) {
        String key = pvField.getFullName();
        Alarm alarm = alarmMap.get(key);
        if(alarm!=null) return alarm;
        AlarmImpl alarmImpl = new AlarmImpl();
        if(alarmImpl.isAlarm(pvField)) {
            alarmMap.put(key, alarmImpl);
            return alarmImpl;
        }
        return null;
    }
    private static TreeMap<String,Alarm> alarmMap = new TreeMap<String,Alarm>();
    private static class AlarmImpl implements Alarm {
        private PVString pvMessage = null;
        private PVString pvSeverityChoice = null;
        private PVStringArray pvSeverityChoices = null;
        private PVInt pvSeverityIndex = null;
        
        private boolean isAlarm(PVField pvField) {
            if(pvField.getField().getType()!=Type.structure) return false;
            PVStructure pvStructure = (PVStructure)pvField;
            PVStructure pvStruct = pvStructure.getStructureField("severity");
            if(pvStruct==null) return false;
            Enumerated enumerated = AlarmSeverity.getAlarmSeverity(pvStruct);
            if(enumerated==null) return false;
            pvSeverityChoice = enumerated.getChoice();
            pvSeverityChoices = enumerated.getChoices();
            pvSeverityIndex  = enumerated.getIndex();
            pvMessage = pvStructure.getStringField("message");
            if(pvMessage==null) return false;
            return true;
            
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.Alarm#getMessage()
         */
        public PVString getAlarmMessage() {
            return pvMessage;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.Alarm#getSeverityChoice()
         */
        public PVString getAlarmSeverityChoice() {
            return pvSeverityChoice;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.Alarm#getSeverityChoices()
         */
        public PVStringArray getAlarmSeverityChoices() {
            return pvSeverityChoices;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.Alarm#getSeverityIndex()
         */
        public PVInt getAlarmSeverityIndex() {
            return pvSeverityIndex;
        }
        
    }
}
