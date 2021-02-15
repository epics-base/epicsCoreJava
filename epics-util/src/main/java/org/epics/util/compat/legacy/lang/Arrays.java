package org.epics.util.compat.legacy.lang;


import java.util.Comparator;
import java.util.List;

/**
 * Delegate implementation of java.util.Arrays class
 *
 * @author George McIntyre. 15-Feb-2021, SLAC
 */
public class Arrays {
    public static long[] copyOf(long[] original, int newLength) {
        long[] copy = new long[newLength];
        System.arraycopy(original, 0, copy, 0,
                Math.min(original.length, newLength));
        return copy;
    }

    public static String[] copyOfRange(String[] original, int from, int to) {
        int length = to - from;
        String[] copy = new String[length];
        System.arraycopy(original, from, copy, 0, length);
        return copy;
    }
    ////// Delegates

    public static void sort(long[] longs) {
        java.util.Arrays.sort(longs);
    }

    public static void sort(long[] longs, int i, int i1) {
        java.util.Arrays.sort(longs, i, i1);
    }

    public static void sort(int[] ints) {
        java.util.Arrays.sort(ints);
    }

    public static void sort(int[] ints, int i, int i1) {
        java.util.Arrays.sort(ints, i, i1);
    }

    public static void sort(short[] shorts) {
        java.util.Arrays.sort(shorts);
    }

    public static void sort(short[] shorts, int i, int i1) {
        java.util.Arrays.sort(shorts, i, i1);
    }

    public static void sort(char[] chars) {
        java.util.Arrays.sort(chars);
    }

    public static void sort(char[] chars, int i, int i1) {
        java.util.Arrays.sort(chars, i, i1);
    }

    public static void sort(byte[] bytes) {
        java.util.Arrays.sort(bytes);
    }

    public static void sort(byte[] bytes, int i, int i1) {
        java.util.Arrays.sort(bytes, i, i1);
    }

    public static void sort(double[] doubles) {
        java.util.Arrays.sort(doubles);
    }

    public static void sort(double[] doubles, int i, int i1) {
        java.util.Arrays.sort(doubles, i, i1);
    }

    public static void sort(float[] floats) {
        java.util.Arrays.sort(floats);
    }

    public static void sort(float[] floats, int i, int i1) {
        java.util.Arrays.sort(floats, i, i1);
    }

    public static void sort(Object[] objects) {
        java.util.Arrays.sort(objects);
    }

    public static void sort(Object[] objects, int i, int i1) {
        java.util.Arrays.sort(objects, i, i1);
    }

    public static <T> void sort(T[] ts, Comparator<? super T> comparator) {
        java.util.Arrays.sort(ts, comparator);
    }

    public static <T> void sort(T[] ts, int i, int i1, Comparator<? super T> comparator) {
        java.util.Arrays.sort(ts, i, i1, comparator);
    }

    public static int binarySearch(long[] longs, long l) {
        return java.util.Arrays.binarySearch(longs, l);
    }

    public static int binarySearch(int[] ints, int i) {
        return java.util.Arrays.binarySearch(ints, i);
    }

    public static int binarySearch(short[] shorts, short i) {
        return java.util.Arrays.binarySearch(shorts, i);
    }

    public static int binarySearch(char[] chars, char c) {
        return java.util.Arrays.binarySearch(chars, c);
    }

    public static int binarySearch(byte[] bytes, byte b) {
        return java.util.Arrays.binarySearch(bytes, b);
    }

    public static int binarySearch(double[] doubles, double v) {
        return java.util.Arrays.binarySearch(doubles, v);
    }

    public static int binarySearch(float[] floats, float v) {
        return java.util.Arrays.binarySearch(floats, v);
    }

    public static int binarySearch(Object[] objects, Object o) {
        return java.util.Arrays.binarySearch(objects, o);
    }

    public static <T> int binarySearch(T[] ts, T t, Comparator<? super T> comparator) {
        return java.util.Arrays.binarySearch(ts, t, comparator);
    }

    public static boolean equals(long[] longs, long[] longs1) {
        return java.util.Arrays.equals(longs, longs1);
    }

    public static boolean equals(int[] ints, int[] ints1) {
        return java.util.Arrays.equals(ints, ints1);
    }

    public static boolean equals(short[] shorts, short[] shorts1) {
        return java.util.Arrays.equals(shorts, shorts1);
    }

    public static boolean equals(char[] chars, char[] chars1) {
        return java.util.Arrays.equals(chars, chars1);
    }

    public static boolean equals(byte[] bytes, byte[] bytes1) {
        return java.util.Arrays.equals(bytes, bytes1);
    }

    public static boolean equals(boolean[] booleans, boolean[] booleans1) {
        return java.util.Arrays.equals(booleans, booleans1);
    }

    public static boolean equals(double[] doubles, double[] doubles1) {
        return java.util.Arrays.equals(doubles, doubles1);
    }

    public static boolean equals(float[] floats, float[] floats1) {
        return java.util.Arrays.equals(floats, floats1);
    }

    public static boolean equals(Object[] objects, Object[] objects1) {
        return java.util.Arrays.equals(objects, objects1);
    }

    public static void fill(long[] longs, long l) {
        java.util.Arrays.fill(longs, l);
    }

    public static void fill(long[] longs, int i, int i1, long l) {
        java.util.Arrays.fill(longs, i, i1, l);
    }

    public static void fill(int[] ints, int i) {
        java.util.Arrays.fill(ints, i);
    }

    public static void fill(int[] ints, int i, int i1, int i2) {
        java.util.Arrays.fill(ints, i, i1, i2);
    }

    public static void fill(short[] shorts, short i) {
        java.util.Arrays.fill(shorts, i);
    }

    public static void fill(short[] shorts, int i, int i1, short i2) {
        java.util.Arrays.fill(shorts, i, i1, i2);
    }

    public static void fill(char[] chars, char c) {
        java.util.Arrays.fill(chars, c);
    }

    public static void fill(char[] chars, int i, int i1, char c) {
        java.util.Arrays.fill(chars, i, i1, c);
    }

    public static void fill(byte[] bytes, byte b) {
        java.util.Arrays.fill(bytes, b);
    }

    public static void fill(byte[] bytes, int i, int i1, byte b) {
        java.util.Arrays.fill(bytes, i, i1, b);
    }

    public static void fill(boolean[] booleans, boolean b) {
        java.util.Arrays.fill(booleans, b);
    }

    public static void fill(boolean[] booleans, int i, int i1, boolean b) {
        java.util.Arrays.fill(booleans, i, i1, b);
    }

    public static void fill(double[] doubles, double v) {
        java.util.Arrays.fill(doubles, v);
    }

    public static void fill(double[] doubles, int i, int i1, double v) {
        java.util.Arrays.fill(doubles, i, i1, v);
    }

    public static void fill(float[] floats, float v) {
        java.util.Arrays.fill(floats, v);
    }

    public static void fill(float[] floats, int i, int i1, float v) {
        java.util.Arrays.fill(floats, i, i1, v);
    }

    public static void fill(Object[] objects, Object o) {
        java.util.Arrays.fill(objects, o);
    }

    public static void fill(Object[] objects, int i, int i1, Object o) {
        java.util.Arrays.fill(objects, i, i1, o);
    }

    public static <T> List<T> asList(T... ts) {
        return java.util.Arrays.asList(ts);
    }

    public static int hashCode(long[] longs) {
        return java.util.Arrays.hashCode(longs);
    }

    public static int hashCode(int[] ints) {
        return java.util.Arrays.hashCode(ints);
    }

    public static int hashCode(short[] shorts) {
        return java.util.Arrays.hashCode(shorts);
    }

    public static int hashCode(char[] chars) {
        return java.util.Arrays.hashCode(chars);
    }

    public static int hashCode(byte[] bytes) {
        return java.util.Arrays.hashCode(bytes);
    }

    public static int hashCode(boolean[] booleans) {
        return java.util.Arrays.hashCode(booleans);
    }

    public static int hashCode(float[] floats) {
        return java.util.Arrays.hashCode(floats);
    }

    public static int hashCode(double[] doubles) {
        return java.util.Arrays.hashCode(doubles);
    }

    public static int hashCode(Object[] objects) {
        return java.util.Arrays.hashCode(objects);
    }

    public static int deepHashCode(Object[] objects) {
        return java.util.Arrays.deepHashCode(objects);
    }

    public static boolean deepEquals(Object[] objects, Object[] objects1) {
        return java.util.Arrays.deepEquals(objects, objects1);
    }

    public static String toString(long[] longs) {
        return java.util.Arrays.toString(longs);
    }

    public static String toString(int[] ints) {
        return java.util.Arrays.toString(ints);
    }

    public static String toString(short[] shorts) {
        return java.util.Arrays.toString(shorts);
    }

    public static String toString(char[] chars) {
        return java.util.Arrays.toString(chars);
    }

    public static String toString(byte[] bytes) {
        return java.util.Arrays.toString(bytes);
    }

    public static String toString(boolean[] booleans) {
        return java.util.Arrays.toString(booleans);
    }

    public static String toString(float[] floats) {
        return java.util.Arrays.toString(floats);
    }

    public static String toString(double[] doubles) {
        return java.util.Arrays.toString(doubles);
    }

    public static String toString(Object[] objects) {
        return java.util.Arrays.toString(objects);
    }

    public static String deepToString(Object[] objects) {
        return java.util.Arrays.deepToString(objects);
    }
}
