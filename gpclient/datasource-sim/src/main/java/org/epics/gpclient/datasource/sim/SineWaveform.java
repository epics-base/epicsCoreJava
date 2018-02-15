/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource.sim;

import java.time.Duration;
import java.time.Instant;
import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ArrayInteger;
import org.epics.util.array.ListDouble;
import org.epics.util.stats.Range;
import org.epics.vtype.Alarm;
import org.epics.vtype.Display;
import org.epics.vtype.Time;
import org.epics.vtype.VDoubleArray;


/**
 * Function to simulate a waveform containing a sine wave.
 *
 * @author carcassi
 */
public class SineWaveform extends SimFunction<VDoubleArray> {

    private double periodInSeconds;
    private double wavelengthInSamples;
    private int nSamples;
    private Instant initialReference;

    /**
     * Creates sine wave of 100 samples, with period of 1 second, wavelength of
     * 100 samples, updating at 10 Hz.
     */
    public SineWaveform() {
        this(1.0, 100.0, 0.1);
    }
    
    /**
     * Creates sine wave of 100 samples, with given period and given wavelength of
     * 100 samples, updating at given rate.
     *
     * @param periodInSeconds the period measured in seconds
     * @param wavelengthInSamples the wavelength measured in samples
     * @param updateRateInSeconds the update rate in seconds
     */
    public SineWaveform(Double periodInSeconds, Double wavelengthInSamples, Double updateRateInSeconds) {
        this(periodInSeconds, wavelengthInSamples, 100.0, updateRateInSeconds);
    }

    /**
     * Creates sine wave of 100 samples, with given period and given wavelength of
     * given number of samples, updating at given rate.
     *
     * @param periodInSeconds the period measured in seconds
     * @param wavelengthInSamples the wavelength measured in samples
     * @param nSamples the number of samples
     * @param updateRateInSeconds the update rate in seconds
     */
    public SineWaveform(Double periodInSeconds, Double wavelengthInSamples, Double nSamples, Double updateRateInSeconds) {
        super(updateRateInSeconds, VDoubleArray.class);
        this.periodInSeconds = periodInSeconds;
        this.wavelengthInSamples = wavelengthInSamples;
        this.nSamples = nSamples.intValue();
        if (this.nSamples <= 0) {
            throw new IllegalArgumentException("Number of sample must be a positive integer.");
        }
    }

    private ListDouble generateNewValue(final double omega, final double t, double k) {
        double[] newArray = new double[nSamples];
        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = Math.sin(omega * t + k * i);
        }
        return ArrayDouble.of(newArray);
    }
    
    private static Range UNIT_RANGE = Range.of(-1.0, 1.0);
    private static Display DISPLAY = Display.of(UNIT_RANGE, UNIT_RANGE.shrink(0.9), UNIT_RANGE.shrink(0.8), Range.undefined(), "", Display.defaultNumberFormat());

    @Override
    VDoubleArray nextValue(Instant instant) {
        if (initialReference == null) {
            initialReference = instant;
        }
        double t = Duration.between(initialReference, instant).getSeconds();
        double omega = 2 * Math.PI / periodInSeconds;
        double k = 2 * Math.PI / wavelengthInSamples;
        return VDoubleArray.of(generateNewValue(omega, t, k), Alarm.none(),
                Time.of(instant), DISPLAY);
    }
}
