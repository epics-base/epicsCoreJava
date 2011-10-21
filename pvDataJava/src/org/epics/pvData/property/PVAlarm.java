/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.property;
import org.epics.pvData.pv.PVField;

/**
 * PVAlarm. Attach to a PVData alarm structure.
 * Note that an alarm structure has the following structure:
 * <pre>
 *    structure alarm
 *        int severity
 *        int status
 *        string message
 * </pre>
 * This interface converts betweeen the integer severity and the enum alarmSeverity.
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
     * Get the alarm. A logic error exception will be thrown if not attached to an alarm field.
     */
    void get(Alarm alarm);
    /**
     * Set the alarm.
     * @param alarm The new value.
     * @return (false,true) if the alarm field is (immutable,updated).
     */
    boolean set(Alarm alarm);
}
