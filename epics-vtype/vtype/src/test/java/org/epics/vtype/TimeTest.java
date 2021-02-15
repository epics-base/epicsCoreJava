/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.joda.time.Instant;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author carcassi
 */
public class TimeTest {

    public TimeTest() {
    }

    @Test
    public void equals1() {
        // Granularity of time storage is milliseconds with joda time so we'll modify the tests to use ms instead of nanoseconds
        assertThat(Time.of(Instant.ofEpochSecond(123).plus(123), 4, false), equalTo(Time.of(Instant.ofEpochSecond(123).plus(123), 4, false)));
        Time time = Time.now();
        assertThat(time, equalTo(Time.of(time.getTimestamp(), null, true)));
        assertThat(time, not(equalTo(null)));
        assertThat(Time.of(Instant.ofEpochSecond(123).plus(123), 4, false), not(equalTo(Time.of(Instant.ofEpochSecond(123).plus(124), 4, false))));
        assertThat(Time.of(Instant.ofEpochSecond(123).plus(123), 4, false), not(equalTo(Time.of(Instant.ofEpochSecond(123).plus(123), 3, false))));
        assertThat(Time.of(Instant.ofEpochSecond(123).plus(123), 4, false), not(equalTo(Time.of(Instant.ofEpochSecond(123).plus(123), 4, true))));
    }

    @Test
    public void hashCode1() {
        assertThat(Time.of(Instant.ofEpochSecond(123).plus(123), 4, false).hashCode(), equalTo(Time.of(Instant.ofEpochSecond(123).plus(123), 4, false).hashCode()));
        assertThat(Time.of(Instant.ofEpochSecond(123).plus(123), 4, false).hashCode(), not(equalTo(Time.of(Instant.ofEpochSecond(123).plus(124), 4, false).hashCode())));
        assertThat(Time.of(Instant.ofEpochSecond(123).plus(123), 4, false).hashCode(), not(equalTo(Time.of(Instant.ofEpochSecond(123).plus(123), 3, false).hashCode())));
        assertThat(Time.of(Instant.ofEpochSecond(123).plus(123), 4, false).hashCode(), not(equalTo(Time.of(Instant.ofEpochSecond(123).plus(123), 4, true).hashCode())));
    }

    @Test
    public void now1() {
        Instant timestamp = Instant.now();
        Time time = Time.now();
        assertThat(time.getTimestamp().getMillis(), lessThan(timestamp.plus(1000).getMillis()));
        assertThat(time.getTimestamp().getMillis(), greaterThan(timestamp.minus(1000).getMillis()));
        assertThat(time.getUserTag(), nullValue());
        assertThat(time.isValid(), equalTo(true));
    }

    @Test
    public void nowInvalid1() {
        Instant timestamp = Instant.now();
        Time time = Time.nowInvalid();
        assertThat(time.getTimestamp().getMillis(), lessThan(timestamp.plus(1000).getMillis()));
        assertThat(time.getTimestamp().getMillis(), greaterThan(timestamp.minus(1000).getMillis()));
        assertThat(time.getUserTag(), nullValue());
        assertThat(time.isValid(), equalTo(false));
    }

    @Test
    public void of1() {
        Instant timestamp = Instant.ofEpochSecond(123).plus(123L/1000000L);
        Time time = Time.of(timestamp, 4, false);
        assertThat(time.getTimestamp(), equalTo(timestamp));
        assertThat(time.getUserTag(), equalTo(4));
        assertThat(time.isValid(), equalTo(false));
        // Modified precision of test to match joda time's millisecond precision
        assertThat(time.toString(), equalTo("1970-01-01T00:02:03.000Z(4)"));
    }

    @Test
    public void of2() {
        Instant timestamp = Instant.ofEpochSecond(123).plus(123L/1000000L);
        Time time = Time.of(timestamp);
        assertThat(time.getTimestamp(), equalTo(timestamp));
        assertThat(time.getUserTag(), equalTo(null));
        assertThat(time.isValid(), equalTo(true));
        // Modified precision of test to match joda time's millisecond precision
        assertThat(time.toString(), equalTo("1970-01-01T00:02:03.000Z"));
    }

    @Test(expected = NullPointerException.class)
    public void of3() {
        Time.of(null);
    }

    @Test(expected = NullPointerException.class)
    public void of4() {
        Time.of(null, null, true);
    }

}
