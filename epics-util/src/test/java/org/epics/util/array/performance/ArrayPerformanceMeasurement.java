/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array.performance;

import org.epics.util.array.ArrayShort;
import org.epics.util.array.ArrayDouble;
import org.epics.util.array.CollectionNumber;
import org.epics.util.array.ArrayFloat;
import org.epics.util.array.ArrayByte;
import org.epics.util.array.ArrayInteger;
import org.epics.util.compat.legacy.lang.Random;
import org.epics.util.array.ArrayLong;
import org.epics.util.array.IteratorNumber;

import static org.epics.util.array.CollectionNumbers.*;

/**
 * Benchmark and example of how to use the util.array package without losing
 * performance.
 *
 * @author carcassi
 */
public class ArrayPerformanceMeasurement {

    public static void main(String[] args) {
        // Test parameters
        // Size of the array to test
        int arraySize = 100000;
        // Number of iterations to perform
        int nIterations = 10000;
        System.out.println("This benchmark will prepare arrays of all types with " + arraySize + " elements");
        System.out.println("It will calculate the sum of all elements " + nIterations + " times and report the time in nanoseconds");
        System.out.println("The tests are conducted on straight array, wrapped array using ArrayXxx and through the abstract CollectionNumber");
        System.out.println("");
        System.out.println("Current Java version is " + System.getProperty("java.version"));

        System.out.println("");
        System.out.println("Preparing data");
        double[] doubleArray = new double[arraySize];
        float[] floatArray = new float[arraySize];
        long[] longArray = new long[arraySize];
        int[] intArray = new int[arraySize];
        short[] shortArray = new short[arraySize];
        byte[] byteArray = new byte[arraySize];
        Random rand = new Random();
        for (int i = 0; i < doubleArray.length; i++) {
            doubleArray[i] = rand.nextGaussian();
            floatArray[i] = (float) rand.nextGaussian();
            longArray[i] = rand.nextInt(100);
            intArray[i] = rand.nextInt(100);
            shortArray[i] = (short) rand.nextInt(100);
        }
        rand.nextBytes(byteArray);

        ArrayDouble doubleCollection = unmodifiableListDouble(doubleArray);
        ArrayFloat floatCollection = unmodifiableListFloat(floatArray);
        ArrayLong longCollection = unmodifiableListLong(longArray);
        ArrayInteger intCollection = unmodifiableListInt(intArray);
        ArrayShort shortCollection = unmodifiableListShort(shortArray);
        ArrayByte byteCollection = unmodifiableListByte(byteArray);

        System.out.println("");
        System.out.println("Benchmark direct arrays");
        profileJavaArray(doubleArray, nIterations);
        profileJavaArray(floatArray, nIterations);
        profileJavaArray(longArray, nIterations);
        profileJavaArray(intArray, nIterations);
        profileJavaArray(shortArray, nIterations);
        profileJavaArray(byteArray, nIterations);

        System.out.println("");
        System.out.println("Benchmark array wrappers");
        profileArray(doubleCollection, nIterations);
        profileArray(floatCollection, nIterations);
        profileArray(longCollection, nIterations);
        profileArray(intCollection, nIterations);
        profileArray(shortCollection, nIterations);
        profileArray(byteCollection, nIterations);

        // As time of writing, the performance impact of the wrapper itself is negligible:
        // the difference is within the fluctuations within different runs.
        // This means one can always code to ArrayDouble instead of double[] and
        // suffer no performance penalty.

        System.out.println("");
        System.out.println("Benchmark array wrappers through common abstract class");
        // Note: rearrenging the order will change which type executes faster
        // The first couple of iterations will be faster: if only a couple of
        // implementation of an interface are used, the JIT will inline with a
        // simple switch.
        profileCollectionNumber(doubleCollection, nIterations);
        profileCollectionNumber(floatCollection, nIterations);
        // After a couple of implementations of the same class are used, JIT
        // will de-optimize and implement a full lookup table. At this point the
        // abstract function call should dominate.
        profileCollectionNumber(longCollection, nIterations);
        profileCollectionNumber(intCollection, nIterations);
        profileCollectionNumber(shortCollection, nIterations);
        profileCollectionNumber(byteCollection, nIterations);
        // Note that the double array performance regressed because of the JIT
        // de-optimization.
        profileCollectionNumber(doubleCollection, nIterations);

        // Conclusion: if a program uses only one implementation, say ArrayDouble,
        // of CollectionNumber, using the interfaces has no penalty. If it uses
        // multiple implementations, it will introduce the penalty. The penalty
        // can be eliminated by writing multiple copies of the code for each
        // concrete array class. This is no worse than writing multiple versions
        // of the code to the different array classes.

        // What one should do is first code to the abstract class:
        //    doStuff(CollectionNumber col) {
        //         ...
        //    }
        // Then profile and if the performance bottleneck is the actual use of abstract calls,
        // optimize the particular case:
        //    doStuff(CollectionNumber col) {
        //         if (col instanceof ArrayDouble) {
        //             doStuffImpl((ArrayDouble) col);
        //             return;
        //         }
        //         ...
        //    }
        //    doStuffImpl(ArrayDouble col) {
        //         ...
        //    }
        //

        System.out.println("");
        System.out.println("See code comments for details on how to interpret the numbers");
    }

    // Iterations using abstract interfaces

    private static double computeSum(CollectionNumber collection) {
        IteratorNumber iter = collection.iterator();
        double sum = 0;
        while (iter.hasNext()) {
            sum += iter.nextDouble();
        }
        return sum;
    }

    private static void profileCollectionNumber(CollectionNumber list, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            double sum = computeSum(list);
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using " + list.getClass().getSimpleName() + " through abstract class: ns " + (stopTime - startTime) / nIterations);
    }

    // Iterations using concrete classes. Note that the implementation
    // differ only in the type of the array parameter

    private static void profileArray(ArrayDouble array, int nIterations) {
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

        System.out.println("Iteration using ArrayDouble: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileArray(ArrayFloat array, int nIterations) {
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

        System.out.println("Iteration using ArrayFloat: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileArray(ArrayLong array, int nIterations) {
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

        System.out.println("Iteration using ArrayLong: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileArray(ArrayInteger array, int nIterations) {
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

        System.out.println("Iteration using ArrayInt: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileArray(ArrayShort array, int nIterations) {
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

        System.out.println("Iteration using ArrayShort: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileArray(ArrayByte array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            double sum = 0;
            for (int j = 0; j < array.size(); j++) {
                sum += array.getByte(j);
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using ArrayByte: ns " + (stopTime - startTime) / nIterations);
    }

    // Iterations using direct arrays

    private static void profileJavaArray(double[] array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            double sum = 0;
            for (double v : array) {
                sum += v;
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using double[]: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileJavaArray(float[] array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            double sum = 0;
            for (float v : array) {
                sum += v;
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using float[]: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileJavaArray(long[] array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            double sum = 0;
            for (long l : array) {
                sum += l;
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using long[]: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileJavaArray(int[] array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            double sum = 0;
            for (int k : array) {
                sum += k;
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using int[]: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileJavaArray(short[] array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            double sum = 0;
            for (short value : array) {
                sum += value;
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using short[]: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileJavaArray(byte[] array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            double sum = 0;
            for (byte b : array) {
                sum += b;
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using byte[]: ns " + (stopTime - startTime) / nIterations);
    }
}
