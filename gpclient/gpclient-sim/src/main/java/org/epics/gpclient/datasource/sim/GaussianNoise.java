/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.util.compat.legacy.lang.Random;
import org.epics.util.stats.Range;
import org.epics.vtype.Display;

/**
 * Function to simulate a signal that has a gaussian distribution. The warning
 * limits are set above the standard deviation and the alarm above two times
 * the standard deviation. The total range is 4 times the standard deviation.
 * Alarm is based on the limits. Timestamp are generated at the rate requested.
 *
 * @author carcassi
 */
public class GaussianNoise extends VDoubleSimFunction {

    private final Random rand = new Random();
    private final double average;
    private final double stdDev;

    /**
     * Creates a signal with a normal distribution (average zero and
     * standard deviation one), updating every 500ms (2Hz).
     */
    public GaussianNoise() {
        this(0.0, 1.0, DEFAULT_INTERVAL);
    }

    /**
     * Creates a signal with a gaussian distribution, updating at the rate
     * specified.
     *
     * @param average average of the gaussian distribution
     * @param stdDev standard deviation of the gaussian distribution
     * @param interval time between samples in seconds
     */
    public GaussianNoise(Double average, Double stdDev, Double interval) {
        super(interval, Display.of(Range.of(average - 4 * stdDev, average + 4 * stdDev),
                        Range.of(average - 2 * stdDev, average + 2 * stdDev),
                        Range.of(average - stdDev, average + stdDev),
                        Range.undefined(),
                        "", Display.defaultNumberFormat()));
        this.average = average;
        this.stdDev = stdDev;
    }

    @Override
    double nextDouble() {
        return average + rand.nextGaussian() * stdDev;
    }
}
