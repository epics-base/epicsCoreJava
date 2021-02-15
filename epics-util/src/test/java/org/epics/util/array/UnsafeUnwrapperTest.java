/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.epics.util.array.CollectionNumbers.*;

/**
 *
 * @author carcassi
 */
public class UnsafeUnwrapperTest {

    public UnsafeUnwrapperTest() {
    }

    static void testArrayEquals(UnsafeUnwrapper.Array<?> array, Object reference, int referenceStartIndex, int referenceSize) {
        assertThat(array.array, equalTo(reference));
        assertThat(array.startIndex, equalTo(referenceStartIndex));
        assertThat(array.size, equalTo(referenceSize));
    }

    static void testArraySame(UnsafeUnwrapper.Array<?> array, Object reference, int referenceStartIndex, int referenceSize) {
        assertThat(array.array, sameInstance(reference));
        assertThat(array.startIndex, equalTo(referenceStartIndex));
        assertThat(array.size, equalTo(referenceSize));
    }

    @Test
    public void wrappedArray1() {
        float[] array = new float[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListFloat(array);
        testArraySame(UnsafeUnwrapper.wrappedArray(coll), array, 0, 10);
    }

    @Test
    public void wrappedArray2() {
        byte[] array = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListByte(array);
        testArraySame(UnsafeUnwrapper.wrappedArray(coll), array, 0, 10);
    }

    @Test
    public void wrappedDoubleArray1() {
        double[] array = new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = toList(array);
        testArraySame(UnsafeUnwrapper.wrappedDoubleArray(coll), array, 0, 10);
    }

    @Test
    public void wrappedDoubleArray2() {
        float[] array = new float[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListFloat(array);
        assertThat(UnsafeUnwrapper.wrappedDoubleArray(coll), nullValue());
    }

    @Test
    public void readSafeDoubleArray1(){
        double[] array = new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = toList(array);
        testArraySame(UnsafeUnwrapper.readSafeDoubleArray(coll), array, 0, 10);
    }

    @Test
    public void readSafeDoubleArray2(){
        float[] array = new float[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListFloat(array);
        testArrayEquals(UnsafeUnwrapper.readSafeDoubleArray(coll), new double[] {0,1,2,3,4,5,6,7,8,9}, 0, 10);
    }

    @Test
    public void writeSafeDoubleArray1(){
        double[] array = new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = toList(array);
        testArraySame(UnsafeUnwrapper.writeSafeDoubleArray(coll), array, 0, 10);
    }

    @Test
    public void writeSafeDoubleArray2(){
        double[] array = new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListDouble(array);
        testArrayEquals(UnsafeUnwrapper.writeSafeDoubleArray(coll), array, 0, 10);
    }

    @Test
    public void writeSafeDoubleArray3(){
        float[] array = new float[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListFloat(array);
        testArrayEquals(UnsafeUnwrapper.writeSafeDoubleArray(coll), new double[] {0,1,2,3,4,5,6,7,8,9}, 0, 10);
    }


    @Test
    public void wrappedFloatArray1() {
        float[] array = new float[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListFloat(array);
        testArraySame(UnsafeUnwrapper.wrappedFloatArray(coll), array, 0, 10);
    }

    @Test
    public void wrappedFloatArray2() {
        double[] array = new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = toList(array);
        assertThat(UnsafeUnwrapper.wrappedFloatArray(coll), nullValue());
    }

    @Test
    public void readSafeFloatArray1(){
        float[] array = new float[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListFloat(array);
        testArraySame(UnsafeUnwrapper.readSafeFloatArray(coll), array, 0, 10);
    }

    @Test
    public void readSafeFloatArray2(){
        double[] array = new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = toList(array);
        testArrayEquals(UnsafeUnwrapper.readSafeFloatArray(coll), new float[] {0,1,2,3,4,5,6,7,8,9}, 0, 10);
    }

    @Test
    public void writeSafeFloatArray1(){
        float[] array = new float[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = toList(array);
        testArraySame(UnsafeUnwrapper.writeSafeFloatArray(coll), array, 0, 10);
    }

    @Test
    public void writeSafeFloatArray2(){
        float[] array = new float[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListFloat(array);
        testArrayEquals(UnsafeUnwrapper.writeSafeFloatArray(coll), array, 0, 10);
    }

    @Test
    public void writeSafeFloatArray3(){
        double[] array = new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListDouble(array);
        testArrayEquals(UnsafeUnwrapper.writeSafeFloatArray(coll), new float[] {0,1,2,3,4,5,6,7,8,9}, 0, 10);
    }


    @Test
    public void wrappedByteArray1() {
        byte[] array = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListByte(array);
        testArraySame(UnsafeUnwrapper.wrappedByteArray(coll), array, 0, 10);
    }

    @Test
    public void wrappedByteArray2() {
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListLong(array);
        assertThat(UnsafeUnwrapper.wrappedByteArray(coll), nullValue());
    }

    @Test
    public void readSafeByteArray1(){
        byte[] array = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListByte(array);
        testArraySame(UnsafeUnwrapper.readSafeByteArray(coll), array, 0, 10);
    }

    @Test
    public void readSafeByteArray2(){
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListLong(array);
        testArrayEquals(UnsafeUnwrapper.readSafeByteArray(coll), new byte[] {0,1,2,3,4,5,6,7,8,9}, 0, 10);
    }

    @Test
    public void writeSafeByteArray1(){
        byte[] array = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = toList(array);
        testArraySame(UnsafeUnwrapper.writeSafeByteArray(coll), array, 0, 10);
    }

    @Test
    public void writeSafeByteArray2(){
        byte[] array = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListByte(array);
        testArrayEquals(UnsafeUnwrapper.writeSafeByteArray(coll), array, 0, 10);
    }

    @Test
    public void writeSafeByteArray3(){
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListLong(array);
        testArrayEquals(UnsafeUnwrapper.writeSafeByteArray(coll), new byte[] {0,1,2,3,4,5,6,7,8,9}, 0, 10);
    }


    @Test
    public void wrappedShortArray1() {
        short[] array = new short[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListShort(array);
        testArraySame(UnsafeUnwrapper.wrappedShortArray(coll), array, 0, 10);
    }

    @Test
    public void wrappedShortArray2() {
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListLong(array);
        assertThat(UnsafeUnwrapper.wrappedShortArray(coll), nullValue());
    }

    @Test
    public void readSafeShortArray1(){
        short[] array = new short[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListShort(array);
        testArraySame(UnsafeUnwrapper.readSafeShortArray(coll), array, 0, 10);
    }

    @Test
    public void readSafeShortArray2(){
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListLong(array);
        testArrayEquals(UnsafeUnwrapper.readSafeShortArray(coll), new short[] {0,1,2,3,4,5,6,7,8,9}, 0, 10);
    }

    @Test
    public void writeSafeShortArray1(){
        short[] array = new short[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = toList(array);
        testArraySame(UnsafeUnwrapper.writeSafeShortArray(coll), array, 0, 10);
    }

    @Test
    public void writeSafeShortArray2(){
        short[] array = new short[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListShort(array);
        testArrayEquals(UnsafeUnwrapper.writeSafeShortArray(coll), array, 0, 10);
    }

    @Test
    public void writeSafeShortArray3(){
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListLong(array);
        testArrayEquals(UnsafeUnwrapper.writeSafeShortArray(coll), new short[] {0,1,2,3,4,5,6,7,8,9}, 0, 10);
    }


    @Test
    public void wrappedIntArray1() {
        int[] array = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListInt(array);
        testArraySame(UnsafeUnwrapper.wrappedIntArray(coll), array, 0, 10);
    }

    @Test
    public void wrappedIntArray2() {
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListLong(array);
        assertThat(UnsafeUnwrapper.wrappedIntArray(coll), nullValue());
    }

    @Test
    public void readSafeIntArray1(){
        int[] array = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListInt(array);
        testArraySame(UnsafeUnwrapper.readSafeIntArray(coll), array, 0, 10);
    }

    @Test
    public void readSafeIntArray2(){
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListLong(array);
        testArrayEquals(UnsafeUnwrapper.readSafeIntArray(coll), new int[] {0,1,2,3,4,5,6,7,8,9}, 0, 10);
    }

    @Test
    public void writeSafeIntArray1(){
        int[] array = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = toList(array);
        testArraySame(UnsafeUnwrapper.writeSafeIntArray(coll), array, 0, 10);
    }

    @Test
    public void writeSafeIntArray2(){
        int[] array = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListInt(array);
        testArrayEquals(UnsafeUnwrapper.writeSafeIntArray(coll), array, 0, 10);
    }

    @Test
    public void writeSafeIntArray3(){
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListLong(array);
        testArrayEquals(UnsafeUnwrapper.writeSafeIntArray(coll), new int[] {0,1,2,3,4,5,6,7,8,9}, 0, 10);
    }


    @Test
    public void wrappedLongArray1() {
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListLong(array);
        testArraySame(UnsafeUnwrapper.wrappedLongArray(coll), array, 0, 10);
    }

    @Test
    public void wrappedLongArray2() {
        int[] array = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListInt(array);
        assertThat(UnsafeUnwrapper.wrappedLongArray(coll), nullValue());
    }

    @Test
    public void readSafeLongArray1(){
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListLong(array);
        testArraySame(UnsafeUnwrapper.readSafeLongArray(coll), array, 0, 10);
    }

    @Test
    public void readSafeLongArray2(){
        int[] array = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListInt(array);
        testArrayEquals(UnsafeUnwrapper.readSafeLongArray(coll), new long[] {0,1,2,3,4,5,6,7,8,9}, 0, 10);
    }

    @Test
    public void writeSafeLongArray1(){
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = toList(array);
        testArraySame(UnsafeUnwrapper.writeSafeLongArray(coll), array, 0, 10);
    }

    @Test
    public void writeSafeLongArray2(){
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListLong(array);
        testArrayEquals(UnsafeUnwrapper.writeSafeLongArray(coll), array, 0, 10);
    }

    @Test
    public void writeSafeLongArray3(){
        int[] array = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = unmodifiableListInt(array);
        testArrayEquals(UnsafeUnwrapper.writeSafeLongArray(coll), new long[] {0,1,2,3,4,5,6,7,8,9}, 0, 10);
    }
}
