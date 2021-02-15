/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import java.util.Arrays;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class AlarmTest {

    @Test
    public void of1() {
        Alarm alarm = Alarm.of(AlarmSeverity.MAJOR, AlarmStatus.DEVICE, "DEVICE");
        assertThat(alarm.getSeverity(), equalTo(AlarmSeverity.MAJOR));
        assertThat(alarm.getStatus(), equalTo(AlarmStatus.DEVICE));
        assertThat(alarm.getName(), equalTo("DEVICE"));
        assertThat(alarm.toString(), equalTo("MAJOR(DEVICE) - DEVICE"));
    }

    @Test(expected = NullPointerException.class)
    public void of2() {
        Alarm.of(null, AlarmStatus.DEVICE, "DEVICE");
    }

    @Test(expected = NullPointerException.class)
    public void of3() {
        Alarm.of(AlarmSeverity.MAJOR, null, "DEVICE");
    }

    @Test(expected = NullPointerException.class)
    public void of4() {
        Alarm.of(AlarmSeverity.MAJOR, AlarmStatus.DEVICE, null);
    }

    @Test
    public void none1() {
        Alarm alarm = Alarm.none();
        assertThat(alarm.getSeverity(), equalTo(AlarmSeverity.NONE));
        assertThat(alarm.getStatus(), equalTo(AlarmStatus.NONE));
        assertThat(alarm.getName(), equalTo("None"));
        assertThat(alarm.toString(), equalTo("NONE(NONE) - None"));
    }

    @Test
    public void noValue1() {
        Alarm alarm = Alarm.noValue();
        assertThat(alarm.getSeverity(), equalTo(AlarmSeverity.INVALID));
        assertThat(alarm.getStatus(), equalTo(AlarmStatus.CLIENT));
        assertThat(alarm.getName(), equalTo("No value"));
        assertThat(alarm.toString(), equalTo("INVALID(CLIENT) - No value"));
    }

    @Test
    public void disconnected1() {
        Alarm alarm = Alarm.disconnected();
        assertThat(alarm.getSeverity(), equalTo(AlarmSeverity.INVALID));
        assertThat(alarm.getStatus(), equalTo(AlarmStatus.CLIENT));
        assertThat(alarm.getName(), equalTo("Disconnected"));
        assertThat(alarm.toString(), equalTo("INVALID(CLIENT) - Disconnected"));
    }

    @Test
    public void equals1() {
        assertThat(Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH"), equalTo(Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH")));
        assertThat(Alarm.none(), equalTo(Alarm.none()));
        assertThat(Alarm.none(), not(equalTo(null)));
        assertThat(Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH"), not(equalTo(Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW"))));
        assertThat(Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH"), not(equalTo(Alarm.of(AlarmSeverity.MINOR, AlarmStatus.CLIENT, "HIGH"))));
        assertThat(Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH"), not(equalTo(Alarm.of(AlarmSeverity.MAJOR, AlarmStatus.DB, "HIGH"))));
    }

    @Test
    public void hashCode1() {
        assertThat(Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH").hashCode(), equalTo(Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH").hashCode()));
        assertThat(Alarm.none().hashCode(), equalTo(Alarm.none().hashCode()));
        assertThat(Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH").hashCode(), not(equalTo(Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW").hashCode())));
        assertThat(Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH").hashCode(), not(equalTo(Alarm.of(AlarmSeverity.MINOR, AlarmStatus.CLIENT, "HIGH").hashCode())));
        assertThat(Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH").hashCode(), not(equalTo(Alarm.of(AlarmSeverity.MAJOR, AlarmStatus.DB, "HIGH").hashCode())));
    }

    @Test
    public void alarmOf1() {
        assertThat(Alarm.alarmOf(null), equalTo(Alarm.noValue()));
        assertThat(Alarm.alarmOf(new Object()), equalTo(Alarm.none()));
        assertThat(Alarm.alarmOf(new AlarmProvider() {
                    public Alarm getAlarm() {
                        return Alarm.of(AlarmSeverity.MINOR, AlarmStatus.CLIENT, "alarmOf1");
                    }
                }),
                equalTo(Alarm.of(AlarmSeverity.MINOR, AlarmStatus.CLIENT, "alarmOf1")));
    }

    @Test
    public void alarmOf2() {
        assertThat(Alarm.alarmOf(null, false), equalTo(Alarm.disconnected()));
        assertThat(Alarm.alarmOf(new Object(), false), equalTo(Alarm.none()));
        assertThat(Alarm.alarmOf(new AlarmProvider() {
                    public Alarm getAlarm() {
                        return Alarm.of(AlarmSeverity.MINOR, AlarmStatus.CLIENT, "alarmOf1");
                    }
                }, false),
                equalTo(Alarm.of(AlarmSeverity.MINOR, AlarmStatus.CLIENT, "alarmOf1")));
    }

    @Test
    public void highestSeverityOf1() {
        Alarm none = Alarm.none();
        Alarm minor = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "Minor alarm");
        Alarm otherMinor = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "Other minor alarm");
        Alarm major = Alarm.of(AlarmSeverity.MAJOR, AlarmStatus.DB, "Major alarm");
        Alarm invalid = Alarm.of(AlarmSeverity.INVALID, AlarmStatus.DRIVER, "Invalid alarm");
        Alarm undefined = Alarm.of(AlarmSeverity.UNDEFINED, AlarmStatus.UNDEFINED, "Undefined alarm");
        assertThat(Alarm.highestAlarmOf(Arrays.asList(VType.toVType(0.0, none), VType.toVType(0.0, minor)), true), sameInstance(minor));
        assertThat(Alarm.highestAlarmOf(Arrays.asList(VType.toVType(0.0, none), VType.toVType(0.0, minor), VType.toVType(0.0, otherMinor)), true), sameInstance(minor));
        assertThat(Alarm.highestAlarmOf(Arrays.asList(null, VType.toVType(0.0, minor), VType.toVType(0.0, otherMinor)), true), sameInstance(minor));
        assertThat(Alarm.highestAlarmOf(Arrays.asList(null, VType.toVType(0.0, minor), VType.toVType(0.0, otherMinor)), false), sameInstance(Alarm.noValue()));
        assertThat(Alarm.highestAlarmOf(Arrays.asList(VType.toVType(0.0, none), VType.toVType(0.0, major), VType.toVType(0.0, minor), VType.toVType(0.0, otherMinor)), true), sameInstance(major));
        assertThat(Alarm.highestAlarmOf(Arrays.asList(VType.toVType(0.0, none), VType.toVType(0.0, major), VType.toVType(0.0, minor), VType.toVType(0.0, otherMinor), VType.toVType(0.0, invalid)), true), sameInstance(invalid));
        assertThat(Alarm.highestAlarmOf(Arrays.asList(VType.toVType(0.0, none), VType.toVType(0.0, major), VType.toVType(0.0, minor), VType.toVType(0.0, undefined), VType.toVType(0.0, invalid)), true), sameInstance(undefined));
        assertThat(Alarm.highestAlarmOf(Arrays.asList(VType.toVType(0.0, none), VType.toVType(0.0, major), VType.toVType(0.0, minor), VType.toVType(0.0, undefined), VType.toVType(0.0, invalid), null), false), sameInstance(undefined));
    }
}
