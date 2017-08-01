/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array.performance;

import org.epics.util.array.ArrayShort;
import org.epics.util.array.ArrayDouble;
import org.epics.util.array.CollectionNumber;
import org.epics.util.array.ArrayFloat;
import org.epics.util.array.ArrayByte;
import org.epics.util.array.ArrayInt;
import java.util.Random;
import org.epics.util.array.ArrayLong;
import org.epics.util.array.IteratorNumber;

/**
 * Benchmark and example of how to use the util.array package without losing
 * performance.
 *
 * @author carcassi
 */
public class ArrayCopyPerformanceMeasurement {

    public static void main(String[] args) {
        // Test parameters
        // Size of the array to test
        int arraySize = 100000;
        // Number of iterations to perform
        int nIterations = 10000;
        
        System.out.println("");
        System.out.println("Preparing data");
        double[] doubleArray = new double[arraySize];
        Random rand = new Random();
        for (int i = 0; i < doubleArray.length; i++) {
            doubleArray[i] = rand.nextGaussian();
        }
        double[] doubleArrayBig = new double[2*arraySize];
        for (int i = 0; i < doubleArrayBig.length; i++) {
            doubleArrayBig[i] = rand.nextGaussian();
        }

        ArrayDouble doubleCollection = new ArrayDouble(doubleArray);

        System.out.println("");
        System.out.println("Benchmark array copy");
        profileJavaArrayCopy(doubleArray, new double[arraySize], nIterations);

        System.out.println("");
        System.out.println("Benchmark loop copy");
        profileJavaArrayLoop(doubleArray, new double[arraySize], nIterations);

        System.out.println("");
        System.out.println("Benchmark setAll");
        profileArraySetAll(doubleCollection, new ArrayDouble(new double[arraySize], false), nIterations);
    }

    private static void profileArraySetAll(ArrayDouble src, ArrayDouble dst, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            dst.setAll(0, src);
            // NOTE: this check is required or the whole computation will be optimized away
            if (dst.getDouble(0) == 0) {
                System.out.println("Unexpected value " + dst.getDouble(0));
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Copy using ArrayDouble.setAll: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileJavaArrayLoop(double[] src, double[] dst, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            for (int j = 0; j < src.length; j++) {
                dst[j]= src[j];
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (dst[0] == 0) {
                System.out.println("Unexpected value " + dst[0]);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Copy using loop copy on double[]: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileJavaArrayCopy(double[] src, double[] dst, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            System.arraycopy(src, 0, dst, 0, src.length);
            // NOTE: this check is required or the whole computation will be optimized away
            if (dst[0] == 0) {
                System.out.println("Unexpected value " + dst[0]);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Copy using System.arraycopy on double[]: ns " + (stopTime - startTime) / nIterations);
    }
}
