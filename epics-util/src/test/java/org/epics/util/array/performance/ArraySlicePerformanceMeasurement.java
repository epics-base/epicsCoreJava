/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array.performance;

import org.epics.util.array.ArrayShort;
import org.epics.util.array.ArrayDouble;
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
        double[] doubleArrayBig = new double[2*arraySize];
        float[] floatArrayBig = new float[2*arraySize];
        long[] longArrayBig = new long[2*arraySize];
        int[] intArrayBig = new int[2*arraySize];
        short[] shortArrayBig = new short[2*arraySize];
        byte[] byteArrayBig = new byte[2*arraySize];
        for (int i = 0; i < doubleArrayBig.length; i++) {
            doubleArrayBig[i] = rand.nextGaussian();
            floatArrayBig[i] = (float) rand.nextGaussian();
            longArrayBig[i] = rand.nextInt(100);
            intArrayBig[i] = rand.nextInt(100);
            shortArrayBig[i] = (short) rand.nextInt(100);
        }
        rand.nextBytes(byteArrayBig);

        ArrayDouble doubleCollection = unmodifiableListDouble(doubleArray);
        ArrayFloat floatCollection = unmodifiableListFloat(floatArray);
        ArrayLong longCollection = unmodifiableListLong(longArray);
        ArrayInteger intCollection = unmodifiableListInt(intArray);
        ArrayShort shortCollection = unmodifiableListShort(shortArray);
        ArrayByte byteCollection = unmodifiableListByte(byteArray);
        ArrayDouble doubleCollectionBig = unmodifiableListDouble(doubleArrayBig).subList(arraySize/2, arraySize *3/2);
        ArrayFloat floatCollectionBig = unmodifiableListFloat(floatArrayBig).subList(arraySize/2, arraySize *3/2);
        ArrayLong longCollectionBig = unmodifiableListLong(longArrayBig).subList(arraySize/2, arraySize *3/2);
        ArrayInteger intCollectionBig = unmodifiableListInt(intArrayBig).subList(arraySize/2, arraySize *3/2);
        ArrayShort shortCollectionBig = unmodifiableListShort(shortArrayBig).subList(arraySize/2, arraySize *3/2);
        ArrayByte byteCollectionBig = unmodifiableListByte(byteArrayBig).subList(arraySize/2, arraySize *3/2);

        System.out.println("");
        System.out.println("Benchmark direct arrays");
        profileJavaArray(doubleArray, nIterations);
        profileJavaArray(floatArray, nIterations);
        profileJavaArray(longArray, nIterations);
        profileJavaArray(intArray, nIterations);
        profileJavaArray(shortArray, nIterations);
        profileJavaArray(byteArray, nIterations);

        // The only performance penalty introduced by the iterator is the
        // cost of the method calls.
        // The implementation is such that the number of operations are
        // the same as iterating over a full array (an increment and an array
        // access per element. The method called gets inlined after a small warmup.
        System.out.println("");
        System.out.println("Benchmark array wrappers using iterator");
        System.out.print("Full array warmup - ");
        profileArrayIterator(doubleCollection, 10);
        System.out.print("Full array after warmup - ");
        profileArrayIterator(doubleCollection, nIterations);
        System.out.print("Sliced array - ");
        profileArrayIterator(doubleCollectionBig, nIterations);
        System.out.print("Full array warmup - ");
        profileArrayIterator(floatCollection, 10);
        System.out.print("Full array after warmup - ");
        profileArrayIterator(floatCollection, nIterations);
        System.out.print("Sliced array - ");
        profileArrayIterator(floatCollectionBig, nIterations);
        System.out.print("Full array warmup - ");
        profileArrayIterator(longCollection, 10);
        System.out.print("Full array after warmup - ");
        profileArrayIterator(longCollection, nIterations);
        System.out.print("Sliced array - ");
        profileArrayIterator(longCollectionBig, nIterations);
        System.out.print("Full array warmup - ");
        profileArrayIterator(intCollection, 10);
        System.out.print("Full array after warmup - ");
        profileArrayIterator(intCollection, nIterations);
        System.out.print("Sliced array after warmup - ");
        profileArrayIterator(intCollectionBig, nIterations);
        System.out.print("Full array warmup - ");
        profileArrayIterator(shortCollection, 10);
        System.out.print("Full array after warmup - ");
        profileArrayIterator(shortCollection, nIterations);
        System.out.print("Sliced array after warmup - ");
        profileArrayIterator(shortCollectionBig, nIterations);
        System.out.print("Full array warmup - ");
        profileArrayIterator(byteCollection, 10);
        System.out.print("Full array after warmup - ");
        profileArrayIterator(byteCollection, nIterations);
        System.out.print("Sliced array after warmup - ");
        profileArrayIterator(byteCollectionBig, nIterations);

        // Using a for loop introduces some extra operations: a sum and a boundary check
        // for each access. Of these, the boundary check is the one that introduces
        // the biggest penalty, as the sum is done on a final field. The JIT
        // is smart enough to remove the boundary check, but it will take
        // the code to be "warmed up" for it to kick in.
        System.out.println("");
        System.out.println("Benchmark array wrappers using loop");
        System.out.print("Full array - ");
        profileArrayLoop(doubleCollection, nIterations);
        System.out.print("Sliced array warmup - ");
        profileArrayLoop(doubleCollectionBig, 10);
        System.out.print("Sliced array after warmup - ");
        profileArrayLoop(doubleCollectionBig, nIterations);
        System.out.print("Full array - ");
        profileArrayLoop(floatCollection, nIterations);
        System.out.print("Sliced array warmup - ");
        profileArrayLoop(floatCollectionBig, 10);
        System.out.print("Sliced array after warmup - ");
        profileArrayLoop(floatCollectionBig, nIterations);
        System.out.print("Full array - ");
        profileArrayLoop(longCollection, nIterations);
        System.out.print("Sliced array warmup - ");
        profileArrayLoop(longCollectionBig, 10);
        System.out.print("Sliced array after warmup - ");
        profileArrayLoop(longCollectionBig, nIterations);
        System.out.print("Full array - ");
        profileArrayLoop(intCollection, nIterations);
        System.out.print("Sliced array warmup - ");
        profileArrayLoop(intCollectionBig, 10);
        System.out.print("Sliced array after warmup - ");
        profileArrayLoop(intCollectionBig, nIterations);
        System.out.print("Full array - ");
        profileArrayLoop(shortCollection, nIterations);
        System.out.print("Sliced array warmup - ");
        profileArrayLoop(shortCollectionBig, 10);
        System.out.print("Sliced array after warmup - ");
        profileArrayLoop(shortCollectionBig, nIterations);
        System.out.print("Full array - ");
        profileArrayLoop(byteCollection, nIterations);
        System.out.print("Sliced array warmup - ");
        profileArrayLoop(byteCollectionBig, 10);
        System.out.print("Sliced array after warmup - ");
        profileArrayLoop(byteCollectionBig, nIterations);

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

    private static void profileArrayLoop(ArrayFloat array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            float sum = 0;
            for (int j = 0; j < array.size(); j++) {
                sum += array.getFloat(j);
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using ArrayFloat loop: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileArrayLoop(ArrayLong array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            long sum = 0;
            for (int j = 0; j < array.size(); j++) {
                sum += array.getLong(j);
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using ArrayLong loop: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileArrayLoop(ArrayInteger array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            int sum = 0;
            for (int j = 0; j < array.size(); j++) {
                sum += array.getInt(j);
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using ArrayInt loop: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileArrayLoop(ArrayShort array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            short sum = 0;
            for (int j = 0; j < array.size(); j++) {
                sum += array.getShort(j);
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using ArrayShort loop: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileArrayLoop(ArrayByte array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            byte sum = 0;
            for (int j = 0; j < array.size(); j++) {
                sum += array.getByte(j);
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using ArrayByte loop: ns " + (stopTime - startTime) / nIterations);
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

    private static void profileArrayIterator(ArrayFloat array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            IteratorNumber iter = array.iterator();
            float sum = 0;
            while (iter.hasNext()) {
                sum += iter.nextFloat();
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using ArrayFloat iterator: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileArrayIterator(ArrayLong array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            IteratorNumber iter = array.iterator();
            long sum = 0;
            while (iter.hasNext()) {
                sum += iter.nextLong();
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using ArrayLong iterator: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileArrayIterator(ArrayInteger array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            IteratorNumber iter = array.iterator();
            int sum = 0;
            while (iter.hasNext()) {
                sum += iter.nextInt();
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using ArrayInt iterator: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileArrayIterator(ArrayShort array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            IteratorNumber iter = array.iterator();
            short sum = 0;
            while (iter.hasNext()) {
                sum += iter.nextShort();
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using ArrayShort iterator: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileArrayIterator(ArrayByte array, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            IteratorNumber iter = array.iterator();
            byte sum = 0;
            while (iter.hasNext()) {
                sum += iter.nextByte();
            }
            // NOTE: this check is required or the whole computation will be optimized away
            if (sum == 0) {
                System.out.println("Unexpected value " + sum);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Iteration using ArrayByte iterator: ns " + (stopTime - startTime) / nIterations);
    }

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
            float sum = 0;
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
            long sum = 0;
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
            int sum = 0;
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
            short sum = 0;
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
            byte sum = 0;
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
