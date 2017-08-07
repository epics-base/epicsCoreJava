/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class ListNumberTestBase<T extends ListNumber> extends CollectionNumberTestBase<T> {

    public T incrementCollection;
    public T referenceIncrementCollection;
    
    public ListNumberTestBase(T collection, T incrementCollection, T referenceIncrementCollection) {
        super(collection);
        this.incrementCollection = incrementCollection;
        this.referenceIncrementCollection = referenceIncrementCollection;
    }
    
    @Test
    public void getXxx() {
        testList(collection);
    }
    
    @Test
    public void equals() {
        assertThat(incrementCollection, equalTo(referenceIncrementCollection));
        assertThat(referenceIncrementCollection, equalTo(incrementCollection));
    }
    
    @Test
    public void notEquals() {
        assertThat(incrementCollection, not(equalTo(collection)));
        assertThat(collection, not(equalTo(incrementCollection)));
    }
    
    @Test
    public void hashcodeConsistency() {
        assertThat(incrementCollection.hashCode(), equalTo(referenceIncrementCollection.hashCode()));
        assertThat(referenceIncrementCollection.hashCode(), equalTo(incrementCollection.hashCode()));
    }

    public static void testList(ListNumber coll) {
        assertEquals(10, coll.size());
        for (int i = 0; i < coll.size(); i++) {
            assertEquals(1.0, coll.getDouble(i), 0.00001);
            assertEquals((float) 1.0, coll.getFloat(i), 0.00001);
            assertEquals(1L, coll.getLong(i));
            assertEquals(1, coll.getInt(i));
            assertEquals((short) 1, coll.getShort(i));
            assertEquals((byte) 1, coll.getByte(i));
        }
    }
    
}
