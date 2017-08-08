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
