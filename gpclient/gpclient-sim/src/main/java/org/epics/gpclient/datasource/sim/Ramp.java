/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;


/**
 * Function to simulate a signal that increases constantly within a range
 * (saw-tooth shape). The warning
 * limits are set at 80% of the range and the alarm at 90% the range.
 * Alarm is based on the limits. Timestamp are generated at the rate requested.
 *
 * @author carcassi
 */
public class Ramp extends VDoubleSimFunction {

    protected final double min;
    protected final double max;
    private double currentValue;
    protected final double step;

    /**
     * Creates a ramp shaped signal between -5 and +5, incrementing 1 every 500ms (2Hz).
     */
    public Ramp() {
        this (-5.0, 5.0, DEFAULT_INTERVAL);
    }

    /**
     * Creates a ramp shaped signal between min and max, incrementing 1
     * every interval seconds.
     *
     * @param min minimum value
     * @param max maximum value
     * @param interval interval between samples in seconds
     */
    public Ramp(Double min, Double max, Double interval) {
        this(min, max, 1.0, interval);
    }

    /**
     * Creates a ramp shaped signal between min and max, updating a step amount
     * every interval seconds.
     *
     * @param min minimum value
     * @param max maximum value
     * @param step increment for each sample
     * @param interval interval between samples in seconds
     */
    public Ramp(Double min, Double max, Double step, Double interval) {
        super(interval, createDisplay(min, max));
        this.min = min;
        this.max = max;
        if (step >=0) {
            this.currentValue = min - step;
        } else {
            this.currentValue = max - step;
        }
        this.step = step;
    }

    @Override
    double nextDouble() {
        currentValue = currentValue + step;
        if (currentValue > max) {
            currentValue = min;
        }
        if (currentValue < min) {
            currentValue = max;
        }

        return currentValue;
    }
}
