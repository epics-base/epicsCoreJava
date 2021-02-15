/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ListDouble;


/**
 * Function to simulate a waveform containing a square wave.
 *
 * @author carcassi
 */
public class SquareWaveform extends VDoubleArraySimFunction {

    private final double omega;
    private final double k;
    private int nSamples;

    /**
     * Creates a square wave of 100 samples, with period of 1 second, wavelength of
     * 100 samples, updating every 500ms (2Hz).
     */
    public SquareWaveform() {
        this(5.0, 100.0, DEFAULT_INTERVAL);
    }

    /**
     * Creates a square wave of 100 samples, with given period and given wavelength of
     * 100 samples, updating at given rate.
     *
     * @param periodInSeconds the period measured in seconds
     * @param wavelengthInSamples the wavelength measured in samples
     * @param updateRateInSeconds the update rate in seconds
     */
    public SquareWaveform(Double periodInSeconds, Double wavelengthInSamples, Double updateRateInSeconds) {
        this(periodInSeconds, wavelengthInSamples, 100.0, updateRateInSeconds);
    }

    /**
     * Creates a square wave of 100 samples, with given period and given wavelength of
     * given number of samples, updating at given rate.
     *
     * @param periodInSeconds the period measured in seconds
     * @param wavelengthInSamples the wavelength measured in samples
     * @param nSamples the number of samples
     * @param updateRateInSeconds the update rate in seconds
     */
    public SquareWaveform(Double periodInSeconds, Double wavelengthInSamples, Double nSamples, Double updateRateInSeconds) {
        super(updateRateInSeconds, createDisplay(-1, 1));
        this.omega = 2 * Math.PI / periodInSeconds;
        this.k = 2 * Math.PI / wavelengthInSamples;
        this.nSamples = nSamples.intValue();
        if (this.nSamples <= 0) {
            throw new IllegalArgumentException("Number of sample must be a positive integer.");
        }
    }

    @Override
    ListDouble nextListDouble(double time) {
        double[] newArray = new double[nSamples];
        for (int i = 0; i < newArray.length; i++) {
            double x = (omega * time + k * i) / (2 * Math.PI);
            double normalizedPositionInPeriod = x - (double) (long) x;
            if (normalizedPositionInPeriod < 0.5) {
                newArray[i] = 1.0;
            } else if (normalizedPositionInPeriod < 1.0) {
                newArray[i] = -1.0;
            } else {
                newArray[i] = 1.0;
            }
        }
        return ArrayDouble.of(newArray);
    }
}
