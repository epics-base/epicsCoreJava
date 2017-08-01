/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class ArrayDoubleTest {

    public ArrayDoubleTest() {
    }

    @Test
    public void wrap1() {
        ArrayDouble array = new ArrayDouble(new double[] {0, 1, 2, 3, 4, 5});
        assertThat(CollectionNumbers.doubleArrayCopyOf(array), equalTo(new double[] {0, 1, 2, 3, 4, 5}));
    }

    @Test(expected=UnsupportedOperationException.class)
    public void wrap2() {
        ArrayDouble array = new ArrayDouble(0, 1, 2, 3, 4, 5);
        array.setDouble(0, 0);
    }

    @Test
    public void wrap3() {
        ArrayDouble array = new ArrayDouble(new double[] {0, 1, 2, 3, 4, 5}, false);
        array.setDouble(0, 5);
        array.setDouble(5, 0);
        assertThat(CollectionNumbers.doubleArrayCopyOf(array), equalTo(new double[] {5, 1, 2, 3, 4, 0}));
    }

    @Test
    public void equals1() {
        ArrayDouble array = new ArrayDouble(new double[] {Double.MIN_VALUE}, false);
        assertThat(array, equalTo(array));
    }

    @Test
    public void equals2() {
        ArrayDouble array = new ArrayDouble(new double[] {Double.MIN_VALUE}, false);
        assertThat(array, not(equalTo(null)));
    }

    @Test
    public void equals3() {
        ArrayDouble array = new ArrayDouble(new double[] {Double.MIN_VALUE}, false);
        ArrayDouble array2 = new ArrayDouble(new double[] {Double.MIN_VALUE}, false);
        assertThat(array, equalTo(array2));
    }

    @Test
    public void equals4() {
        ArrayDouble array = new ArrayDouble(new double[] {Double.MIN_VALUE}, false);
        ArrayDouble array2 = new ArrayDouble(new double[] {Double.MAX_VALUE}, false);
        assertThat(array, not(equalTo(array2)));
    }

    @Test
    public void serialization1() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(buffer);
        ArrayDouble array = new ArrayDouble(new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        stream.writeObject(array);
        ObjectInputStream inStream = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        ArrayDouble read = (ArrayDouble) inStream.readObject();
        assertThat(read, not(sameInstance(array)));
        assertThat(read, equalTo(array));
    }
    
    @Test
    public void setAll1() {
        Random rand = new Random(0);
        double[] array = rand.doubles(100).toArray();
        double[] otherArray = new double[100];
        ArrayDouble list = new ArrayDouble(array);
        ArrayDouble otherList = new ArrayDouble(otherArray, false);
        otherList.setAll(0, list);
        assertThat(array, equalTo(otherArray));
    }
    
    @Test
    public void setAll2() {
        double[] array = new double[50];
        Arrays.fill(array, 0.1);
        double[] otherArray = new double[100];
        ArrayDouble list = new ArrayDouble(array);
        ArrayDouble otherList = new ArrayDouble(otherArray, false);
        otherList.setAll(25, list);
        for (int i = 0; i < otherList.size(); i++) {
            if (i < 25 || i >= 75) {
                assertThat(otherList.getDouble(i), equalTo(0.0));
            } else {
                assertThat(otherList.getDouble(i), equalTo(0.1));
            }
        }
    }

    @Test
    public void subList1() {
        ArrayDouble array = new ArrayDouble(new double[] {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0});
        ArrayDouble subList = array.subList(3, 5);
        assertThat(subList.toArray(new double[subList.size()]), equalTo(new double[]{3.0, 4.0}));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void subList2() {
        ArrayDouble array = new ArrayDouble(new double[] {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0});
        ListDouble subList = array.subList(3, 11);
    }

    @Test
    public void subList3() {
        ArrayDouble array = new ArrayDouble(new double[] {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0});
        ArrayDouble subList = array.subList(1, 9);
        assertThat(subList.toArray(new double[subList.size()]), equalTo(new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0}));
        ArrayDouble subSubList = subList.subList(1, 7);
        assertThat(subSubList.toArray(new double[subList.size()]), equalTo(new double[]{2.0, 3.0, 4.0, 5.0, 6.0, 7.0}));
    }
}
