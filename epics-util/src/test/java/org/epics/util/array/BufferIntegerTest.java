/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.epics.util.array.FeatureTestListNumber.testList;
import static org.epics.util.array.FeatureTestCollectionNumber.testIterationForAllTypes;

/**
 *
 * @author carcassi
 */
public class BufferIntegerTest {

    public BufferIntegerTest() {
    }

    @Test
    public void iteration1() {
        BufferInteger coll = new BufferInteger();
        for (int i = 0; i < 10; i++) {
            coll.addInt(1);
        }
        testIterationForAllTypes(coll);
        testList(coll);
    }

    @Test
    public void add1() {
        BufferInteger coll = new BufferInteger();
        for (int i = 0; i < 5; i++) {
            coll.addInt(1);
        }
        assertThat(coll.size(), equalTo(5));
        for (int i = 0; i < 5; i++) {
            coll.addInt(1);
        }
        assertThat(coll.size(), equalTo(10));
        for (int i = 0; i < 5; i++) {
            coll.addInt(1);
        }
        assertThat(coll.size(), equalTo(15));
    }

    @Test
    public void add2() {
        BufferInteger coll = new BufferInteger();
        for (int i = 0; i < 11; i++) {
            coll.addInt(i);
        }
        ListInteger reference = CollectionNumbers.unmodifiableListInt(0,1,2,3,4,5,6,7,8,9,10);
        assertThat(coll, equalTo(reference));
    }

    @Test
    public void add3() {
        BufferInteger coll = new BufferInteger();
        for (int i = 0; i < 5; i++) {
            coll.addInt(i);
        }
        ListInteger reference = CollectionNumbers.unmodifiableListInt(0,1,2,3,4);
        assertThat(coll, equalTo(reference));
    }

    @Test
    public void clear1() {
        BufferInteger coll = new BufferInteger();
        for (int i = 0; i < 5; i++) {
            coll.addInt(i);
        }
        coll.clear();
        assertThat(coll.size(), equalTo(0));
    }
}
