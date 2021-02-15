/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.util.array.ListDouble;
import org.epics.vtype.Alarm;
import org.epics.vtype.Display;
import org.epics.vtype.Time;
import org.epics.vtype.VDoubleArray;
import org.joda.time.Duration;
import org.joda.time.Instant;

/**
 * Base class for all simulated functions that return numbers.
 *
 * @author carcassi
 */
abstract class VDoubleArraySimFunction extends SimFunction<VDoubleArray> {

    /**
     * The display to be used for all values.
     */
    protected final Display display;

    /**
     * The timestamp for the last reset.
     */
    private Instant initialReference;

    /**
     * Creates a new simulation function.
     *
     * @param secondsBetweenSamples seconds between each samples
     */
    VDoubleArraySimFunction(double secondsBetweenSamples, Display display) {
        super(secondsBetweenSamples, VDoubleArray.class);
        this.display = display;
    }

    @Override
    Instant resetTime() {
        initialReference = Instant.now();
        return initialReference;
    }

    @Override
    final VDoubleArray nextValue(Instant instant) {
        Duration difference = Duration.millis(instant.minus(initialReference.getMillis()).getMillis());
        double t = difference.getStandardSeconds();
        ListDouble value = nextListDouble(t);
        return VDoubleArray.of(value, Alarm.none(), Time.of(instant), display);
    }

    /**
     * Returns the next value in the sequence.
     *
     * @return the new value
     */
    abstract ListDouble nextListDouble(double time);

}
