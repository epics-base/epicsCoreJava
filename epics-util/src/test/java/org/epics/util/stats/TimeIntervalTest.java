/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.stats;

import org.joda.time.Instant;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author carcassi
 */
public class TimeIntervalTest {

    public TimeIntervalTest() {
    }

    @Test
    public void interval1() {
        TimeInterval interval = TimeInterval.between(Instant.ofEpochSecond(0), Instant.ofEpochSecond(3600));
        assertThat(interval.getStart(), equalTo(Instant.ofEpochSecond(0)));
        assertThat(interval.getEnd(), equalTo(Instant.ofEpochSecond(3600)));
    }

    @Test
    public void interval2() {
        TimeInterval interval = TimeInterval.between(Instant.ofEpochSecond(3600), Instant.ofEpochSecond(7200));
        assertThat(interval.getStart(), equalTo(Instant.ofEpochSecond(3600)));
        assertThat(interval.getEnd(), equalTo(Instant.ofEpochSecond(7200)));
    }

    @Test
    public void interval3() {
        TimeInterval interval = TimeInterval.between(Instant.ofEpochSecond(0), null);
        assertThat(interval.getStart(), equalTo(Instant.ofEpochSecond(0)));
        assertThat(interval.getEnd(), nullValue());
    }

    @Test
    public void interval4() {
        TimeInterval interval = TimeInterval.between(null, Instant.ofEpochSecond(0));
        assertThat(interval.getStart(), nullValue());
        assertThat(interval.getEnd(), equalTo(Instant.ofEpochSecond(0)));
    }

    @Test
    public void equals1() {
        TimeInterval interval = TimeInterval.between(Instant.ofEpochSecond(0), Instant.ofEpochSecond(3600));
        assertThat(interval, equalTo(TimeInterval.between(Instant.ofEpochSecond(0), Instant.ofEpochSecond(3600))));
    }

    @Test
    public void equals2() {
        // Change test to verify milliseconds due to limitations of joda-time
        TimeInterval interval = TimeInterval.between(Instant.ofEpochMilli(0L*1000L+1L), Instant.ofEpochSecond(3600L));
        assertThat(interval, not(equalTo(TimeInterval.between(Instant.ofEpochSecond(0L), Instant.ofEpochSecond(3600L)))));
    }

    @Test
    public void equals3() {
        TimeInterval interval = TimeInterval.between(Instant.ofEpochSecond(0L), Instant.ofEpochMilli(3600L*1000L+1L));
        assertThat(interval, not(equalTo(TimeInterval.between(Instant.ofEpochSecond(0L), Instant.ofEpochSecond(3600L)))));
    }

    @Test
    public void equals4() {
        TimeInterval interval = TimeInterval.between(Instant.ofEpochSecond(0L), null);
        assertThat(interval, equalTo(TimeInterval.between(Instant.ofEpochSecond(0L), null)));
    }

    @Test
    public void equals5() {
        TimeInterval interval = TimeInterval.between(null, Instant.ofEpochSecond(0L));
        assertThat(interval, equalTo(TimeInterval.between(null, Instant.ofEpochSecond(0L))));
    }

    @Test
    public void contains1() {
        TimeInterval interval = TimeInterval.between(Instant.ofEpochSecond(0L), Instant.ofEpochMilli(3600L*1000L+1L));
        assertThat(interval.contains(Instant.ofEpochSecond(3L)), is(true));
        assertThat(interval.contains(Instant.ofEpochMilli(110L)), is(true));
        assertThat(interval.contains(Instant.ofEpochSecond(3600L)), is(true));
        assertThat(interval.contains(Instant.ofEpochMilli(-1L*1000L+110L)), is(false));
        assertThat(interval.contains(Instant.ofEpochMilli(3600L*1000L+2L)), is(false));
    }

    @Test
    public void contains2() {
        TimeInterval interval = TimeInterval.between(Instant.ofEpochSecond(0L), null);
        assertThat(interval.contains(Instant.ofEpochMilli(-3600L*1000L+2L)), is(false));
        assertThat(interval.contains(Instant.ofEpochMilli(-1L*1000L+110L)), is(false));
        assertThat(interval.contains(Instant.ofEpochMilli(110L)), is(true));
        assertThat(interval.contains(Instant.ofEpochSecond(3L)), is(true));
        assertThat(interval.contains(Instant.ofEpochSecond(3600L)), is(true));
    }

    @Test
    public void contains3() {
        TimeInterval interval = TimeInterval.between(null, Instant.ofEpochSecond(0));
        assertThat(interval.contains(Instant.ofEpochMilli(-3600L*1000L+2L)), is(true));
        assertThat(interval.contains(Instant.ofEpochMilli(-1L*1000L+110L)), is(true));
        assertThat(interval.contains(Instant.ofEpochMilli(110L)), is(false));
        assertThat(interval.contains(Instant.ofEpochSecond(3L)), is(false));
        assertThat(interval.contains(Instant.ofEpochSecond(3600L)), is(false));
    }
}
