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
 */
public class VDoubleTest {

    @Test
    public void of1() {
        Alarm alarm = Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW");
        Time time = Time.of(Instant.ofEpochSecond(1354719441, 521786982));
        VDouble value = VDouble.of(1.0, alarm, time, Display.none());
        assertThat(value.getValue(), equalTo(1.0));
        assertThat(value.getAlarm(), equalTo(alarm));
        assertThat(value.getTime(), equalTo(time));
        assertThat(value.toString(), equalTo("VDouble[1.0 ,MINOR(DB) - LOW, 2012-12-05T14:57:21.521786982Z]"));
    }
    
    @Test(expected = NullPointerException.class)
    public void of2() {
        VDouble.of(null, Alarm.none(), Time.now(), Display.none());
    }
    
    @Test(expected = NullPointerException.class)
    public void of3() {
        VDouble.of(1.0, null, Time.now(), Display.none());
    }
    
    @Test(expected = NullPointerException.class)
    public void of4() {
        VDouble.of(1.0, Alarm.none(), null, Display.none());
    }
    
    @Test(expected = NullPointerException.class)
    public void of5() {
        VDouble.of(1.0, Alarm.none(), Time.now(), null);
    }
    
}
