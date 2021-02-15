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
public class VBooleanTest {

    public Boolean getValue() {
        return Boolean.TRUE;
    }

    public Boolean getAnotherValue() {
        return Boolean.FALSE;
    }

    @Test
    public void of1() {
        Alarm alarm = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW");
        Time time = Time.of(Instant.ofEpochMilli(1354719441L*1000L+521786982L/1000000L));
        VBoolean value = VBoolean.of(getValue(), alarm, time);
        assertThat(value.getValue(), equalTo(getValue()));
        assertThat(value.getAlarm(), equalTo(alarm));
        assertThat(value.getTime(), equalTo(time));
        // Modified precision of test to match joda time's millisecond precision
        assertThat(value.toString(), equalTo("VBoolean[true, MINOR(DB) - LOW, 2012-12-05T14:57:21.521Z]"));
    }

    @Test(expected = NullPointerException.class)
    public void of2() {
        VBoolean.of(null, Alarm.none(), Time.now());
    }

    @Test(expected = NullPointerException.class)
    public void of3() {
        VBoolean.of(getValue(), null, Time.now());
    }

    @Test(expected = NullPointerException.class)
    public void of4() {
        VBoolean.of(getValue(), Alarm.none(), null);
    }

    @Test
    public void equals1() {
        Alarm alarm = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW");
        Time time = Time.of(Instant.ofEpochMilli(1354719441L*1000L+521786982L/1000000L));
        Time now = Time.now();
        assertThat(VBoolean.of(getValue(), alarm, time), equalTo(VBoolean.of(getValue(), alarm, time)));
        assertThat(VBoolean.of(getAnotherValue(), Alarm.none(), now), equalTo(VBoolean.of(getAnotherValue(), Alarm.none(), now)));
        assertThat(VBoolean.of(getValue(), alarm, time), not(equalTo(null)));
        assertThat(VBoolean.of(getValue(), alarm, time), not(equalTo(VBoolean.of(getAnotherValue(), alarm, time))));
        assertThat(VBoolean.of(getValue(), alarm, time), not(equalTo(VBoolean.of(getValue(), Alarm.none(), time))));
        assertThat(VBoolean.of(getValue(), alarm, time), not(equalTo(VBoolean.of(getValue(), alarm, now))));
    }

    @Test
    public void hashCode1() {
        Alarm alarm = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW");
        Time time = Time.of(Instant.ofEpochMilli(1354719441L*1000L+521786982L/1000000L));
        Time now = Time.now();
        assertThat(VBoolean.of(getValue(), alarm, time).hashCode(), equalTo(VBoolean.of(getValue(), alarm, time).hashCode()));
        assertThat(VBoolean.of(getAnotherValue(), Alarm.none(), now).hashCode(), equalTo(VBoolean.of(getAnotherValue(), Alarm.none(), now).hashCode()));
        assertThat(VBoolean.of(getValue(), alarm, time).hashCode(), not(equalTo(VBoolean.of(getAnotherValue(), alarm, time).hashCode())));
        assertThat(VBoolean.of(getValue(), alarm, time).hashCode(), not(equalTo(VBoolean.of(getValue(), Alarm.none(), time).hashCode())));
        assertThat(VBoolean.of(getValue(), alarm, time).hashCode(), not(equalTo(VBoolean.of(getValue(), alarm, now).hashCode())));
    }

}
