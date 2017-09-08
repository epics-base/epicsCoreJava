/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public abstract class FeatureTestListDouble extends FeatureTestListNumber {

    @Override
    abstract public ListDouble createConstantCollection();
    
    @Override
    abstract public ListDouble createRampCollection();
    
    @Test
    public void equalsDouble() {
        assertThat(createRampCollection(), equalTo(ListNumbers.toList(new double[] {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0})));
    }
    
    @Test
    public void equalsFloat() {
        assertThat(createRampCollection(), not(equalTo(new ArrayFloat(0.0F, 1.0F, 2.0F, 3.0F, 4.0F, 5.0F, 6.0F, 7.0F, 8.0F, 9.0F))));
    }
    
    @Test
    public void equalsLong() {
        assertThat(createRampCollection(), not(equalTo(new ArrayLong(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L))));
    }
    
    @Test
    public void equalsInt() {
        assertThat(createRampCollection(), not(equalTo(new ArrayInt(0, 1, 2, 3, 4, 5, 6, 7, 8, 9))));
    }
    
    @Test
    public void equalsShort() {
        assertThat(createRampCollection(), not(equalTo(new ArrayShort(new short[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9}))));
    }
    
    @Test
    public void equalsByte() {
        assertThat(createRampCollection(), not(equalTo(new ArrayByte(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9}))));
    }

    @Test
    public void toString1() {
        assertThat(createRampCollection().toString(), equalTo("[0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0]"));
    }
}
