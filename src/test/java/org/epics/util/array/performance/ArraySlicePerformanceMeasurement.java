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
public class ArraySlicePerformanceMeasurement {

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
        ArrayDouble doubleCollectionBig = new ArrayDouble(doubleArrayBig, arraySize/4, arraySize, true);

        System.out.println("");
        System.out.println("Benchmark direct arrays");
        profileJavaArray(doubleArray, nIterations);
        
        // Using the iterator should introduce no performance penalty. The
        // implementation is such that the number of operations are
        // the same as iterating over a full array (an increment and an array
        // access per element
        System.out.println("");
        System.out.println("Benchmark array wrappers using iterator");
        System.out.print("Full array - ");
        profileArrayIterator(doubleCollection, nIterations);
        System.out.print("Sliced array - ");
        profileArrayIterator(doubleCollectionBig, nIterations);

        // Using a for loop introduces some extra operations: a sum and a boundary check
        // for each access. Of these, the boundary check is the one that introduces
        // the biggest penalty, as the sum is done on a final field. The JIT
        // is smart enough to remove the boundary check, but it will take
        // the code to be "warmed up" for it to kick in.
        System.out.println("");
        System.out.println("Benchmark array wrappers using loop");
        System.out.print("Full array - ");
        profileArrayLoop(doubleCollection, nIterations);
        System.out.print("Sliced array - ");
        profileArrayLoop(doubleCollectionBig, nIterations);
        System.out.print("Sliced array after warmup - ");
        profileArrayLoop(doubleCollectionBig, nIterations);
        
        System.out.println("");
        System.out.println("See code comments for details on how to interpret the numbers");
    }

    private static void profileArrayLoop(ArrayDouble array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            double sum = 0;
            for (int j = 0; j < array.size(); j++) {
                sum += array.getDouble(j);
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using ArrayDouble loop: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileArrayIterator(ArrayDouble array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            IteratorNumber iter = array.iterator();
            double sum = 0;
            while (iter.hasNext()) {
                sum += iter.nextDouble();
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using ArrayDouble iterator: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileJavaArray(double[] array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            double sum = 0;
            for (int j = 0; j < array.length; j++) {
                sum += array[j];
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using double[]: ns " + (stopTime - startTime) / nIterations);
    }
}
