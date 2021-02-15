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

import static org.epics.util.array.CollectionNumbers.*;

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
        System.out.println("Benchmark array copy");
        profileJavaArrayCopy(doubleArray, new double[arraySize], nIterations);
        profileJavaArrayCopy(floatArray, new float[arraySize], nIterations);
        profileJavaArrayCopy(longArray, new long[arraySize], nIterations);
        profileJavaArrayCopy(intArray, new int[arraySize], nIterations);
        profileJavaArrayCopy(shortArray, new short[arraySize], nIterations);
        profileJavaArrayCopy(byteArray, new byte[arraySize], nIterations);

        System.out.println("");
        System.out.println("Benchmark loop copy");
        profileJavaArrayLoop(doubleArray, new double[arraySize], nIterations);
        profileJavaArrayLoop(floatArray, new float[arraySize], nIterations);
        profileJavaArrayLoop(longArray, new long[arraySize], nIterations);
        profileJavaArrayLoop(intArray, new int[arraySize], nIterations);
        profileJavaArrayLoop(shortArray, new short[arraySize], nIterations);
        profileJavaArrayLoop(byteArray, new byte[arraySize], nIterations);

        System.out.println("");
        System.out.println("Benchmark setAll");
        profileArraySetAll(doubleCollection, toListDouble(new double[arraySize]), nIterations);
        profileArraySetAll(floatCollection, toListFloat(new float[arraySize]), nIterations);
        profileArraySetAll(longCollection, toListLong(new long[arraySize]), nIterations);
        profileArraySetAll(intCollection, toListInt(new int[arraySize]), nIterations);
        profileArraySetAll(shortCollection, toListShort(new short[arraySize]), nIterations);
        profileArraySetAll(byteCollection, toListByte(new byte[arraySize]), nIterations);
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

    private static void profileArraySetAll(ArrayFloat src, ArrayFloat dst, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            dst.setAll(0, src);
            // NOTE: this check is required or the whole computation will be optimized away
            if (dst.getDouble(0) == 0) {
                System.out.println("Unexpected value " + dst.getDouble(0));
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Copy using ArrayFloat.setAll: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileArraySetAll(ArrayLong src, ArrayLong dst, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            dst.setAll(0, src);
            // NOTE: this check is required or the whole computation will be optimized away
            if (dst.getDouble(0) == 0) {
                System.out.println("Unexpected value " + dst.getDouble(0));
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Copy using ArrayLong.setAll: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileArraySetAll(ArrayInteger src, ArrayInteger dst, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            dst.setAll(0, src);
            // NOTE: this check is required or the whole computation will be optimized away
            if (dst.getDouble(0) == 0) {
                System.out.println("Unexpected value " + dst.getDouble(0));
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Copy using ArrayInt.setAll: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileArraySetAll(ArrayShort src, ArrayShort dst, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            dst.setAll(0, src);
            // NOTE: this check is required or the whole computation will be optimized away
            if (dst.getDouble(0) == 0) {
                System.out.println("Unexpected value " + dst.getDouble(0));
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Copy using ArrayShort.setAll: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileArraySetAll(ArrayByte src, ArrayByte dst, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            dst.setAll(0, src);
            // NOTE: this check is required or the whole computation will be optimized away
            if (dst.getDouble(0) == 0) {
                System.out.println("Unexpected value " + dst.getDouble(0));
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Copy using ArrayByte.setAll: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileJavaArrayLoop(double[] src, double[] dst, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            System.arraycopy(src, 0, dst, 0, src.length);
            // NOTE: this check is required or the whole computation will be optimized away
            if (dst[0] == 0) {
                System.out.println("Unexpected value " + dst[0]);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Copy using loop copy on double[]: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileJavaArrayLoop(float[] src, float[] dst, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            System.arraycopy(src, 0, dst, 0, src.length);
            // NOTE: this check is required or the whole computation will be optimized away
            if (dst[0] == 0) {
                System.out.println("Unexpected value " + dst[0]);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Copy using loop copy on float[]: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileJavaArrayLoop(long[] src, long[] dst, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            System.arraycopy(src, 0, dst, 0, src.length);
            // NOTE: this check is required or the whole computation will be optimized away
            if (dst[0] == 0) {
                System.out.println("Unexpected value " + dst[0]);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Copy using loop copy on long[]: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileJavaArrayLoop(int[] src, int[] dst, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            System.arraycopy(src, 0, dst, 0, src.length);
            // NOTE: this check is required or the whole computation will be optimized away
            if (dst[0] == 0) {
                System.out.println("Unexpected value " + dst[0]);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Copy using loop copy on int[]: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileJavaArrayLoop(short[] src, short[] dst, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            System.arraycopy(src, 0, dst, 0, src.length);
            // NOTE: this check is required or the whole computation will be optimized away
            if (dst[0] == 0) {
                System.out.println("Unexpected value " + dst[0]);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Copy using loop copy on short[]: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileJavaArrayLoop(byte[] src, byte[] dst, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            System.arraycopy(src, 0, dst, 0, src.length);
            // NOTE: this check is required or the whole computation will be optimized away
            if (dst[0] == 0) {
                System.out.println("Unexpected value " + dst[0]);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Copy using loop copy on byte[]: ns " + (stopTime - startTime) / nIterations);
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

    private static void profileJavaArrayCopy(float[] src, float[] dst, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            System.arraycopy(src, 0, dst, 0, src.length);
            // NOTE: this check is required or the whole computation will be optimized away
            if (dst[0] == 0) {
                System.out.println("Unexpected value " + dst[0]);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Copy using System.arraycopy on float[]: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileJavaArrayCopy(long[] src, long[] dst, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            System.arraycopy(src, 0, dst, 0, src.length);
            // NOTE: this check is required or the whole computation will be optimized away
            if (dst[0] == 0) {
                System.out.println("Unexpected value " + dst[0]);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Copy using System.arraycopy on long[]: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileJavaArrayCopy(int[] src, int[] dst, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            System.arraycopy(src, 0, dst, 0, src.length);
            // NOTE: this check is required or the whole computation will be optimized away
            if (dst[0] == 0) {
                System.out.println("Unexpected value " + dst[0]);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Copy using System.arraycopy on int[]: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileJavaArrayCopy(short[] src, short[] dst, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            System.arraycopy(src, 0, dst, 0, src.length);
            // NOTE: this check is required or the whole computation will be optimized away
            if (dst[0] == 0) {
                System.out.println("Unexpected value " + dst[0]);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Copy using System.arraycopy on short[]: ns " + (stopTime - startTime) / nIterations);
    }

    private static void profileJavaArrayCopy(byte[] src, byte[] dst, int nIterations) {
        long startTime = System.nanoTime();
        for (int i = 0; i < nIterations; i++) {
            System.arraycopy(src, 0, dst, 0, src.length);
            // NOTE: this check is required or the whole computation will be optimized away
            if (dst[0] == 0) {
                System.out.println("Unexpected value " + dst[0]);
            }
        }
        long stopTime = System.nanoTime();

        System.out.println("Copy using System.arraycopy on byte[]: ns " + (stopTime - startTime) / nIterations);
    }
}
