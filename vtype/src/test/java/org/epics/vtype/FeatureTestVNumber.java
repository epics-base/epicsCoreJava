/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import java.time.Instant;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 * @param <N>
 * @param <V>
 */
public abstract class FeatureTestVNumber<N extends Number, V extends VNumber> {
    
    abstract N getValue();
    
    abstract V of(N value, Alarm alarm, Time time, Display display);
    
    abstract String getToString();

    @Test
    public void of1() {
        Alarm alarm = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW");
        Time time = Time.of(Instant.ofEpochSecond(1354719441, 521786982));
        V value = of(getValue(), alarm, time, Display.none());
        assertThat(value.getValue(), equalTo(getValue()));
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
    
}
