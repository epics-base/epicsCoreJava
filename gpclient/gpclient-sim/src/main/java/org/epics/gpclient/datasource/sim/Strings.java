/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.vtype.Alarm;
import org.epics.vtype.Time;
import org.epics.vtype.VString;
import org.joda.time.Instant;


/**
 * Function to simulate a signal that generates Strings.
 *
 * @author carcassi
 */
public class Strings extends SimFunction<VString> {

    private final StringBuffer buffer = new StringBuffer();

    /**
     * Creates a String that grows between 0 and 10 characters, updating
     * every 500ms (2Hz).
     */
    public Strings() {
        this(DEFAULT_INTERVAL);
    }

    /**
     * Creates a signal uniformly distributed between min and max, updating
     * every interval seconds.
     *
     * @param interval interval between samples in seconds
     */
    public Strings(Double interval) {
        super(interval, VString.class);
        if (interval <= 0.0) {
            throw new IllegalArgumentException("Interval must be greater than zero (was " + interval + ")");
        }
    }

    @Override
    VString nextValue(Instant instant) {
        return VString.of(nextString(), Alarm.none(), Time.of(instant));
    }

    String nextString() {
        if (buffer.length() > 10) {
            buffer.setLength(0);
        }
        buffer.append("A");
        return buffer.toString();
    }
}
