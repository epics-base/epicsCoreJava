/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public abstract class FeatureTestListNumber extends FeatureTestCollectionNumber {

    @Override
    abstract public ListNumber createConstantCollection();
    
    abstract public ListNumber createRampCollection();
    
    abstract public ListNumber createModifiableCollection();
    
    @Test
    public void getXxx() {
        testList(createConstantCollection());
    }
    
    @Test
    public void equals() {
        ListNumber a = createRampCollection();
        ListNumber b = createRampCollection();
        assertThat(a, not(sameInstance(b)));
        assertThat(a, equalTo(b));
        assertThat(b, equalTo(a));
    }
    
    @Test
    public void equalsSame() {
        ListNumber a = createRampCollection();
        assertThat(a, equalTo(a));
    }
    
    @Test
    public void equalsNull() {
        ListNumber a = createRampCollection();
        assertThat(a, not(equalTo(null)));
    }
    
    @Test
    public void notEquals() {
        ListNumber a = createConstantCollection();
        ListNumber b = createRampCollection();
        assertThat(a, not(equalTo(b)));
        assertThat(b, not(equalTo(a)));
    }
    
    @Test
    public void hashcodeConsistency() {
        ListNumber a = createRampCollection();
        ListNumber b = createRampCollection();
        assertThat(a.hashCode(), equalTo(b.hashCode()));
        assertThat(b.hashCode(), equalTo(a.hashCode()));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void defaultSetAll() {
        createRampCollection().setAll(0, new ArrayDouble(0.0));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void defaultSetDouble() {
        createRampCollection().setDouble(0, 0.0);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void defaultSetFloat() {
        createRampCollection().setFloat(0, 0.0F);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void defaultSetLong() {
        createRampCollection().setLong(0, 0L);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void defaultSetInt() {
        createRampCollection().setInt(0, 0);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void defaultSetShort() {
        createRampCollection().setShort(0, (short) 0);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void defaultSetByte() {
        createRampCollection().setByte(0, (byte) 0);
    }
    
    @Test
    public void setAllDouble() {
        ListNumber list = createModifiableCollection();
        ListNumber data = new ArrayDouble(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0);
        list.setAll(0, data);
        testRamp(list);
    }
    
    @Test
    public void setAllFloat() {
        ListNumber list = createModifiableCollection();
        ListNumber data = new ArrayFloat(0.0F, 1.0F, 2.0F, 3.0F, 4.0F, 5.0F, 6.0F, 7.0F, 8.0F, 9.0F);
        list.setAll(0, data);
        testRamp(list);
    }
    
    @Test
    public void setAllLong() {
        ListNumber list = createModifiableCollection();
        ListNumber data = new ArrayLong(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L);
        list.setAll(0, data);
        testRamp(list);
    }
    
    @Test
    public void setAllInt() {
        ListNumber list = createModifiableCollection();
        ListNumber data = new ArrayInt(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        list.setAll(0, data);
        testRamp(list);
    }
    
    @Test
    public void setAllShort() {
        ListNumber list = createModifiableCollection();
        ListNumber data = new ArrayShort(new short[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        list.setAll(0, data);
        testRamp(list);
    }
    
    @Test
    public void setAllByte() {
        ListNumber list = createModifiableCollection();
        ListNumber data = new ArrayByte(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        list.setAll(0, data);
        testRamp(list);
    }
    
    @Test
    public void setAllDouble2() {
        ListNumber list = createModifiableCollection();
        ListNumber data = new ArrayDouble(4.0, 5.0);
        list.setAll(4, data);
        testMiddleInsert(list);
    }
    
    @Test
    public void setAllFloat2() {
        ListNumber list = createModifiableCollection();
        ListNumber data = new ArrayFloat(4.0F, 5.0F);
        list.setAll(4, data);
        testMiddleInsert(list);
    }
    
    @Test
    public void setAllLong2() {
        ListNumber list = createModifiableCollection();
        ListNumber data = new ArrayLong(4L, 5L);
        list.setAll(4, data);
        testMiddleInsert(list);
    }
    
    @Test
    public void setAllInt2() {
        ListNumber list = createModifiableCollection();
        ListNumber data = new ArrayInt(4, 5);
        list.setAll(4, data);
        testMiddleInsert(list);
    }
    
    @Test
    public void setAllShort2() {
        ListNumber list = createModifiableCollection();
        ListNumber data = new ArrayShort(new short[] {4, 5});
        list.setAll(4, data);
        testMiddleInsert(list);
    }
    
    @Test
    public void setAllByte2() {
        ListNumber list = createModifiableCollection();
        ListNumber data = new ArrayByte(new byte[] {4, 5});
        list.setAll(4, data);
        testMiddleInsert(list);
    }
    
    @Test
    public void setDouble() {
        ListNumber list = createModifiableCollection();
        for (int i = 0; i < list.size(); i++) {
            list.setDouble(i, 1.0);
        }
        testList(list);
        testIterationForAllTypes(list);
    }
    
    @Test
    public void setFloat() {
        ListNumber list = createModifiableCollection();
        for (int i = 0; i < list.size(); i++) {
            list.setFloat(i, 1.0F);
        }
        testList(list);
        testIterationForAllTypes(list);
    }
    
    @Test
    public void setLong() {
        ListNumber list = createModifiableCollection();
        for (int i = 0; i < list.size(); i++) {
            list.setLong(i, 1L);
        }
        testList(list);
        testIterationForAllTypes(list);
    }
    
    @Test
    public void setInt() {
        ListNumber list = createModifiableCollection();
        for (int i = 0; i < list.size(); i++) {
            list.setInt(i, 1);
        }
        testList(list);
        testIterationForAllTypes(list);
    }
    
    @Test
    public void setShort() {
        ListNumber list = createModifiableCollection();
        for (int i = 0; i < list.size(); i++) {
            list.setShort(i, (short) 1);
        }
        testList(list);
        testIterationForAllTypes(list);
    }
    
    @Test
    public void setByte() {
        ListNumber list = createModifiableCollection();
        for (int i = 0; i < list.size(); i++) {
            list.setByte(i, (byte) 1);
        }
        testList(list);
        testIterationForAllTypes(list);
    }

    public static void testList(ListNumber coll) {
        assertEquals(10, coll.size());
        for (int i = 0; i < coll.size(); i++) {
            assertThat(coll.getDouble(i), equalTo((double) 1));
            assertThat(coll.getFloat(i), equalTo((float) 1));
            assertThat(coll.getLong(i), equalTo((long) 1));
            assertThat(coll.getInt(i), equalTo((int) 1));
            assertThat(coll.getShort(i), equalTo((short) 1));
            assertThat(coll.getByte(i), equalTo((byte) 1));
        }
    }

    public static void testRamp(ListNumber coll) {
        assertEquals(10, coll.size());
        for (int i = 0; i < coll.size(); i++) {
            assertThat(coll.getDouble(i), equalTo((double) i));
            assertThat(coll.getFloat(i), equalTo((float) i));
            assertThat(coll.getLong(i), equalTo((long) i));
            assertThat(coll.getInt(i), equalTo((int) i));
            assertThat(coll.getShort(i), equalTo((short) i));
            assertThat(coll.getByte(i), equalTo((byte) i));
        }
    }

    public static void testMiddleInsert(ListNumber coll) {
        assertEquals(10, coll.size());
        for (int i = 0; i < coll.size(); i++) {
            if (i >= 4 && i < 6) {
                assertThat(coll.getDouble(i), equalTo((double) i));
                assertThat(coll.getFloat(i), equalTo((float) i));
                assertThat(coll.getLong(i), equalTo((long) i));
                assertThat(coll.getInt(i), equalTo((int) i));
                assertThat(coll.getShort(i), equalTo((short) i));
                assertThat(coll.getByte(i), equalTo((byte) i));
            } else {
                assertThat(coll.getDouble(i), equalTo((double) 0));
                assertThat(coll.getFloat(i), equalTo((float) 0));
                assertThat(coll.getLong(i), equalTo((long) 0));
                assertThat(coll.getInt(i), equalTo((int) 0));
                assertThat(coll.getShort(i), equalTo((short) 0));
                assertThat(coll.getByte(i), equalTo((byte) 0));
            }
        }
    }
    
}
