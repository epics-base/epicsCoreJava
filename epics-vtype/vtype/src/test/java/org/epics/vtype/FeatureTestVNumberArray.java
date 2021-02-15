/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.array.ArrayInteger;
import org.epics.util.array.ListInteger;
import org.epics.util.array.ListNumber;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.epics.util.stats.Range;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.text.DecimalFormat;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 *
 * @author carcassi
 * @param <L>
 * @param <V>
 */
public abstract class FeatureTestVNumberArray<L extends ListNumber, V extends VNumberArray> {

    abstract L getData();

    abstract L getOtherData();

    abstract V of(L data, Alarm alarm, Time time, Display display);

    abstract V of(L data, ListInteger sizes, Alarm alarm, Time time, Display display);

    abstract String getToString();

    @Test
    public void vNumberArrayOf1() {
        Alarm alarm = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW");
        Time time = Time.of(Instant.ofEpochSecond(1354719441L).plus(Duration.millis(521786982L / 1000000L)));
        ListInteger sizes = ArrayInteger.of(5,2);
        VNumberArray value = VNumberArray.of(getData(), sizes, alarm, time, Display.none());
        assertThat(value.getData(), Matchers.<ListNumber>equalTo(getData()));
        assertThat(value.getSizes(), equalTo(sizes));
        assertThat(value.getAlarm(), equalTo(alarm));
        assertThat(value.getTime(), equalTo(time));

        value = VNumberArray.of(getData(), alarm, time, Display.none());
        assertThat(value.getData(), Matchers.<ListNumber>equalTo(getData()));
        assertThat(value.getSizes(), Matchers.<ListInteger>equalTo(ArrayInteger.of(10)));
        assertThat(value.getAlarm(), equalTo(alarm));
        assertThat(value.getTime(), equalTo(time));
    }

    @Test
    public void of1() {
        Alarm alarm = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW");
        Time time = Time.of(Instant.ofEpochSecond(1354719441L).plus(Duration.millis(521786982L / 1000000L)));
        ListInteger sizes = ArrayInteger.of(5,2);
        V value = of(getData(), sizes, alarm, time, Display.none());
        assertThat(value.getData(), Matchers.<ListNumber>equalTo(getData()));
        assertThat(value.getSizes(), equalTo(sizes));
        assertThat(value.getAlarm(), equalTo(alarm));
        assertThat(value.getTime(), equalTo(time));
        assertThat(value.toString(), equalTo(getToString()));

        value = of(getData(), alarm, time, Display.none());
        assertThat(value.getData(), Matchers.<ListNumber>equalTo(getData()));
        assertThat(value.getSizes(), Matchers.<ListInteger>equalTo(ArrayInteger.of(10)));
        assertThat(value.getAlarm(), equalTo(alarm));
        assertThat(value.getTime(), equalTo(time));
    }

    @Test(expected = NullPointerException.class)
    public void of2() {
        of(null, Alarm.none(), Time.now(), Display.none());
    }

    @Test(expected = NullPointerException.class)
    public void of3() {
        of(getData(), null, Time.now(), Display.none());
    }

    @Test(expected = NullPointerException.class)
    public void of4() {
        of(getData(), Alarm.none(), null, Display.none());
    }

    @Test(expected = NullPointerException.class)
    public void of5() {
        of(getData(), Alarm.none(), Time.now(), null);
    }

    @Test(expected = NullPointerException.class)
    public void of6() {
        of(null, ArrayInteger.of(10), Alarm.none(), Time.now(), Display.none());
    }

    @Test(expected = NullPointerException.class)
    public void of7() {
        of(getData(), ArrayInteger.of(10), null, Time.now(), Display.none());
    }

    @Test(expected = NullPointerException.class)
    public void of8() {
        of(getData(), ArrayInteger.of(10), Alarm.none(), null, Display.none());
    }

    @Test(expected = NullPointerException.class)
    public void of9() {
        of(getData(), ArrayInteger.of(10), Alarm.none(), Time.now(), null);
    }

    @Test(expected = NullPointerException.class)
    public void of10() {
        of(getData(), null, Alarm.none(), Time.now(), Display.none());
    }

    @Test
    public void equals1() {
        Alarm alarm = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW");
        Time time = Time.of(Instant.ofEpochSecond(1354719441L).plus(Duration.millis(521786982L / 1000000L)));
        Time now = Time.now();
        assertThat(of(getData(), alarm, time, Display.none()), equalTo(of(getData(), alarm, time, Display.none())));
        assertThat(of(getOtherData(), Alarm.none(), now, Display.none()), equalTo(of(getOtherData(), Alarm.none(), now, Display.none())));
        assertThat(of(getData(), alarm, time, Display.none()), not(equalTo(null)));
        assertThat(of(getData(), alarm, time, Display.none()), not(equalTo(of(getOtherData(), alarm, time, Display.none()))));
        assertThat(of(getData(), alarm, time, Display.none()), not(equalTo(of(getData(), Alarm.none(), time, Display.none()))));
        assertThat(of(getData(), alarm, time, Display.none()), not(equalTo(of(getData(), alarm, now, Display.none()))));
        assertThat(of(getData(), alarm, time, Display.none()), not(equalTo(of(getData(), alarm, time, Display.of(Range.undefined(), Range.undefined(), Range.undefined(), Range.undefined(), "meters", new DecimalFormat())))));
    }

    @Test
    public void hashCode1() {
        Alarm alarm = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW");
        Time time = Time.of(Instant.ofEpochSecond(1354719441L).plus(Duration.millis(521786982L / 1000000L)));
        Time now = Time.now();
        assertThat(of(getData(), alarm, time, Display.none()).hashCode(), equalTo(of(getData(), alarm, time, Display.none()).hashCode()));
        assertThat(of(getOtherData(), Alarm.none(), now, Display.none()).hashCode(), equalTo(of(getOtherData(), Alarm.none(), now, Display.none()).hashCode()));
        assertThat(of(getData(), alarm, time, Display.none()).hashCode(), not(equalTo(of(getOtherData(), alarm, time, Display.none()).hashCode())));
        assertThat(of(getData(), alarm, time, Display.none()).hashCode(), not(equalTo(of(getData(), Alarm.none(), time, Display.none()).hashCode())));
        assertThat(of(getData(), alarm, time, Display.none()).hashCode(), not(equalTo(of(getData(), alarm, now, Display.none()).hashCode())));
        assertThat(of(getData(), alarm, time, Display.none()).hashCode(), not(equalTo(of(getData(), alarm, time, Display.of(Range.undefined(), Range.undefined(), Range.undefined(), Range.undefined(), "meters", new DecimalFormat())).hashCode())));
    }

}
