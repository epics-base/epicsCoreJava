/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import org.epics.util.array.ArrayLong;
import org.epics.util.array.ListLong;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.epics.util.array.ListTest.testList;
import static org.epics.util.array.CollectionNumberTestBase.testIterationForAllTypes;

/**
 *
 * @author carcassi
 */
public class ListLongTest {

    public ListLongTest() {
    }

    @Test
    public void list1() {
        ListLong coll = new ListLong() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public long getLong(int index) {
                return 1L;
            }
        };
        testIterationForAllTypes(coll);
        testList(coll);
    }

    @Test
    public void equals1() {
        ListLong coll = new ListLong() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public long getLong(int index) {
                return index;
            }
        };
        ListLong other = new ArrayLong(new long[] {0,1,2,3,4,5,6,7,8,9});
        assertThat(coll, equalTo(other));
        assertThat(other, equalTo(coll));
    }

    @Test
    public void hashcode1() {
        ListLong coll = new ListLong() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public long getLong(int index) {
                return index;
            }
        };
        ListLong other = new ArrayLong(new long[] {0,1,2,3,4,5,6,7,8,9});
        assertThat(coll.hashCode(), equalTo(other.hashCode()));
        assertThat(coll.hashCode(), equalTo(Arrays.hashCode(new long[] {0,1,2,3,4,5,6,7,8,9})));
    }

}
