/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.util.array.ArrayInteger;
import org.epics.util.array.ListDouble;
import org.epics.util.stats.Range;
import org.epics.vtype.Alarm;
import org.epics.vtype.Display;
import org.epics.vtype.Time;
import org.epics.vtype.VDoubleArray;
import org.joda.time.Instant;

/**
 * Function to simulate a 2D waveform containing a sine wave.
 *
 * @author carcassi
 */
public class Square2DWaveform extends SimFunction<VDoubleArray> {

    private final double periodInSeconds;
    private final double wavelengthInSamples;
    private final int xSamples;
    private final int ySamples;
    private final double angle;
    private Instant initialReference;

    /**
     * Creates sine wave of 100 samples, with period of 1 second, wavelength of
     * 100 samples along the x axis, updating every 500ms (2Hz).
     */
    public Square2DWaveform() {
        this(1.0, 100.0, DEFAULT_INTERVAL);
    }

    /**
     * Creates sine wave of 100 samples, with given period and given wavelength of
     * 100 samples along the x axis, updating at given rate.
     *
     * @param periodInSeconds the period measured in seconds
     * @param wavelengthInSamples the wavelength measured in samples
     * @param updateRateInSeconds the update rate in seconds
     */
    public Square2DWaveform(Double periodInSeconds, Double wavelengthInSamples, Double updateRateInSeconds) {
        this(periodInSeconds, wavelengthInSamples, 100.0, updateRateInSeconds);
    }

    /**
     * Creates sine wave of 100 samples, with given period and given wavelength of
     * given number of samples along the x axis, updating at given rate.
     *
     * @param periodInSeconds the period measured in seconds
     * @param wavelengthInSamples the wavelength measured in samples
     * @param nSamples the number of samples
     * @param updateRateInSeconds the update rate in seconds
     */
    public Square2DWaveform(Double periodInSeconds, Double wavelengthInSamples, Double nSamples, Double updateRateInSeconds) {
        this(periodInSeconds, wavelengthInSamples, 0.0, nSamples, nSamples, updateRateInSeconds);
    }

    /**
     * Creates sine wave with given parameters.
     *
     * @param periodInSeconds the period measured in seconds
     * @param wavelengthInSamples the wavelength measured in samples
     * @param angle the direction of propagation for the wave
     * @param xSamples number of samples on the x direction
     * @param ySamples number of samples on the y direction
     * @param updateRateInSeconds the update rate in seconds
     */
    public Square2DWaveform(Double periodInSeconds, Double wavelengthInSamples, Double angle, Double xSamples, Double ySamples, Double updateRateInSeconds) {
        super(updateRateInSeconds, VDoubleArray.class);
        this.periodInSeconds = periodInSeconds;
        this.wavelengthInSamples = wavelengthInSamples;
        this.xSamples = xSamples.intValue();
        this.ySamples = ySamples.intValue();
        this.angle = angle;
        if (this.xSamples <= 0 || this.ySamples <= 0) {
            throw new IllegalArgumentException("Number of sample must be a positive integer.");
        }
    }

    private ListDouble generateNewValue(final double omega, final double t, double k) {
        final double kx = Math.cos(angle * Math.PI / 180.0) * k;
        final double ky = Math.sin(angle * Math.PI / 180.0) * k;
        return new ListDouble() {

            public double getDouble(int index) {
                int x = index % xSamples;
                int y = index / xSamples;
                double length = (omega * t + kx* x + ky * y) / (2 * Math.PI);
                double normalizedPositionInPeriod = length - (double) (long) length;
                if (normalizedPositionInPeriod < 0.5) {
                    return 1.0;
                } else if (normalizedPositionInPeriod < 1.0) {
                    return -1.0;
                } else {
                    return 1.0;
                }
            }

            public int size() {
                return xSamples*ySamples;
            }
        };
    }

    private static Range UNIT_RANGE = Range.of(-1.0, 1.0);
    private static Display DISPLAY = Display.of(UNIT_RANGE, UNIT_RANGE.shrink(0.9), UNIT_RANGE.shrink(0.8), Range.undefined(), "", Display.defaultNumberFormat());

    @Override
    VDoubleArray nextValue(Instant instant) {
        if (initialReference == null) {
            initialReference = instant;
        }
        double t = instant.minus(initialReference.getMillis()).getMillis()*1000;
        double omega = 2 * Math.PI / periodInSeconds;
        double k = 2 * Math.PI / wavelengthInSamples;
        return VDoubleArray.of(generateNewValue(omega, t, k), ArrayInteger.of(ySamples, xSamples), Alarm.none(),
                Time.of(instant), DISPLAY);
    }
}
