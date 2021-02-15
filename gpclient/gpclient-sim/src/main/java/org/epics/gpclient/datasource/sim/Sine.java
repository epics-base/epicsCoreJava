/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;


/**
 * Function to simulate a signal shaped like a sine. The warning
 * limits are set at 80% of the range and the alarm at 90% the range.
 * Alarm is based on the limits. Timestamp are generated at the rate requested.
 *
 * @author carcassi
 */
public class Sine extends VDoubleSimFunction {

    private long currentValue;
    protected final double samplesPerCycle;

    /**
     * Creates a sine shaped signal between -5 and 5, updating
     * every 500ms (2Hz) with 10 samples every full sine cycle.
     */
    public Sine() {
        this(-5.0, 5.0, DEFAULT_INTERVAL);
    }

    /**
     * Creates a sine shaped signal between min and max, updating
     * every interval seconds with 10 samples every full sine cycle.
     *
     * @param min minimum value
     * @param max maximum value
     * @param secondsBeetwenSamples interval between samples in seconds
     */
    public Sine(Double min, Double max, Double secondsBeetwenSamples) {
        this(min, max, 10.0, secondsBeetwenSamples);
    }

    /**
     * Creates a sine shaped signal between min and max, updating
     * every interval seconds with samplesPerCycles samples every full sine cycle.
     *
     * @param min minimum value
     * @param max maximum value
     * @param samplesPerCycle number of samples for each full cycle (each 2 Pi)
     * @param secondsBeetwenSamples interval between samples in seconds
     */
    public Sine(Double min, Double max, Double samplesPerCycle, Double secondsBeetwenSamples) {
        super(secondsBeetwenSamples, createDisplay(min, max));
        this.currentValue = 0;
        this.samplesPerCycle = samplesPerCycle;
    }

    @Override
    double nextDouble() {
        double value = display.getDisplayRange().rescale(Math.sin(currentValue * 2 * Math.PI /samplesPerCycle) / 2 + 0.5);
        currentValue++;

        return value;
    }
}
