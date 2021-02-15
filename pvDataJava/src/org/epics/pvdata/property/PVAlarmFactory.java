/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.property;

import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Type;

/**
 * Implementation of PVAlarm.
 * @author mrk
 *
 */
public final class PVAlarmFactory implements PVAlarm{
    private PVInt pvSeverity = null;
    private PVInt pvStatus = null;
    private PVString pvMessage = null;
    private static final String noAlarmFound = "No alarm structure was located";
    private static final String notAttached = "Not attached to an alarm structure";

    /**
     * Create a PVAlarm.
     *
     * @return the newly created PVAlarm
     */
    public static PVAlarm create() { return new PVAlarmFactory();}

    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVAlarm#attach(org.epics.pvdata.pv.PVField)
     */
    public boolean attach(PVField pvField) {
        if(pvField.getField().getType()!=Type.structure) {
            throw new IllegalArgumentException(noAlarmFound);
        }
        PVStructure pvStructure = (PVStructure)(pvField);
        boolean again = true;
        while(true) {
            PVField xxx = pvStructure.getSubField("severity");
            if(xxx!=null) xxx = pvStructure.getSubField("status");
            if(xxx!=null) xxx = pvStructure.getSubField("message");
            if(xxx!=null) {
                pvSeverity = pvStructure.getIntField("severity");
                pvStatus = pvStructure.getIntField("status");
                pvMessage = pvStructure.getStringField("message");
            }
            if(pvSeverity!=null && pvStatus!=null && pvMessage!=null) return true;
            if(!again) break;
            pvStructure = pvStructure.getParent();
            if(pvStructure==null) break;
            again = false;
        }
        pvSeverity = null;
        pvStatus = null;
        pvMessage = null;
        return false;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVAlarm#detach()
     */
    public void detach() {
        pvSeverity = null;
        pvMessage = null;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVAlarm#isAttached()
     */
    public boolean isAttached() {
        if(pvSeverity==null || pvMessage==null) return false;
        return true;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVAlarm#get(org.epics.pvdata.property.Alarm)
     */
    public void get(Alarm alarm) {
        if(pvSeverity==null || pvMessage==null) {
            throw new IllegalStateException(notAttached);
        }
        alarm.setSeverity(AlarmSeverity.getSeverity(pvSeverity.get()));
        alarm.setStatus(AlarmStatus.getStatus(pvStatus.get()));
        alarm.setMessage(pvMessage.get());
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.PVAlarm#set(org.epics.pvdata.property.Alarm)
     */
    public boolean set(Alarm alarm) {
        if(pvSeverity==null || pvMessage==null) {
            throw new IllegalStateException(notAttached);
        }
        if(pvSeverity.isImmutable() || pvMessage.isImmutable()) return false;
        Alarm current = new Alarm();
        get(current);
        boolean returnValue = false;
        if(current.getSeverity()!=alarm.getSeverity())
        {
            pvSeverity.put(alarm.getSeverity().ordinal());
            returnValue = true;
        }
        if(current.getStatus()!=alarm.getStatus())
        {
            pvStatus.put(alarm.getStatus().ordinal());
            returnValue = true;
        }
        if(!current.getMessage().equals(alarm.getMessage()))
        {
            pvMessage.put(alarm.getMessage());
            returnValue = true;
        }
        return returnValue;
    }

}
