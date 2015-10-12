/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.property;
import org.epics.pvdata.pv.PVField;

/**
 * PVAlarm. Attach to a PVData alarm structure.
 * Note that an alarm structure has the following structure:
 * <pre>
 *    structure alarm
 *        int severity
 *        int status
 *        string message
 * </pre>
 * This interface converts between the integer severity and the enum alarmSeverity.
 * @author mrk
 *
 */
public interface PVAlarm {
    /**
     * Attempt to attach to the alarm field.
     * The field must either be an alarm field itself or
     * a subfield of the parent of a field named value.
     * @param pvField The field for which to find an alarm field,
     * @return (false,true) if alarm field (not found, found).
     */
    boolean attach(PVField pvField);
    /**
     * Remove attachment to alarm field.
     */
    void detach();
    /**
     * Is this attached to an alarm structure.
     * @return (false,true) is (not, is) attached to an alarm structure.
     */
    boolean isAttached();
    /**
     * Get the alarm values from the attached alarm field and write to the specified Alarm.
     * @param alarm the Alarm to be updated
     * @throws IllegalStateException if this PVAlarm not attached to an alarm field 
     */
    void get(Alarm alarm);
    /**
     * Set the alarm.
     * @param alarm The new value.
     * @return (false,true) if the alarm field is (immutable,updated).
     */
    boolean set(Alarm alarm);
}
