/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import org.epics.util.array.ListFloat;
import org.epics.util.array.ArrayFloat;
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
public class ListFloatTest {

    public ListFloatTest() {
    }

    @Test
    public void list1() {
        ListFloat coll = new ListFloat() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public float getFloat(int index) {
                return 1.0F;
            }
        };
        testIterationForAllTypes(coll);
        testList(coll);
    }

    @Test
    public void equals1() {
        ListFloat coll = new ListFloat() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public float getFloat(int index) {
                return index;
            }
        };
        ListFloat other = new ArrayFloat(new float[] {0,1,2,3,4,5,6,7,8,9});
        assertThat(coll, equalTo(other));
        assertThat(other, equalTo(coll));
    }

    @Test
    public void hashcode1() {
        ListFloat coll = new ListFloat() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public float getFloat(int index) {
                return index;
            }
        };
        ListFloat other = new ArrayFloat(new float[] {0,1,2,3,4,5,6,7,8,9});
        assertThat(coll.hashCode(), equalTo(other.hashCode()));
        assertThat(coll.hashCode(), equalTo(Arrays.hashCode(new float[] {0,1,2,3,4,5,6,7,8,9})));
    }

}
