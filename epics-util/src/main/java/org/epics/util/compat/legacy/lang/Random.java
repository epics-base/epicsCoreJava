package org.epics.util.compat.legacy.lang;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the Java Random class
 *
 * @author George McIntyre. 15-Feb-2021, SLAC
 */
public class Random {
    private final java.util.Random random;

    public Random() {
        random = new java.util.Random();
    }

    public Random(long l) {
        random = new java.util.Random(l);
    }

    public List<Double> doubles(long streamSize,
                                double randomNumberOrigin,
                                double randomNumberBound) {
        List<Double> list = new ArrayList<Double>();
        for (int i = 0; i < streamSize; i++) {
            list.add(nextDouble(randomNumberOrigin, randomNumberBound));
        }
        return list;
    }

    public List<Integer> ints(long streamSize,
                              int randomNumberOrigin,
                              int randomNumberBound) {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < streamSize; i++) {
            list.add(nextInt(randomNumberOrigin, randomNumberBound));
        }
        return list;
    }

    public double nextDouble(double origin, double bound) {
        double r = nextDouble();
        r = r * (bound - origin) + origin;
        if (r >= bound) // correct for rounding
            r = bound - Double.MIN_VALUE;
        return r;
    }

    public int nextInt(int origin, int bound) {
        return nextInt(bound - origin) + origin;
    }

    ////// Delegates

    public void setSeed(long l) {
        random.setSeed(l);
    }

    public void nextBytes(byte[] bytes) {
        random.nextBytes(bytes);
    }

    public int nextInt() {
        return random.nextInt();
    }

    public int nextInt(int i) {
        return random.nextInt(i);
    }

    public long nextLong() {
        return random.nextLong();
    }

    public boolean nextBoolean() {
        return random.nextBoolean();
    }

    public float nextFloat() {
        return random.nextFloat();
    }

    public double nextDouble() {
        return random.nextDouble();
    }

    public double nextGaussian() {
        return random.nextGaussian();
    }
}
