/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ListDouble;
import org.epics.util.compat.legacy.lang.Random;

/**
 * Function to simulate a waveform containing a uniformly distributed
 * random data.
 *
 * @author carcassi
 */
public class NoiseWaveform extends VDoubleArraySimFunction {

    private Random rand = new Random();
    private int nSamples;

    /**
     * Creates a waveform with samples from a uniform distribution from -5 to 5,
     * updating every 500ms (2Hz).
     */
    public NoiseWaveform() {
        this(-5.0, 5.0, DEFAULT_INTERVAL);
    }

    /**
     * Creates a gaussian waveform signal with a gaussian distribution, updating at the rate
     * specified.
     *
     * @param min the minimum value
     * @param max the maximum value
     * @param interval time between samples in seconds
     */
    public NoiseWaveform(Double min, Double max, Double interval) {
        this(min, max, 100.0, interval);
    }

    /**
     * Creates a gaussian waveform signal with a gaussian distribution, updating at the rate
     * specified.
     *
     * @param min the minimum value
     * @param max the maximum value
     * @param nSamples number of elements in the waveform
     * @param interval time between samples in seconds
     */
    public NoiseWaveform(Double min, Double max, Double nSamples, Double interval) {
        super(interval, createDisplay(min, max));
        this.nSamples = nSamples.intValue();
        if (this.nSamples <= 0) {
            throw new IllegalArgumentException("Number of sample must be a positive integer.");
        }
    }

    @Override
    ListDouble nextListDouble(double time) {
        double[] newArray = new double[nSamples];
        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = display.getDisplayRange().rescale(rand.nextDouble());
        }
        return ArrayDouble.of(newArray);
    }
}
