/**
 * 
 */
package org.epics.pvData.property;
import java.util.TreeMap;

import org.epics.pvData.misc.Enumerated;
import org.epics.pvData.pv.*;

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
        private PVBoolean pvAckTransient = null;
        private PVString pvAckSeverityChoice = null;
        private PVStringArray pvAckSeverityChoices = null;
        private PVInt pvAckSeverityIndex = null;
        
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
            pvAckTransient = pvStructure.getBooleanField("ackTransient");
            if(pvAckTransient==null) return false;
            pvStruct = pvStructure.getStructureField("ackSeverity");
            if(pvStruct==null) return false;
            enumerated = AlarmSeverity.getAlarmSeverity(pvStruct);
            if(enumerated==null) return false;
            pvAckSeverityChoice = enumerated.getChoice();
            pvAckSeverityChoices = enumerated.getChoices();
            pvAckSeverityIndex  = enumerated.getIndex();
            return true;
            
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.Alarm#getMessage()
         */
        @Override
        public PVString getAlarmMessage() {
            return pvMessage;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.Alarm#getSeverityChoice()
         */
        @Override
        public PVString getAlarmSeverityChoice() {
            return pvSeverityChoice;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.Alarm#getSeverityChoices()
         */
        @Override
        public PVStringArray getAlarmSeverityChoices() {
            return pvSeverityChoices;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.Alarm#getSeverityIndex()
         */
        @Override
        public PVInt getAlarmSeverityIndex() {
            return pvSeverityIndex;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.Alarm#getAckAlarmSeverityChoice()
         */
        @Override
        public PVString getAckAlarmSeverityChoice() {
            return pvAckSeverityChoice;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.Alarm#getAckAlarmSeverityChoices()
         */
        @Override
        public PVStringArray getAckAlarmSeverityChoices() {
            return pvAckSeverityChoices;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.Alarm#getAckAlarmSeverityIndex()
         */
        @Override
        public PVInt getAckAlarmSeverityIndex() {
            return pvAckSeverityIndex;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.Alarm#getAckTransient()
         */
        @Override
        public PVBoolean getAckTransient() {
            return null;
        }
        
    }
}
