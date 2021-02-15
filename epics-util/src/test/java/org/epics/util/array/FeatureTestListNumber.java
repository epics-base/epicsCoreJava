/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.epics.util.array.CollectionNumbers.*;

public abstract class FeatureTestListNumber extends FeatureTestCollectionNumber {

    @Override
    abstract public ListNumber createConstantCollection();

    abstract public ListNumber createRampCollection();

    abstract public ListNumber createModifiableCollection();

    abstract public ListNumber createEmpty();

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

    @Test
    public void equalsDouble() {
        ListNumber list = createRampCollection();
        ListNumber doubleList = unmodifiableListDouble(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        if (list instanceof ListDouble) {
            assertThat(createRampCollection(), equalTo(doubleList));
        } else {
            assertThat(createRampCollection(), not(equalTo(doubleList)));
        }
    }

    @Test
    public void equalsFloat() {
        ListNumber list = createRampCollection();
        ListNumber floatList = unmodifiableListFloat(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        if (list instanceof ListFloat) {
            assertThat(createRampCollection(), equalTo(floatList));
        } else {
            assertThat(createRampCollection(), not(equalTo(floatList)));
        }
    }

    @Test
    public void equalsLong() {
        ListNumber list = createRampCollection();
        ListNumber longList = unmodifiableListLong(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        if (list instanceof ListLong) {
            assertThat(createRampCollection(), equalTo(longList));
        } else {
            assertThat(createRampCollection(), not(equalTo(longList)));
        }
    }

    @Test
    public void equalsInt() {
        ListNumber list = createRampCollection();
        ListNumber intList = unmodifiableListInt(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        if (list instanceof ListInteger) {
            assertThat(createRampCollection(), equalTo(intList));
        } else {
            assertThat(createRampCollection(), not(equalTo(intList)));
        }
    }

    @Test
    public void equalsShort() {
        ListNumber list = createRampCollection();
        ListNumber shortList = unmodifiableListShort(new short[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        if (list instanceof ListShort) {
            assertThat(createRampCollection(), equalTo(shortList));
        } else {
            assertThat(createRampCollection(), not(equalTo(shortList)));
        }
    }

    @Test
    public void equalsByte() {
        ListNumber list = createRampCollection();
        ListNumber byteList = unmodifiableListByte(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        if (list instanceof ListByte) {
            assertThat(createRampCollection(), equalTo(byteList));
        } else {
            assertThat(createRampCollection(), not(equalTo(byteList)));
        }
    }

    @Test
    public void toString1() {
        ListNumber list = createRampCollection();
        if (list instanceof ListDouble || list instanceof ListFloat) {
            assertThat(list.toString(), equalTo("[0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0]"));
        } else {
            assertThat(list.toString(), equalTo("[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]"));
        }
    }

    @Test
    public void toString2() {
        ListNumber list = createEmpty();
        assertThat(list.toString(), equalTo("[]"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void defaultSetAll() {
        createRampCollection().setAll(0, unmodifiableListDouble(0.0));
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
        ListNumber data = toList(new double[] {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0});
        list.setAll(0, data);
        testRamp(list);
    }

    @Test
    public void setAllFloat() {
        ListNumber list = createModifiableCollection();
        ListNumber data = unmodifiableListFloat(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        list.setAll(0, data);
        testRamp(list);
    }

    @Test
    public void setAllLong() {
        ListNumber list = createModifiableCollection();
        ListNumber data = unmodifiableListLong(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        list.setAll(0, data);
        testRamp(list);
    }

    @Test
    public void setAllInt() {
        ListNumber list = createModifiableCollection();
        ListNumber data = unmodifiableListInt(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        list.setAll(0, data);
        testRamp(list);
    }

    @Test
    public void setAllShort() {
        ListNumber list = createModifiableCollection();
        ListNumber data = unmodifiableListShort(new short[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        list.setAll(0, data);
        testRamp(list);
    }

    @Test
    public void setAllByte() {
        ListNumber list = createModifiableCollection();
        ListNumber data = unmodifiableListByte(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        list.setAll(0, data);
        testRamp(list);
    }

    @Test
    public void setAllDouble2() {
        ListNumber list = createModifiableCollection();
        ListNumber data = unmodifiableListDouble(4, 5);
        list.setAll(4, data);
        testMiddleInsert(list);
    }

    @Test
    public void setAllFloat2() {
        ListNumber list = createModifiableCollection();
        ListNumber data = unmodifiableListFloat(4, 5);
        list.setAll(4, data);
        testMiddleInsert(list);
    }

    @Test
    public void setAllLong2() {
        ListNumber list = createModifiableCollection();
        ListNumber data = unmodifiableListLong(4, 5);
        list.setAll(4, data);
        testMiddleInsert(list);
    }

    @Test
    public void setAllInt2() {
        ListNumber list = createModifiableCollection();
        ListNumber data = unmodifiableListInt(4, 5);
        list.setAll(4, data);
        testMiddleInsert(list);
    }

    @Test
    public void setAllShort2() {
        ListNumber list = createModifiableCollection();
        ListNumber data = unmodifiableListShort(new short[] {4, 5});
        list.setAll(4, data);
        testMiddleInsert(list);
    }

    @Test
    public void setAllByte2() {
        ListNumber list = createModifiableCollection();
        ListNumber data = unmodifiableListByte(new byte[] {4, 5});
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

    @Test
    public void subList1() {
        ListNumber array = createRampCollection();
        ListNumber subList = array.subList(3, 5);
        assertThat(subList.toArray(new double[subList.size()]), equalTo(new double[]{3.0, 4.0}));
        assertThat(subList.toArray(new float[subList.size()]), equalTo(new float[]{3.0F, 4.0F}));
        assertThat(subList.toArray(new long[subList.size()]), equalTo(new long[]{3, 4}));
        assertThat(subList.toArray(new int[subList.size()]), equalTo(new int[]{3, 4}));
        assertThat(subList.toArray(new short[subList.size()]), equalTo(new short[]{3, 4}));
        assertThat(subList.toArray(new byte[subList.size()]), equalTo(new byte[]{3, 4}));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void subList2() {
        ListNumber array = createRampCollection();
        ListNumber subList = array.subList(3, 11);
    }

    @Test
    public void subList3() {
        ListNumber array = createRampCollection();
        ListNumber subList = array.subList(1, 9);
        assertThat(subList.toArray(new double[subList.size()]), equalTo(new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0}));
        ListNumber subSubList = subList.subList(1, 7);
        assertThat(subSubList.toArray(new double[subSubList.size()]), equalTo(new double[]{2.0, 3.0, 4.0, 5.0, 6.0, 7.0}));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void subListAccessOutOfBounds1() {
        ListNumber subList = createRampCollection().subList(2, 8);
        subList.getDouble(-1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void subListAccessOutOfBounds2() {
        ListNumber subList = createRampCollection().subList(2, 8);
        subList.getDouble(subList.size());
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void subListWriteOutOfBounds1() {
        ListNumber subList = createModifiableCollection().subList(2, 8);
        subList.setDouble(-1, 0.0);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void subListWriteOutOfBounds2() {
        ListNumber subList = createModifiableCollection().subList(2, 8);
        subList.setDouble(subList.size(), 0.0);
    }

    @Test
    public void subListEquals() {
        ListNumber ramp = createRampCollection();
        ListNumber list = createModifiableCollection();
        list.setDouble(0, 0);
        list.setDouble(1, 1);
        list.setDouble(2, 2);
        assertThat(list.subList(0, 3), equalTo(ramp.subList(0, 3)));
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
