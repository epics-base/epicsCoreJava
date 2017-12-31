/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import java.text.DecimalFormat;
import java.time.Instant;
import org.epics.util.stats.Range;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class VStringTest {
    
    public String getValue() {
        return "A string";
    }
    
    public String getAnotherValue() {
        return "Another string";
    }

    @Test
    public void of1() {
        Alarm alarm = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW");
        Time time = Time.of(Instant.ofEpochSecond(1354719441, 521786982));
        VString value = VString.of(getValue(), alarm, time);
        assertThat(value.getValue(), equalTo(getValue()));
        assertThat(value.getAlarm(), equalTo(alarm));
        assertThat(value.getTime(), equalTo(time));
        assertThat(value.toString(), equalTo("VString[\"A string\", MINOR(DB) - LOW, 2012-12-05T14:57:21.521786982Z]"));
    }
    
    @Test(expected = NullPointerException.class)
    public void of2() {
        VString.of(null, Alarm.none(), Time.now());
    }
    
    @Test(expected = NullPointerException.class)
    public void of3() {
        VString.of(getValue(), null, Time.now());
    }
    
    @Test(expected = NullPointerException.class)
    public void of4() {
        VString.of(getValue(), Alarm.none(), null);
    }
    
    @Test
    public void equals1() {
        Alarm alarm = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW");
        Time time = Time.of(Instant.ofEpochSecond(1354719441, 521786982));
        Time now = Time.now();
        assertThat(VString.of(getValue(), alarm, time), equalTo(VString.of(getValue(), alarm, time)));
        assertThat(VString.of(getAnotherValue(), Alarm.none(), now), equalTo(VString.of(getAnotherValue(), Alarm.none(), now)));
        assertThat(VString.of(getValue(), alarm, time), not(equalTo(null)));
        assertThat(VString.of(getValue(), alarm, time), not(equalTo(VString.of(getAnotherValue(), alarm, time))));
        assertThat(VString.of(getValue(), alarm, time), not(equalTo(VString.of(getValue(), Alarm.none(), time))));
        assertThat(VString.of(getValue(), alarm, time), not(equalTo(VString.of(getValue(), alarm, now))));
    }
    
    @Test
    public void hashCode1() {
        Alarm alarm = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW");
        Time time = Time.of(Instant.ofEpochSecond(1354719441, 521786982));
        Time now = Time.now();
        assertThat(VString.of(getValue(), alarm, time).hashCode(), equalTo(VString.of(getValue(), alarm, time).hashCode()));
        assertThat(VString.of(getAnotherValue(), Alarm.none(), now).hashCode(), equalTo(VString.of(getAnotherValue(), Alarm.none(), now).hashCode()));
        assertThat(VString.of(getValue(), alarm, time).hashCode(), not(equalTo(VString.of(getAnotherValue(), alarm, time).hashCode())));
        assertThat(VString.of(getValue(), alarm, time).hashCode(), not(equalTo(VString.of(getValue(), Alarm.none(), time).hashCode())));
        assertThat(VString.of(getValue(), alarm, time).hashCode(), not(equalTo(VString.of(getValue(), alarm, now).hashCode())));
    }
    
}
