/**
 * 
 */
package org.epics.pvData.property;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.ScalarType;
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
    	PVString pvMessage = null;
        PVInt alarmSeverity = null;
        if(!pvField.getField().getFieldName().equals("alarm")) return null;
        if(pvField.getField().getType()!=Type.structure) return null;
        PVStructure pvStructure = (PVStructure)pvField;
        PVField pvf = pvStructure.getSubField("severity");
        if(pvf==null) return null;
        if(pvf.getField().getType()!=Type.scalar) return null;
        PVScalar pvs = (PVScalar)pvf;
        if(pvs.getScalar().getScalarType()!=ScalarType.pvInt) return null;
        alarmSeverity = (PVInt)pvf;
        pvf = pvStructure.getSubField("message");
        if(pvf==null) return null;
        if(pvf.getField().getType()!=Type.scalar) return null;
        pvs = (PVScalar)pvf;
        if(pvs.getScalar().getScalarType()!=ScalarType.pvString) return null;
        pvMessage = (PVString)pvf;
        return new AlarmImpl(pvMessage,alarmSeverity);
    }
    private static class AlarmImpl implements Alarm {
        private final PVString pvMessage;
        private final PVInt alarmSeverity;
        
        private AlarmImpl(PVString pvMessage,PVInt alarmSeverity) {
        	this.pvMessage = pvMessage;
            this.alarmSeverity = alarmSeverity;
        }
        		/* (non-Javadoc)
		 * @see org.epics.pvData.property.Alarm#getMessage()
		 */
		@Override
		public String getMessage() {
			return pvMessage.get();
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.property.Alarm#getSeverity()
		 */
		@Override
		public int getSeverity() {
			return alarmSeverity.get();
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.property.Alarm#setMessage(java.lang.String)
		 */
		@Override
		public void setMessage(String message) {
			pvMessage.put(message);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.property.Alarm#setSeverity(org.epics.pvData.property.AlarmSeverity)
		 */
		@Override
		public void setSeverity(AlarmSeverity alarmSeverity) {
			this.alarmSeverity.put(alarmSeverity.ordinal());
		}
        
    }
}
