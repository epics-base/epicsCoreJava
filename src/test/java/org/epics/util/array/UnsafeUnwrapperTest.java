/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

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
        ListNumber coll = new ArrayFloat(array);
        testArraySame(UnsafeUnwrapper.wrappedArray(coll), array, 0, 10);
    }

    @Test
    public void wrappedArray2() {
        byte[] array = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayByte(array);
        testArraySame(UnsafeUnwrapper.wrappedArray(coll), array, 0, 10);
    }

    @Test
    public void wrappedFloatArray1() {
        float[] array = new float[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayFloat(array);
        testArraySame(UnsafeUnwrapper.wrappedFloatArray(coll), array, 0, 10);
    }

    @Test
    public void wrappedFloatArray2() {
        double[] array = new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayDouble(array);
        assertThat(UnsafeUnwrapper.wrappedFloatArray(coll), nullValue());
    }

    @Test
    public void floatArrayWrappedOrCopy1(){
        float[] array = new float[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayFloat(array);
        testArraySame(UnsafeUnwrapper.floatArrayWrappedOrCopy(coll), array, 0, 10);
    }

    @Test
    public void floatArrayWrappedOrCopy2(){
        double[] array = new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayDouble(array);
        testArrayEquals(UnsafeUnwrapper.floatArrayWrappedOrCopy(coll), new float[] {0,1,2,3,4,5,6,7,8,9}, 0, 10);
    }

    @Test
    public void wrappedDoubleArray1() {
        double[] array = new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayDouble(array);
        testArraySame(UnsafeUnwrapper.wrappedDoubleArray(coll), array, 0, 10);
    }

    @Test
    public void wrappedDoubleArray2() {
        float[] array = new float[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayFloat(array);
        assertThat(UnsafeUnwrapper.wrappedDoubleArray(coll), nullValue());
    }

    @Test
    public void doubleArrayWrappedOrCopy1(){
        double[] array = new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayDouble(array);
        testArraySame(UnsafeUnwrapper.doubleArrayWrappedOrCopy(coll), array, 0, 10);
    }

    @Test
    public void doubleArrayWrappedOrCopy2(){
        float[] array = new float[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayFloat(array);
        testArrayEquals(UnsafeUnwrapper.doubleArrayWrappedOrCopy(coll), new double[] {0,1,2,3,4,5,6,7,8,9}, 0, 10);
    }

    @Test
    public void wrappedByteArray1() {
        byte[] array = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayByte(array);
        testArraySame(UnsafeUnwrapper.wrappedByteArray(coll), array, 0, 10);
    }

    @Test
    public void wrappedByteArray2() {
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayLong(array);
        assertThat(UnsafeUnwrapper.wrappedByteArray(coll), nullValue());
    }

    @Test
    public void byteArrayWrappedOrCopy1(){
        byte[] array = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayByte(array);
        testArraySame(UnsafeUnwrapper.byteArrayWrappedOrCopy(coll), array, 0, 10);
    }

    @Test
    public void byteArrayWrappedOrCopy2(){
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayLong(array);
        testArrayEquals(UnsafeUnwrapper.byteArrayWrappedOrCopy(coll), new byte[] {0,1,2,3,4,5,6,7,8,9}, 0, 10);
    }

    @Test
    public void wrappedShortArray1() {
        short[] array = new short[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayShort(array);
        testArraySame(UnsafeUnwrapper.wrappedShortArray(coll), array, 0, 10);
    }

    @Test
    public void wrappedShortArray2() {
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayLong(array);
        assertThat(UnsafeUnwrapper.wrappedShortArray(coll), nullValue());
    }

    @Test
    public void shortArrayWrappedOrCopy1(){
        short[] array = new short[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayShort(array);
        testArraySame(UnsafeUnwrapper.shortArrayWrappedOrCopy(coll), array, 0, 10);
    }

    @Test
    public void shortArrayWrappedOrCopy2(){
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayLong(array);
        testArrayEquals(UnsafeUnwrapper.shortArrayWrappedOrCopy(coll), new short[] {0,1,2,3,4,5,6,7,8,9}, 0, 10);
    }

    @Test
    public void wrappedIntArray1() {
        int[] array = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayInt(array);
        testArraySame(UnsafeUnwrapper.wrappedIntArray(coll), array, 0, 10);
    }

    @Test
    public void wrappedIntArray2() {
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayLong(array);
        assertThat(UnsafeUnwrapper.wrappedIntArray(coll), nullValue());
    }

    @Test
    public void intArrayWrappedOrCopy1(){
        int[] array = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayInt(array);
        testArraySame(UnsafeUnwrapper.intArrayWrappedOrCopy(coll), array, 0, 10);
    }

    @Test
    public void intArrayWrappedOrCopy2(){
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayLong(array);
        testArrayEquals(UnsafeUnwrapper.intArrayWrappedOrCopy(coll), new int[] {0,1,2,3,4,5,6,7,8,9}, 0, 10);
    }

    @Test
    public void wrappedLongArray1() {
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayLong(array);
        testArraySame(UnsafeUnwrapper.wrappedLongArray(coll), array, 0, 10);
    }

    @Test
    public void wrappedLongArray2() {
        int[] array = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayInt(array);
        assertThat(UnsafeUnwrapper.wrappedLongArray(coll), nullValue());
    }

    @Test
    public void longArrayWrappedOrCopy1(){
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayLong(array);
        testArraySame(UnsafeUnwrapper.longArrayWrappedOrCopy(coll), array, 0, 10);
    }

    @Test
    public void longArrayWrappedOrCopy2(){
        int[] array = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayInt(array);
        testArrayEquals(UnsafeUnwrapper.longArrayWrappedOrCopy(coll), new long[] {0,1,2,3,4,5,6,7,8,9}, 0, 10);
    }
}
