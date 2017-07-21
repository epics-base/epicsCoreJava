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
