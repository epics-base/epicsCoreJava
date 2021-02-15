/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public abstract class FeatureTestCollectionNumber {

    abstract public CollectionNumber createConstantCollection();

    @Test
    public void iteration() {
        testIterationForAllTypes(createConstantCollection());
    }

    @Test
    public void toArray() {
        testToArrayForAllTypes(createConstantCollection());
    }

    @Test(expected=ArrayStoreException.class)
    public void toArrayInvalid() {
        createConstantCollection().toArray(new Object());
    }

    public static void testIterationForAllTypes(CollectionNumber coll) {
        assertEquals(10, coll.size());
        IteratorNumber iter = coll.iterator();
        while (iter.hasNext()) {
            assertEquals(1.0, iter.nextDouble(), 0.0001);
        }
        iter = coll.iterator();
        while (iter.hasNext()) {
            assertEquals((float) 1.0, iter.nextFloat(), 0.0001);
        }
        iter = coll.iterator();
        while (iter.hasNext()) {
            assertEquals(1L, iter.nextLong());
        }
        iter = coll.iterator();
        while (iter.hasNext()) {
            assertEquals(1, iter.nextInt());
        }
        iter = coll.iterator();
        while (iter.hasNext()) {
            assertEquals((short) 1, iter.nextShort());
        }
        iter = coll.iterator();
        while (iter.hasNext()) {
            assertEquals((byte) 1, iter.nextByte());
        }
    }

    public static void testToArrayForAllTypes(CollectionNumber coll) {
        assertEquals(10, coll.size());

        {
            // Double copies
            double[] shorter = new double[9];
            double[] correct = new double[10];
            double[] longer = new double[12];
            longer[11] = -12;

            double[] shorterCopy = coll.toArray(shorter);
            double[] correctCopy = coll.toArray(correct);
            double[] longerCopy = coll.toArray(longer);

            assertThat(shorterCopy, not(sameInstance(shorter)));
            assertThat(correctCopy, sameInstance(correct));
            assertThat(longerCopy, sameInstance(longer));
            assertThat(shorterCopy, equalTo(new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}));
            assertThat(correctCopy, equalTo(new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}));
            assertThat(longerCopy, equalTo(new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, -12.0}));
        }

        {
            // Float copies
            float[] shorter = new float[9];
            float[] correct = new float[10];
            float[] longer = new float[12];
            longer[11] = -12;

            float[] shorterCopy = coll.toArray(shorter);
            float[] correctCopy = coll.toArray(correct);
            float[] longerCopy = coll.toArray(longer);

            assertThat(shorterCopy, not(sameInstance(shorter)));
            assertThat(correctCopy, sameInstance(correct));
            assertThat(longerCopy, sameInstance(longer));
            assertThat(shorterCopy, equalTo(new float[] {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f}));
            assertThat(correctCopy, equalTo(new float[] {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f}));
            assertThat(longerCopy, equalTo(new float[] {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, -12.0f}));
        }

        {
            // Long copies
            long[] shorter = new long[9];
            long[] correct = new long[10];
            long[] longer = new long[12];
            longer[11] = -12;

            long[] shorterCopy = coll.toArray(shorter);
            long[] correctCopy = coll.toArray(correct);
            long[] longerCopy = coll.toArray(longer);

            assertThat(shorterCopy, not(sameInstance(shorter)));
            assertThat(correctCopy, sameInstance(correct));
            assertThat(longerCopy, sameInstance(longer));
            assertThat(shorterCopy, equalTo(new long[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}));
            assertThat(correctCopy, equalTo(new long[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}));
            assertThat(longerCopy, equalTo(new long[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, -12}));
        }

        {
            // Int copies
            int[] shorter = new int[9];
            int[] correct = new int[10];
            int[] longer = new int[12];
            longer[11] = -12;

            int[] shorterCopy = coll.toArray(shorter);
            int[] correctCopy = coll.toArray(correct);
            int[] longerCopy = coll.toArray(longer);

            assertThat(shorterCopy, not(sameInstance(shorter)));
            assertThat(correctCopy, sameInstance(correct));
            assertThat(longerCopy, sameInstance(longer));
            assertThat(shorterCopy, equalTo(new int[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}));
            assertThat(correctCopy, equalTo(new int[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}));
            assertThat(longerCopy, equalTo(new int[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, -12}));
        }

        {
            // Short copies
            short[] shorter = new short[9];
            short[] correct = new short[10];
            short[] longer = new short[12];
            longer[11] = -12;

            short[] shorterCopy = coll.toArray(shorter);
            short[] correctCopy = coll.toArray(correct);
            short[] longerCopy = coll.toArray(longer);

            assertThat(shorterCopy, not(sameInstance(shorter)));
            assertThat(correctCopy, sameInstance(correct));
            assertThat(longerCopy, sameInstance(longer));
            assertThat(shorterCopy, equalTo(new short[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}));
            assertThat(correctCopy, equalTo(new short[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}));
            assertThat(longerCopy, equalTo(new short[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, -12}));
        }

        {
            // Byte copies
            byte[] shorter = new byte[9];
            byte[] correct = new byte[10];
            byte[] longer = new byte[12];
            longer[11] = -12;

            byte[] shorterCopy = coll.toArray(shorter);
            byte[] correctCopy = coll.toArray(correct);
            byte[] longerCopy = coll.toArray(longer);

            assertThat(shorterCopy, not(sameInstance(shorter)));
            assertThat(correctCopy, sameInstance(correct));
            assertThat(longerCopy, sameInstance(longer));
            assertThat(shorterCopy, equalTo(new byte[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}));
            assertThat(correctCopy, equalTo(new byte[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}));
            assertThat(longerCopy, equalTo(new byte[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, -12}));
        }
    }

}
