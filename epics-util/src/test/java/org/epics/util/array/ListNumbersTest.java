/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.hamcrest.Matchers;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.epics.util.array.ListNumbers.*;
import static org.epics.util.array.CollectionNumbers.*;

/**
 *
 * @author carcassi
 */
public class ListNumbersTest {

    @Test
    public void sortedView1() {
        ArrayDouble values = unmodifiableListDouble(5,3,1,4,2,0);
        SortedListView sortedView = ListNumbers.sortedView(values);
        assertThat(values, equalTo(unmodifiableListDouble(5,3,1,4,2,0)));
        assertThat(sortedView, equalTo((ListNumber) unmodifiableListDouble(0,1,2,3,4,5)));
        assertThat(sortedView.getIndexes(), Matchers.<ListInteger>equalTo(unmodifiableListInt(5,2,4,1,3,0)));
    }

    @Test
    public void sortedView2() {
        ArrayDouble values = unmodifiableListDouble(5,3,1,4,2,0);
        ArrayInteger indexes = unmodifiableListInt(0,3,1,4,2,5);
        SortedListView sortedView = ListNumbers.sortedView(values, indexes);
        assertThat(values, equalTo(unmodifiableListDouble(5,3,1,4,2,0)));
        assertThat(sortedView, equalTo((ListNumber) unmodifiableListDouble(5,4,3,2,1,0)));
        assertThat(sortedView.getIndexes(), Matchers.<ListInteger>equalTo(unmodifiableListInt(0,3,1,4,2,5)));
    }

    @Test
    public void sortedView3() {
        ArrayDouble values = unmodifiableListDouble(-1.7178013239620846, 0.5200744839822301, 0.638091980352644, 0.093683130487196, -1.2967630810250952, 0.7040257444802407, -0.4166241363846508, 2.9610862677876244, 0.03636268292097817, -0.35530274977371445);
        SortedListView sortedView = ListNumbers.sortedView(values);
        assertThat(sortedView, equalTo((ListNumber) unmodifiableListDouble(-1.7178013239620846, -1.2967630810250952, -0.4166241363846508, -0.35530274977371445, 0.03636268292097817, 0.093683130487196, 0.5200744839822301, 0.638091980352644, 0.7040257444802407, 2.9610862677876244)));
    }

    @Test
    public void sortedView4() {
        ArrayDouble values = unmodifiableListDouble(0,1,2,4,3,5);
        SortedListView sortedView = ListNumbers.sortedView(values);
        assertThat(values, equalTo(unmodifiableListDouble(0,1,2,4,3,5)));
        assertThat(sortedView, equalTo((ListNumber) unmodifiableListDouble(0,1,2,3,4,5)));
        assertThat(sortedView.getIndexes(), Matchers.<ListInteger>equalTo(unmodifiableListInt(0,1,2,4,3,5)));
    }

    @Test
    public void binarySearchValueOrLower1() {
        ListNumber values = unmodifiableListDouble(1,2,3,3,4,5,5,6,7,8,10);
        assertThat(ListNumbers.binarySearchValueOrLower(values, 1), equalTo(0));
        assertThat(ListNumbers.binarySearchValueOrLower(values, 10), equalTo(10));
        assertThat(ListNumbers.binarySearchValueOrLower(values, 2), equalTo(1));
        assertThat(ListNumbers.binarySearchValueOrLower(values, 3), equalTo(2));
        assertThat(ListNumbers.binarySearchValueOrLower(values, 5), equalTo(5));
        assertThat(ListNumbers.binarySearchValueOrLower(values, 9), equalTo(9));
        assertThat(ListNumbers.binarySearchValueOrLower(values, 2.5), equalTo(1));
        assertThat(ListNumbers.binarySearchValueOrLower(values, 0.5), equalTo(0));
        assertThat(ListNumbers.binarySearchValueOrLower(values, 10), equalTo(10));
    }

    @Test
    public void binarySearchValueOrLower2() {
        ListNumber values = unmodifiableListDouble(1,2,2,2,2,2,2,2,2,2,3);
        assertThat(ListNumbers.binarySearchValueOrLower(values, 2), equalTo(1));
   }

    @Test
    public void binarySearchValueOrHigher1() {
        ListNumber values = unmodifiableListDouble(1,2,3,3,4,5,5,6,7,8,10);
        assertThat(ListNumbers.binarySearchValueOrHigher(values, 1), equalTo(0));
        assertThat(ListNumbers.binarySearchValueOrHigher(values, 10), equalTo(10));
        assertThat(ListNumbers.binarySearchValueOrHigher(values, 2), equalTo(1));
        assertThat(ListNumbers.binarySearchValueOrHigher(values, 3), equalTo(3));
        assertThat(ListNumbers.binarySearchValueOrHigher(values, 5), equalTo(6));
        assertThat(ListNumbers.binarySearchValueOrHigher(values, 9), equalTo(10));
        assertThat(ListNumbers.binarySearchValueOrHigher(values, 2.5), equalTo(2));
    }

    @Test
    public void binarySearchValueOrHigher2() {
        ListNumber values = unmodifiableListDouble(1,2,2,2,2,2,2,2,2,2,3);
        assertThat(ListNumbers.binarySearchValueOrHigher(values, 2), equalTo(9));
   }

    @Test
    public void linearRange1() throws Exception {
        ListNumber list = ListNumbers.linearListFromRange(0, 1000, 101);
        assertThat(list.getDouble(0), equalTo(0.0));
        assertThat(list.getDouble(35), equalTo(350.0));
        assertThat(list.getDouble(50), equalTo(500.0));
        assertThat(list.getDouble(100), equalTo(1000.0));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void linearRange2() throws Exception {
        ListNumber list = ListNumbers.linearListFromRange(0, 1000, 100);
        list.getDouble(-1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void linearRange3() throws Exception {
        ListNumber list = ListNumbers.linearListFromRange(0, 1000, 100);
        list.getDouble(1000);
    }

    @Test(expected=IllegalArgumentException.class)
    public void linearRange4() throws Exception {
        ListNumber list = ListNumbers.linearListFromRange(0, 1000, 0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void linearRange5() throws Exception {
        ListNumber list = ListNumbers.linearListFromRange(0, 1000, -10);
    }

    @Test
    public void linearRange6() throws Exception {
        ListNumber list = ListNumbers.linearListFromRange(1000, 0, 101);
        assertThat(list.getDouble(0), equalTo(1000.0));
        assertThat(list.getDouble(35), equalTo(650.0));
        assertThat(list.getDouble(50), equalTo(500.0));
        assertThat(list.getDouble(100), equalTo(0.0));
    }

    @Test
    public void linearList1() throws Exception {
        ListNumber list = ListNumbers.linearList(0, 10, 101);
        assertThat(list.getDouble(0), equalTo(0.0));
        assertThat(list.getDouble(35), equalTo(350.0));
        assertThat(list.getDouble(50), equalTo(500.0));
        assertThat(list.getDouble(100), equalTo(1000.0));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void linearList2() throws Exception {
        ListNumber list = ListNumbers.linearList(0, 10, 101);
        list.getDouble(-1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void linearList3() throws Exception {
        ListNumber list = ListNumbers.linearList(0, 10, 101);
        list.getDouble(1000);
    }

    @Test(expected=IllegalArgumentException.class)
    public void linearList4() throws Exception {
        ListNumber list = ListNumbers.linearList(0, 10, 0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void linearList5() throws Exception {
        ListNumber list = ListNumbers.linearList(0, 10, -10);
    }

    @Test
    public void linearList6() throws Exception {
        ListNumber list = ListNumbers.linearList(1000, -10, 101);
        assertThat(list.getDouble(0), equalTo(1000.0));
        assertThat(list.getDouble(35), equalTo(650.0));
        assertThat(list.getDouble(50), equalTo(500.0));
        assertThat(list.getDouble(100), equalTo(0.0));
    }

    @Test
    public void isLinear1() {
        assertThat(isLinear(linearList(0, 0.1, 100000)), equalTo(true));
        assertThat(isLinear(linearListFromRange(0, 100, 10000)), equalTo(true));
        assertThat(isLinear(ListMath.add(linearList(0, 0.00001, 10000), 3.0)), equalTo(true));
        assertThat(isLinear(linearListFromRange(0, 100, 10000)), equalTo(true));
        assertThat(isLinear(unmodifiableListDouble(0,1,2,3,4,5)), equalTo(true));
        assertThat(isLinear(unmodifiableListDouble(0,1.00001,2,3,4,5)), equalTo(false));
    }

    @Test
    public void listView1() {
        ArrayDouble values = unmodifiableListDouble(5,3,1,4,2,0);
        ArrayInteger indexes = unmodifiableListInt(0,3,1,4,2,5);
        ListNumber sortedView = ListNumbers.listView(values, indexes);
        assertThat(sortedView, instanceOf(ListDouble.class));
        assertThat(values, equalTo(unmodifiableListDouble(5,3,1,4,2,0)));
        assertThat(sortedView, Matchers.<ListNumber>equalTo(unmodifiableListDouble(5,4,3,2,1,0)));
    }

    @Test
    public void listView2() {
        ArrayFloat values = unmodifiableListFloat(5, 3, 1, 4, 2, 0);
        ArrayInteger indexes = unmodifiableListInt(0, 3, 1, 4, 2, 5);
        ListNumber sortedView = ListNumbers.listView(values, indexes);
        assertThat(sortedView, instanceOf(ListFloat.class));
        assertThat(values, equalTo(unmodifiableListFloat(5, 3, 1, 4, 2, 0)));
        assertThat(sortedView, Matchers.<ListNumber>equalTo(unmodifiableListFloat(5, 4, 3, 2, 1, 0)));
    }

    @Test
    public void listView3() {
        ArrayLong values = unmodifiableListLong(5,3,1,4,2,0);
        ArrayInteger indexes = unmodifiableListInt(0,3,1,4,2,5);
        ListNumber sortedView = ListNumbers.listView(values, indexes);
        assertThat(sortedView, instanceOf(ListLong.class));
        assertThat(values, equalTo(unmodifiableListLong(5,3,1,4,2,0)));
        assertThat(sortedView, Matchers.<ListNumber>equalTo(unmodifiableListLong(5,4,3,2,1,0)));
    }

    @Test
    public void listView4() {
        ArrayInteger values = unmodifiableListInt(5,3,1,4,2,0);
        ArrayInteger indexes = unmodifiableListInt(0,3,1,4,2,5);
        ListNumber sortedView = ListNumbers.listView(values, indexes);
        assertThat(sortedView, instanceOf(ListInteger.class));
        assertThat(values, equalTo(unmodifiableListInt(5,3,1,4,2,0)));
        assertThat(sortedView, Matchers.<ListNumber>equalTo(unmodifiableListInt(5,4,3,2,1,0)));
    }

    @Test
    public void listView5() {
        ArrayShort values = unmodifiableListShort(new short[] {5,3,1,4,2,0});
        ArrayInteger indexes = unmodifiableListInt(0,3,1,4,2,5);
        ListNumber sortedView = ListNumbers.listView(values, indexes);
        assertThat(sortedView, instanceOf(ListShort.class));
        assertThat(values, equalTo(unmodifiableListShort(new short[] {5,3,1,4,2,0})));
        assertThat(sortedView, Matchers.<ListNumber>equalTo(unmodifiableListShort(new short[] {5,4,3,2,1,0})));
    }

    @Test
    public void listView6() {
        ArrayByte values = unmodifiableListByte(new byte[] {5,3,1,4,2,0});
        ArrayInteger indexes = unmodifiableListInt(0,3,1,4,2,5);
        ListNumber sortedView = ListNumbers.listView(values, indexes);
        assertThat(sortedView, instanceOf(ListByte.class));
        assertThat(values, equalTo(unmodifiableListByte(new byte[] {5,3,1,4,2,0})));
        assertThat(sortedView, Matchers.<ListNumber>equalTo(unmodifiableListByte(new byte[] {5,4,3,2,1,0})));
    }
}
