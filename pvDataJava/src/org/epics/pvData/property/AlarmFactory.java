/**
 * 
 */
package org.epics.pvData.property;
import org.epics.pvData.misc.Enumerated;
import org.epics.pvData.pv.PVBoolean;
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
        AlarmImpl alarmImpl = new AlarmImpl();
        if(alarmImpl.isAlarm(pvField)) {
            return alarmImpl;
        }
        return null;
    }
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
