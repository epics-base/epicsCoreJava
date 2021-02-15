/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.joda.time.Instant;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class VEnumTest {

    @Test
    public void of1() {
        EnumDisplay display = EnumDisplay.of("A", "B", "C");
        Alarm alarm = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW");
        Time time = Time.of(Instant.ofEpochMilli(1354719441L*1000L+521786982L/1000000L));
        VEnum value = VEnum.of(0, display, alarm, time);
        assertThat(value.getValue(), equalTo("A"));
        assertThat(value.getIndex(), equalTo(0));
        assertThat(value.getAlarm(), equalTo(alarm));
        assertThat(value.getTime(), equalTo(time));
        assertThat(value.getDisplay(), equalTo(display));
        // Modified precision of test to match joda time's millisecond precision
        assertThat(value.toString(), equalTo("VEnum[\"A\", MINOR(DB) - LOW, 2012-12-05T14:57:21.521Z]"));
    }

    @Test(expected = NullPointerException.class)
    public void of2() {
        VEnum value = VEnum.of(0, null, Alarm.none(), Time.now());
    }

    @Test(expected = NullPointerException.class)
    public void of3() {
        VEnum value = VEnum.of(0, EnumDisplay.of(3), null, Time.now());
    }

    @Test(expected = NullPointerException.class)
    public void of4() {
        VEnum value = VEnum.of(0, EnumDisplay.of(3), Alarm.none(), null);
    }

    @Test
    public void equals1() {
        EnumDisplay display = EnumDisplay.of("A", "B", "C");
        Alarm alarm = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW");
        Time time = Time.of(Instant.ofEpochMilli(1354719441L*1000L+521786982L/1000000L));
        Time now = Time.now();
        assertThat(VEnum.of(0, display, alarm, time), equalTo(VEnum.of(0, display, alarm, time)));
        assertThat(VEnum.of(1, display, Alarm.none(), now), equalTo(VEnum.of(1, display, Alarm.none(), now)));
        assertThat(VEnum.of(0, display, alarm, time), not(equalTo(null)));
        assertThat(VEnum.of(0, display, alarm, time), not(equalTo(VEnum.of(1, display, alarm, time))));
        assertThat(VEnum.of(0, display, alarm, time), not(equalTo(VEnum.of(0, EnumDisplay.of(3), alarm, time))));
        assertThat(VEnum.of(0, display, alarm, time), not(equalTo(VEnum.of(0, display, Alarm.none(), time))));
        assertThat(VEnum.of(0, display, alarm, time), not(equalTo(VEnum.of(0, display, alarm, now))));
    }

    @Test
    public void hashCode1() {
        EnumDisplay display = EnumDisplay.of("A", "B", "C");
        Alarm alarm = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW");
        Time time = Time.of(Instant.ofEpochMilli(1354719441L*1000L+521786982L/1000000L));
        Time now = Time.now();
        assertThat(VEnum.of(0, display, alarm, time).hashCode(), equalTo(VEnum.of(0, display, alarm, time).hashCode()));
        assertThat(VEnum.of(1, display, Alarm.none(), now).hashCode(), equalTo(VEnum.of(1, display, Alarm.none(), now).hashCode()));
        assertThat(VEnum.of(0, display, alarm, time).hashCode(), not(equalTo(VEnum.of(1, display, alarm, time).hashCode())));
        assertThat(VEnum.of(0, display, alarm, time).hashCode(), not(equalTo(VEnum.of(0, EnumDisplay.of(3), alarm, time).hashCode())));
        assertThat(VEnum.of(0, display, alarm, time).hashCode(), not(equalTo(VEnum.of(0, display, Alarm.none(), time).hashCode())));
        assertThat(VEnum.of(0, display, alarm, time).hashCode(), not(equalTo(VEnum.of(0, display, alarm, now).hashCode())));
    }

}
