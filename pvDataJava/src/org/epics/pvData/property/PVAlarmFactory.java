/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.property;

import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Type;

public final class PVAlarmFactory implements PVAlarm{
    private PVInt pvSeverity = null;
    private PVInt pvStatus = null;
    private PVString pvMessage = null;
    private static final String noAlarmFound = "No alarm structure was located";
    private static final String notAttached = "Not attached to an alarm structure";

    /**
     * Create a PVAlarm.
     * @return The newly created PVAlarm.
     */
    public static PVAlarm create() { return new PVAlarmFactory();} 
    /* (non-Javadoc)
     * @see org.epics.pvData.property.PVAlarm#attach(org.epics.pvData.pv.PVField)
     */
    @Override
    public boolean attach(PVField pvField) {
        PVStructure pvStructure = null;
        if(!pvField.getField().getFieldName().equals("alarm")) {
            if(!pvField.getField().getFieldName().equals("value")) {
                pvField.message(noAlarmFound,MessageType.error);
                return false;
            }
            PVStructure pvParent = pvField.getParent();
            if(pvParent==null) {
                pvField.message(noAlarmFound,MessageType.error);
                return false;
            }
            pvStructure = pvParent.getStructureField("alarm");
            if(pvStructure==null) {
                pvField.message(noAlarmFound,MessageType.error);
                return false;
            }
        } else {
            if(pvField.getField().getType()!=Type.structure) {
                pvField.message(noAlarmFound,MessageType.error);
                return false;
            }
            pvStructure = (PVStructure)(pvField);
        }
        PVInt pvInt = pvStructure.getIntField("severity");
        if(pvInt==null) {
            pvField.message(noAlarmFound,MessageType.error);
            return false;
        }
        pvSeverity = pvInt;
        pvInt = pvStructure.getIntField("status");
        if(pvInt==null) {
            pvField.message(noAlarmFound,MessageType.error);
            return false;
        }
        pvStatus = pvInt;
        PVString pvString = pvStructure.getStringField("message");
        if(pvString==null) {
            pvField.message(noAlarmFound,MessageType.error);
            return false;
        }
        pvMessage = pvString;
        return true;

    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.PVAlarm#detach()
     */
    @Override
    public void detach() {
        pvSeverity = null;
        pvMessage = null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.PVAlarm#isAttached()
     */
    @Override
    public boolean isAttached() {
        if(pvSeverity==null || pvMessage==null) return false;
        return true;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.PVAlarm#get(org.epics.pvData.property.Alarm)
     */
    @Override
    public void get(Alarm alarm) {
        if(pvSeverity==null || pvMessage==null) {
            throw new IllegalStateException(notAttached);
        }
        alarm.setSeverity(AlarmSeverity.getSeverity(pvSeverity.get()));
        alarm.setStatus(AlarmStatus.getStatus(pvStatus.get()));
        alarm.setMessage(pvMessage.get());
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.PVAlarm#set(org.epics.pvData.property.Alarm)
     */
    @Override
    public boolean set(Alarm alarm) {
        if(pvSeverity==null || pvMessage==null) {
            throw new IllegalStateException(notAttached);
        }
        if(pvSeverity.isImmutable() || pvMessage.isImmutable()) return false;
        pvSeverity.put(alarm.getSeverity().ordinal());
        pvStatus.put(alarm.getStatus().ordinal());
        pvMessage.put(alarm.getMessage());
        return true;
    }

}
