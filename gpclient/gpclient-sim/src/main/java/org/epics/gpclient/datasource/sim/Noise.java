/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.util.compat.legacy.lang.Random;

/**
 * Function to simulate a signal that has a uniform distribution. The warning
 * limits are set at 80% of the range and the alarm at 90% the range.
 * Alarm is based on the limits. Timestamp are generated at the rate requested.
 *
 * @author carcassi
 */
public class Noise extends VDoubleSimFunction {

    private final Random rand = new Random();

    /**
     * Creates a signal uniformly distributed between -5.0 and 5.0, updating
     * every 500ms (2Hz).
     */
    public Noise() {
        this(-5.0, 5.0, DEFAULT_INTERVAL);
    }

    /**
     * Creates a signal uniformly distributed between min and max, updating
     * every interval seconds.
     *
     * @param min minimum value
     * @param max maximum value
     * @param interval interval between samples in seconds
     */
    public Noise(Double min, Double max, Double interval) {
        super(interval, createDisplay(min, max));
    }

    @Override
    double nextDouble() {
        return display.getDisplayRange().rescale(rand.nextDouble());
    }
}
