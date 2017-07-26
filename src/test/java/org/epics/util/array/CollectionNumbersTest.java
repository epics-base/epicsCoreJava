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
public class CollectionNumbersTest {

    public CollectionNumbersTest() {
    }

    @Test
    public void arrayCopyDouble1(){
        ArrayDouble coll = new ArrayDouble(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9);
        double[] dst = new double[10];
        CollectionNumbers.arrayCopy(coll, dst, 0);
        assertThat(dst, equalTo(new double[] {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9}));
    }

    @Test
    public void arrayCopyDouble2(){
        ArrayDouble coll = new ArrayDouble(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9);
        double[] dst = new double[15];
        CollectionNumbers.arrayCopy(coll, dst, 0);
        assertThat(dst, equalTo(new double[] {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0, 0, 0, 0, 0}));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void arrayCopyDouble3(){
        ArrayDouble coll = new ArrayDouble(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9);
        double[] dst = new double[5];
        CollectionNumbers.arrayCopy(coll, dst, 0);
    }

    @Test
    public void arrayCopyFloat1(){
        ArrayDouble coll = new ArrayDouble(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9);
        float[] dst = new float[10];
        CollectionNumbers.arrayCopy(coll, dst, 0);
        assertThat(dst, equalTo(new float[] {0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f}));
    }

    @Test
    public void arrayCopyFloat2(){
        ArrayDouble coll = new ArrayDouble(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9);
        float[] dst = new float[15];
        CollectionNumbers.arrayCopy(coll, dst, 0);
        assertThat(dst, equalTo(new float[] {0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 0, 0, 0, 0, 0}));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void arrayCopyFloat3(){
        ArrayDouble coll = new ArrayDouble(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9);
        float[] dst = new float[5];
        CollectionNumbers.arrayCopy(coll, dst, 0);
    }

    @Test
    public void arrayCopyLong1(){
        ArrayLong coll = new ArrayLong(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        long[] dst = new long[10];
        CollectionNumbers.arrayCopy(coll, dst, 0);
        assertThat(dst, equalTo(new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9}));
    }

    @Test
    public void arrayCopyLong2(){
        ArrayLong coll = new ArrayLong(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        long[] dst = new long[15];
        CollectionNumbers.arrayCopy(coll, dst, 0);
        assertThat(dst, equalTo(new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0, 0, 0, 0}));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void arrayCopyLong3(){
        ArrayLong coll = new ArrayLong(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        long[] dst = new long[5];
        CollectionNumbers.arrayCopy(coll, dst, 0);
    }

    @Test
    public void arrayCopyInt1(){
        ArrayLong coll = new ArrayLong(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        int[] dst = new int[10];
        CollectionNumbers.arrayCopy(coll, dst, 0);
        assertThat(dst, equalTo(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9}));
    }

    @Test
    public void arrayCopyInt2(){
        ArrayLong coll = new ArrayLong(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        int[] dst = new int[15];
        CollectionNumbers.arrayCopy(coll, dst, 0);
        assertThat(dst, equalTo(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0, 0, 0, 0}));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void arrayCopyInt3(){
        ArrayLong coll = new ArrayLong(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        int[] dst = new int[5];
        CollectionNumbers.arrayCopy(coll, dst, 0);
    }

    @Test
    public void arrayCopyShort1(){
        ArrayLong coll = new ArrayLong(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        short[] dst = new short[10];
        CollectionNumbers.arrayCopy(coll, dst, 0);
        assertThat(dst, equalTo(new short[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9}));
    }

    @Test
    public void arrayCopyShort2(){
        ArrayLong coll = new ArrayLong(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        short[] dst = new short[15];
        CollectionNumbers.arrayCopy(coll, dst, 0);
        assertThat(dst, equalTo(new short[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0, 0, 0, 0}));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void arrayCopyShort3(){
        ArrayLong coll = new ArrayLong(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        short[] dst = new short[5];
        CollectionNumbers.arrayCopy(coll, dst, 0);
    }

    @Test
    public void arrayCopyByte1(){
        ArrayLong coll = new ArrayLong(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        byte[] dst = new byte[10];
        CollectionNumbers.arrayCopy(coll, dst, 0);
        assertThat(dst, equalTo(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9}));
    }

    @Test
    public void arrayCopyByte2(){
        ArrayLong coll = new ArrayLong(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        byte[] dst = new byte[15];
        CollectionNumbers.arrayCopy(coll, dst, 0);
        assertThat(dst, equalTo(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0, 0, 0, 0}));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void arrayCopyByte3(){
        ArrayLong coll = new ArrayLong(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        byte[] dst = new byte[5];
        CollectionNumbers.arrayCopy(coll, dst, 0);
    }

    @Test
    public void floatArrayCopyOf1(){
        float[] array = new float[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ArrayFloat coll = new ArrayFloat(array);
        float[] array2 = CollectionNumbers.floatArrayCopyOf(coll);
        assertThat(array2, equalTo(new float[] {0,1,2,3,4,5,6,7,8,9}));
        assertThat(array2, not(sameInstance(array)));
    }

    @Test
    public void floatArrayCopyOf2(){
        double[] array = new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayDouble(array);
        float[] array2 = CollectionNumbers.floatArrayCopyOf(coll);
        assertThat(array2, equalTo(new float[] {0,1,2,3,4,5,6,7,8,9}));
    }

    @Test
    public void doubleArrayCopyOf1(){
        double[] array = new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayDouble(array);
        double[] array2 = CollectionNumbers.doubleArrayCopyOf(coll);
        assertThat(array2, equalTo(new double[] {0,1,2,3,4,5,6,7,8,9}));
        assertThat(array2, not(sameInstance(array)));
    }

    @Test
    public void doubleArrayCopyOf2(){
        float[] array = new float[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayFloat(array);
        double[] array2 = CollectionNumbers.doubleArrayCopyOf(coll);
        assertThat(array2, equalTo(new double[] {0,1,2,3,4,5,6,7,8,9}));
    }

    @Test
    public void byteArrayCopyOf1(){
        byte[] array = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayByte(array);
        byte[] array2 = CollectionNumbers.byteArrayCopyOf(coll);
        assertThat(array2, equalTo(new byte[] {0,1,2,3,4,5,6,7,8,9}));
        assertThat(array2, not(sameInstance(array)));
    }

    @Test
    public void byteArrayCopyOf2(){
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayLong(array);
        byte[] array2 = CollectionNumbers.byteArrayCopyOf(coll);
        assertThat(array2, equalTo(new byte[] {0,1,2,3,4,5,6,7,8,9}));
    }

    @Test
    public void shortArrayCopyOf1(){
        short[] array = new short[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayShort(array);
        short[] array2 = CollectionNumbers.shortArrayCopyOf(coll);
        assertThat(array2, equalTo(new short[] {0,1,2,3,4,5,6,7,8,9}));
        assertThat(array2, not(sameInstance(array)));
    }

    @Test
    public void shortArrayCopyOf2(){
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayLong(array);
        short[] array2 = CollectionNumbers.shortArrayCopyOf(coll);
        assertThat(array2, equalTo(new short[] {0,1,2,3,4,5,6,7,8,9}));
    }

    @Test
    public void intArrayCopyOf1(){
        int[] array = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayInt(array);
        int[] array2 = CollectionNumbers.intArrayCopyOf(coll);
        assertThat(array2, equalTo(new int[] {0,1,2,3,4,5,6,7,8,9}));
        assertThat(array2, not(sameInstance(array)));
    }

    @Test
    public void intArrayCopyOf2(){
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayLong(array);
        int[] array2 = CollectionNumbers.intArrayCopyOf(coll);
        assertThat(array2, equalTo(new int[] {0,1,2,3,4,5,6,7,8,9}));
    }

    @Test
    public void longArrayCopyOf1(){
        long[] array = new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayLong(array);
        long[] array2 = CollectionNumbers.longArrayCopyOf(coll);
        assertThat(array2, equalTo(new long[] {0,1,2,3,4,5,6,7,8,9}));
        assertThat(array2, not(sameInstance(array)));
    }

    @Test
    public void longArrayCopyOf2(){
        int[] array = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ListNumber coll = new ArrayInt(array);
        long[] array2 = CollectionNumbers.longArrayCopyOf(coll);
        assertThat(array2, equalTo(new long[] {0,1,2,3,4,5,6,7,8,9}));
    }
}
