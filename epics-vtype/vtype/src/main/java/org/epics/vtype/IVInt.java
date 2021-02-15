/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

/**
 * Immutable {@code VInt} implementation.
 *
 * @author carcassi
 */
final class IVInt extends VInt {

    private final Integer value;
    private final Alarm alarm;
    private final Time time;
    private final Display display;

    IVInt(Integer value, Alarm alarm, Time time, Display display) {
        VType.argumentNotNull("value", value);
        VType.argumentNotNull("alarm", alarm);
        VType.argumentNotNull("time", time);
        VType.argumentNotNull("display", display);
        this.value = value;
        this.alarm = alarm;
        this.time = time;
        this.display = display;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    public Alarm getAlarm() {
        return alarm;
    }

    public Time getTime() {
        return time;
    }

    public Display getDisplay() {
        return display;
    }

}
