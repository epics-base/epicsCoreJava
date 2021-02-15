/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

/**
 * A value that provides an alarm.
 *
 * @author carcassi
 */
public interface AlarmProvider {

    /**
     * The alarm associated with this value.
     *
     * @return the alarm; not null
     */
    public Alarm getAlarm();
}
