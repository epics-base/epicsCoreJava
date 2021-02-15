/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ListDouble;
import org.epics.util.compat.legacy.lang.Random;
import org.epics.util.stats.Range;
import org.epics.vtype.Display;
/**
 * Function to simulate a waveform containing a gaussian that moves to the
 * left.
 *
 * @author carcassi
 */
public class GaussianWaveform extends VDoubleArraySimFunction {

    private Random rand = new Random();
    private final double omega;
    private double[] buffer;

    /**
     * Creates a gaussian wave of 100 samples, with period of 1 second, standard deviation of
     * 100 samples, updating every 500ms (2Hz).
     */
    public GaussianWaveform() {
        this(5.0, 100.0, 100.0, DEFAULT_INTERVAL);
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
        super(updateRateInSeconds, Display.of(Range.of(0 - 4 * stdDev, 0 + 4 * stdDev),
                        Range.of(0 - 2 * stdDev, 0 + 2 * stdDev),
                        Range.of(0 - stdDev, 0 + stdDev),
                        Range.undefined(),
                        "", Display.defaultNumberFormat()));
        int size = nSamples.intValue();
        this.omega = 2 * Math.PI / periodInSeconds;
        buffer = new double[size];
        populateGaussian(buffer, stdDev);
    }

    static void populateGaussian(double[] array, double stdDev) {
        for (int i = 0; i < array.length; i++) {
            array[i] = gaussian(i, array.length / 2.0, stdDev);
        }
    }

    @Override
    ListDouble nextListDouble(double time) {
        double x = time * omega / (2 * Math.PI);
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

        return ArrayDouble.of(newArray);
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
}
