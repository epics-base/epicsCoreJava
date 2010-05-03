/**
 * 
 */
package org.epics.pvData.property;
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
        AlarmImpl alarmImpl = new AlarmImpl();
        if(alarmImpl.isAlarm(pvField)) {
            return alarmImpl;
        }
        return null;
    }
    private static class AlarmImpl implements Alarm {
        private PVString pvMessage = null;
        private Enumerated alarmSeverity = null;
        
        private boolean isAlarm(PVField pvField) {
            if(pvField.getField().getType()!=Type.structure) return false;
            PVStructure pvStructure = (PVStructure)pvField;
            PVStructure pvStruct = pvStructure.getStructureField("severity");
            if(pvStruct==null) return false;
            Enumerated enumerated = AlarmSeverity.getAlarmSeverity(pvStruct);
            if(enumerated==null) return false;
            alarmSeverity = enumerated;
            if(pvStructure.getSubField("message")==null) return false;
            pvMessage = pvStructure.getStringField("message");
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
        public String getAlarmSeverityChoice() {
            return alarmSeverity.getChoice();
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.Alarm#getSeverityChoices()
         */
        @Override
        public PVStringArray getAlarmSeverityChoices() {
            return alarmSeverity.getChoices();
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.property.Alarm#getSeverityIndex()
         */
        @Override
        public PVInt getAlarmSeverityIndex() {
            return alarmSeverity.getIndex();
        }
    }
}
