/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import org.epics.util.array.IteratorFloat;
import org.epics.util.array.CollectionShort;
import org.epics.util.array.CollectionInt;
import org.epics.util.array.IteratorInt;
import org.epics.util.array.CollectionFloat;
import org.epics.util.array.CollectionNumber;
import org.epics.util.array.IteratorShort;
import org.epics.util.array.CollectionLong;
import org.epics.util.array.CollectionDouble;
import org.epics.util.array.CollectionByte;
import org.epics.util.array.IteratorLong;
import org.epics.util.array.IteratorDouble;
import org.epics.util.array.IteratorByte;
import org.epics.util.array.IteratorNumber;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class CollectionTest {

    @Test
    public void testCollectionLong() {
        CollectionLong coll = new CollectionLong() {

            @Override
            public IteratorLong iterator() {
                return new IteratorLong() {

                    int n=0;

                    @Override
                    public boolean hasNext() {
                        return n < 10;
                    }

                    @Override
                    public long nextLong() {
                        n++;
                        return 1L;
                    }
                };
            }

            @Override
            public int size() {
                return 10;
            }
        };
        testIterationForAllTypes(coll);
    }

    @Test
    public void testCollectionInt() {
        CollectionInt coll = new CollectionInt() {

            @Override
            public IteratorInt iterator() {
                return new IteratorInt() {

                    int n=0;

                    @Override
                    public boolean hasNext() {
                        return n < 10;
                    }

                    @Override
                    public int nextInt() {
                        n++;
                        return 1;
                    }
                };
            }

            @Override
            public int size() {
                return 10;
            }
        };
        testIterationForAllTypes(coll);
    }

    @Test
    public void testCollectionShort() {
        CollectionShort coll = new CollectionShort() {

            @Override
            public IteratorShort iterator() {
                return new IteratorShort() {

                    int n=0;

                    @Override
                    public boolean hasNext() {
                        return n < 10;
                    }

                    @Override
                    public short nextShort() {
                        n++;
                        return (short) 1;
                    }
                };
            }

            @Override
            public int size() {
                return 10;
            }
        };
        testIterationForAllTypes(coll);
    }

    @Test
    public void testCollectionByte() {
        CollectionByte coll = new CollectionByte() {

            @Override
            public IteratorByte iterator() {
                return new IteratorByte() {

                    int n=0;

                    @Override
                    public boolean hasNext() {
                        return n < 10;
                    }

                    @Override
                    public byte nextByte() {
                        n++;
                        return (byte) 1;
                    }
                };
            }

            @Override
            public int size() {
                return 10;
            }
        };
        testIterationForAllTypes(coll);
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
