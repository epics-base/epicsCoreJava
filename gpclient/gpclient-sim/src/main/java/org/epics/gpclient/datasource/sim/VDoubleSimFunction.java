/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.vtype.Display;
import org.epics.vtype.Time;
import org.epics.vtype.VDouble;
import org.joda.time.Instant;

/**
 * Base class for all simulated functions that return numbers.
 *
 * @author carcassi
 */
abstract class VDoubleSimFunction extends SimFunction<VDouble> {

    /**
     * The display to be used for all values.
     */
    protected final Display display;

    /**
     * Creates a new simulation function.
     *
     * @param secondsBetweenSamples seconds between each samples
     * @param display display
     */
    VDoubleSimFunction(double secondsBetweenSamples, Display display) {
        super(secondsBetweenSamples, VDouble.class);
        this.display = display;
    }


    @Override
    final VDouble nextValue(Instant instant) {
        double value = nextDouble();
        return VDouble.of(value, display.newAlarmFor(value), Time.of(instant), display);
    }

    /**
     * Returns the next value in the sequence.
     *
     * @return the new value
     */
    abstract double nextDouble();

}
