/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.vtype.Alarm;
import org.epics.vtype.Time;
import org.epics.vtype.VBoolean;
import org.joda.time.Instant;

/**
 * Function to simulate a boolean signal that turns on and off.
 *
 * @author carcassi
 */
public class Flipflop extends SimFunction<VBoolean> {

    private boolean value;

    /**
     * Creates a flipflop that changes every 500 ms.
     */
    public Flipflop() {
        this(DEFAULT_INTERVAL);
    }

    /**
     * Creates a signal that turns on and off every interval.
     *
     * @param interval interval between samples in seconds
     */
    public Flipflop(Double interval) {
        super(interval, VBoolean.class);
        if (interval <= 0.0) {
            throw new IllegalArgumentException("Interval must be greater than zero (was " + interval + ")");
        }
    }

    @Override
    VBoolean nextValue(Instant instant) {
        value = !value;
        return VBoolean.of(value, Alarm.none(), Time.of(instant));
    }
}
