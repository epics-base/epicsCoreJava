/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.compat.legacy.lang.Objects;

import java.util.List;

/**
 * Alarm information. Represents the severity and name of the highest alarm
 * associated with the channel.
 *
 * @author carcassi
 */
public abstract class Alarm {

    /**
     * Returns the alarm severity, which describes the quality of the
     * value returned. Never null.
     *
     * @return the alarm severity
     */
    public abstract AlarmSeverity getSeverity();

    /**
     * Returns the alarm status, which returns the source of the alarm.
     * Never null.
     *
     * @return the alarm status
     */
    public abstract AlarmStatus getStatus();

    /**
     * Returns a brief text representation of the highest currently active alarm.
     * Never null.
     *
     * @return the alarm status
     */
    public abstract String getName();

    /**
     * Tests whether the give object is and Alarm with the same name and severity.
     *
     * @param obj another alarm
     * @return true if equal
     */
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

	if (obj instanceof Alarm) {
            Alarm other = (Alarm) obj;

            return getSeverity().equals(other.getSeverity()) &&
                    getStatus().equals(other.getStatus()) &&
                    getName().equals(other.getName());
        }

        return false;
    }

    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(getSeverity());
        hash = 23 * hash + Objects.hashCode(getStatus());
        hash = 23 * hash + Objects.hashCode(getName());
        return hash;
    }

    @Override
    public final String toString() {
        return getSeverity() + "(" + getStatus()+ ") - " + getName();
    }

    /**
     * New alarm with the given severity and status.
     *
     * @param severity the alarm severity
     * @param status the alarm status
     * @param name the alarm name
     * @return the new alarm
     */
    public static Alarm of(final AlarmSeverity severity, final AlarmStatus status, final String name) {
        return new IAlarm(severity, status, name);
    }

    private static final Alarm NONE = of(AlarmSeverity.NONE, AlarmStatus.NONE, "None");
    private static final Alarm HIHI = of(AlarmSeverity.MAJOR, AlarmStatus.RECORD, "HIHI");
    private static final Alarm HIGH = of(AlarmSeverity.MINOR, AlarmStatus.RECORD, "HIGH");
    private static final Alarm LOW = of(AlarmSeverity.MINOR, AlarmStatus.RECORD, "LOW");
    private static final Alarm LOLO = of(AlarmSeverity.MAJOR, AlarmStatus.RECORD, "LOLO");
    private static final Alarm NO_VALUE = of(AlarmSeverity.INVALID, AlarmStatus.CLIENT, "No value");
    private static final Alarm DISCONNECTED = of(AlarmSeverity.INVALID, AlarmStatus.CLIENT, "Disconnected");

    /**
     * No alarm. To be used whenever there is no alarm associated with the value.
     * <p>
     * To test for no alarm, one should always check the severity, and not
     * equality to this specific alarm: depending on the data source the status
     * may be different and contain extra information.
     *
     * @return severity NONE and status "None"
     */
    public static Alarm none() {
        return NONE;
    }

    /**
     * Alarm condition for when a value is above the high alarm threshold.
     *
     * @return the HIHI alarm
     */
    public static Alarm hihi() {
        return HIHI;
    }

    /**
     * Alarm condition for when a value is above the high warning threshold.
     *
     * @return the HIGH alarm
     */
    public static Alarm high() {
        return HIGH;
    }

    /**
     * Alarm condition for when a value is below the low warning threshold.
     *
     * @return the LOW alarm
     */
    public static Alarm low() {
        return LOW;
    }

    /**
     * Alarm condition for when a value is below the low alarm threshold.
     *
     * @return the LOLO alarm
     */
    public static Alarm lolo() {
        return LOLO;
    }

    /**
     * Alarm condition for when a value is not present. To be used as
     * the alarm associated to a null value.
     *
     * @return severity INVALID and status "No Value"
     */
    public static Alarm noValue() {
        return NO_VALUE;
    }

    /**
     * Alarm condition for when a channel is disconnected. To be used as
     * the alarm associated with a broken connection.
     *
     * @return severity UNDEFINED and status "Disconnected"
     */
    public static Alarm disconnected() {
        return DISCONNECTED;
    }

    /**
     * Returns the value with highest severity. null values can either be ignored or
     * treated as disconnected/missing value ({@link Alarm#noValue()}).
     *
     * @param values a list of values
     * @param ignoreNull true to simply skip null values
     * @return the value with highest alarm; can't be null
     */
    public static Alarm highestAlarmOf(final List<?> values, final boolean ignoreNull) {
        Alarm finalAlarm = Alarm.none();
        for (Object value : values) {
            Alarm newAlarm;
            if (value == null && !ignoreNull) {
                newAlarm = Alarm.noValue();
            } else {
                newAlarm = Alarm.none();
                if (value instanceof AlarmProvider) {
                    newAlarm = ((AlarmProvider) value).getAlarm();
                }
            }
            if (newAlarm.getSeverity().compareTo(finalAlarm.getSeverity()) > 0) {
                finalAlarm = newAlarm;
            }
        }
        return finalAlarm;
    }

    /**
     * Null and non-VType safe utility to extract alarm information.
     * <ul>
     * <li>If the value is has an alarm, the associated alarm is returned.</li>
     * <li>If the value is does not have an alarm, {@link Alarm#none()} is returned.</li>
     * <li>If the value is null, {@link Alarm#noValue()} is returned.</li>
     * </ul>
     *
     * @param value the value
     * @return the alarm information for the value
     */
    public static Alarm alarmOf(Object value) {
        return alarmOf(value, true);
    }

    /**
     * Null and non-VType safe utility to extract alarm information for a
     * connection.
     * <ul>
     * <li>If the value is has an alarm, the associated alarm is returned.</li>
     * <li>If the value is does not have an alarm, {@link Alarm#none()} is returned.</li>
     * <li>If the value is null and connected is true, {@link Alarm#noValue()} is returned.</li>
     * <li>If the value is null and disconnected is true, {@link Alarm#disconnected()} is returned.</li>
     * </ul>
     *
     * @param value a value
     * @param connected the connection status
     * @return the alarm information
     */
    public static Alarm alarmOf(Object value, boolean connected) {
        if (value != null) {
            if (value instanceof AlarmProvider) {
                return ((AlarmProvider) value).getAlarm();
            } else {
                return Alarm.none();
            }
        } else if (connected) {
            return Alarm.noValue();
        } else {
            return Alarm.disconnected();
        }
    }

}
