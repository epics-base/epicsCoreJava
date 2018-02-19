/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource.sim;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import org.epics.util.array.ArrayDouble;
import org.epics.util.stats.Range;
import org.epics.vtype.Alarm;
import org.epics.vtype.Display;
import org.epics.vtype.Time;
import org.epics.vtype.VDoubleArray;
/**
 * Function to simulate a waveform containing a gaussian that moves to the
 * left.
 *
 * @author carcassi
 */
public class GaussianWaveform extends SimFunction<VDoubleArray> {

    private Random rand = new Random();
    private double[] buffer;
    private final double periodInSeconds;
    private VDoubleArray lastValue;
    private Instant initialRefernce;

    /**
     * Creates a gaussian wave of 100 samples, with period of 1 second, standard deviation of
     * 100 samples, updating every 500ms (2Hz).
     */
    public GaussianWaveform() {
        this(1.0, 100.0, 100.0, DEFAULT_INTERVAL);
    }

    /**
     * Creates a gaussian wave of given number of samples, with given period and standard,
     * updating at the given rate
     *
     * @param periodInSeconds the period measured in seconds
     * @param stdDev standard deviation of the gaussian distribution
     * @param nSamples number of elements in the waveform
     * @param updateRateInSeconds time between samples in seconds
     */
    public GaussianWaveform(Double periodInSeconds, Double stdDev, Double nSamples, Double updateRateInSeconds) {
        super(updateRateInSeconds, VDoubleArray.class);
        int size = nSamples.intValue();
        this.periodInSeconds = periodInSeconds;
        buffer = new double[size];
        populateGaussian(buffer, stdDev);
    }

    static void populateGaussian(double[] array, double stdDev) {
        for (int i = 0; i < array.length; i++) {
            array[i] = gaussian(i, array.length / 2.0, stdDev);
        }
    }

    private double[] generateNewValue(double omega, double t) {
        double x = t * omega / (2 * Math.PI);
        double normalizedX = x - (double) (long) x;
        int offset = (int) (normalizedX * buffer.length);
        if (offset == buffer.length) {
            offset = 0;
        }
        int localCounter = offset;
        double[] newArray = new double[buffer.length];
        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = buffer[localCounter];
            localCounter++;
            if (localCounter >= buffer.length) {
                localCounter -= buffer.length;
            }
        }

        return newArray;
    }

    /**
     * 1D gaussian, centered on centerX and with the specified width.
     * @param x coordinate x
     * @param centerX center of the gaussian on x
     * @param width width of the gaussian in all directions
     * @return the value of the function at the given coordinates
     */
    public static double gaussian(double x, double centerX, double width) {
        return Math.exp((-Math.pow((x - centerX), 2.0)) / width);
    }
    
    private static Range UNIT_RANGE = Range.of(0.0, 1.0);
    private static Display DISPLAY = Display.of(UNIT_RANGE, UNIT_RANGE, UNIT_RANGE, UNIT_RANGE, "x", Display.defaultNumberFormat());

    @Override
    VDoubleArray nextValue(Instant instant) {
        if (initialRefernce == null) {
            initialRefernce = instant;
        }
        double t = Duration.between(initialRefernce, instant).getSeconds();
        double omega = 2 * Math.PI / periodInSeconds;
        return VDoubleArray.of(ArrayDouble.of(generateNewValue(omega, t)), Alarm.none(),
                Time.of(instant), DISPLAY);
    }
}
