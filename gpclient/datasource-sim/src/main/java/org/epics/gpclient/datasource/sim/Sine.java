/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource.sim;

import org.epics.util.stats.Range;
import org.epics.vtype.Alarm;
import org.epics.vtype.Display;
import org.epics.vtype.Time;
import org.epics.vtype.VDouble;


/**
 * Function to simulate a signal shaped like a sine. The warning
 * limits are set at 80% of the range and the alarm at 90% the range.
 * All values are going to have no alarm status, with the timestamp set at the
 * moment the sample was generated.
 *
 * @author carcassi
 */
public class Sine extends SimFunction<VDouble> {

    private double min;
    private double max;
    private long currentValue;
    private double samplesPerCycle;
    private Range range;
    private VDouble lastValue;

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
        super(secondsBeetwenSamples, VDouble.class);
        this.min = min;
        this.max = max;
        this.currentValue = 0;
        this.samplesPerCycle = samplesPerCycle;
        range = Range.of(min, max);
        lastValue = VDouble.of(0.0, Alarm.none(), Time.now(),
                Display.of(range, range.shrink(0.9), range.shrink(0.8), Range.undefined(),
                        "", Display.defaultNumberFormat()));
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
     * Creates a sine shaped signal between -5 and 5, updating
     * every second with 10 samples every full sine cycle.
     */
    public Sine() {
        this(-5.0, 5.0, 1.0);
    }

    @Override
    VDouble nextValue() {
        double value = range.rescale(Math.sin(currentValue * 2 * Math.PI /samplesPerCycle) / 2 + 0.5);
        currentValue++;

        return newValue(value, lastValue);
    }
}
