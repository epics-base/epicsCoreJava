/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.stats.Range;
import org.hamcrest.Matchers;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.Test;

import java.text.DecimalFormat;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * @param <N>
 * @param <V>
 * @author carcassi
 */
public abstract class FeatureTestVNumber<N extends Number, V extends VNumber> {

    abstract N getValue();

    abstract N getAnotherValue();

    abstract V of(N value, Alarm alarm, Time time, Display display);

    abstract String getToString();

    @Test
    public void vNumberOf1() {
        Alarm alarm = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW");
        Time time = Time.of(Instant.ofEpochSecond(1354719441L).plus(Duration.millis(521786982L / 1000000L)));
        VNumber value = VNumber.of(getValue(), alarm, time, Display.none());
        assertThat(value.getValue(), Matchers.<Number>equalTo(getValue()));
        assertThat(value.getAlarm(), equalTo(alarm));
        assertThat(value.getTime(), equalTo(time));
    }

    @Test
    public void of1() {
        Alarm alarm = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW");
        Time time = Time.of(Instant.ofEpochSecond(1354719441L).plus(Duration.millis(521786982L / 1000000L)));
        V value = of(getValue(), alarm, time, Display.none());
        assertThat(value.getValue(), Matchers.<Number>equalTo(getValue()));
        assertThat(value.getAlarm(), equalTo(alarm));
        assertThat(value.getTime(), equalTo(time));
        assertThat(value.toString(), equalTo(getToString()));
    }

    @Test(expected = NullPointerException.class)
    public void of2() {
        of(null, Alarm.none(), Time.now(), Display.none());
    }

    @Test(expected = NullPointerException.class)
    public void of3() {
        of(getValue(), null, Time.now(), Display.none());
    }

    @Test(expected = NullPointerException.class)
    public void of4() {
        of(getValue(), Alarm.none(), null, Display.none());
    }

    @Test(expected = NullPointerException.class)
    public void of5() {
        of(getValue(), Alarm.none(), Time.now(), null);
    }

    @Test
    public void equals1() {
        Alarm alarm = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW");
        Time time = Time.of(Instant.ofEpochSecond(1354719441L).plus(Duration.millis(521786982L / 1000000L)));
        Time now = Time.now();
        assertThat(of(getValue(), alarm, time, Display.none()), equalTo(of(getValue(), alarm, time, Display.none())));
        assertThat(of(getAnotherValue(), Alarm.none(), now, Display.none()), equalTo(of(getAnotherValue(), Alarm.none(), now, Display.none())));
        assertThat(of(getValue(), alarm, time, Display.none()), not(equalTo(null)));
        assertThat(of(getValue(), alarm, time, Display.none()), not(equalTo(of(getAnotherValue(), alarm, time, Display.none()))));
        assertThat(of(getValue(), alarm, time, Display.none()), not(equalTo(of(getValue(), Alarm.none(), time, Display.none()))));
        assertThat(of(getValue(), alarm, time, Display.none()), not(equalTo(of(getValue(), alarm, now, Display.none()))));
        assertThat(of(getValue(), alarm, time, Display.none()), not(equalTo(of(getValue(), alarm, time, Display.of(Range.undefined(), Range.undefined(), Range.undefined(), Range.undefined(), "meters", new DecimalFormat())))));
    }

    @Test
    public void hashCode1() {
        Alarm alarm = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW");
        Time time = Time.of(Instant.ofEpochSecond(1354719441L).plus(Duration.millis(521786982L / 1000000L)));
        Time now = Time.now();
        assertThat(of(getValue(), alarm, time, Display.none()).hashCode(), equalTo(of(getValue(), alarm, time, Display.none()).hashCode()));
        assertThat(of(getAnotherValue(), Alarm.none(), now, Display.none()).hashCode(), equalTo(of(getAnotherValue(), Alarm.none(), now, Display.none()).hashCode()));
        assertThat(of(getValue(), alarm, time, Display.none()).hashCode(), not(equalTo(of(getAnotherValue(), alarm, time, Display.none()).hashCode())));
        assertThat(of(getValue(), alarm, time, Display.none()).hashCode(), not(equalTo(of(getValue(), Alarm.none(), time, Display.none()).hashCode())));
        assertThat(of(getValue(), alarm, time, Display.none()).hashCode(), not(equalTo(of(getValue(), alarm, now, Display.none()).hashCode())));
        assertThat(of(getValue(), alarm, time, Display.none()).hashCode(), not(equalTo(of(getValue(), alarm, time, Display.of(Range.undefined(), Range.undefined(), Range.undefined(), Range.undefined(), "meters", new DecimalFormat())).hashCode())));
    }

}
