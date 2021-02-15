/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.number;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class UIntegerTest {

    public UIntegerTest() {
    }

    @Test
    public void new1() {
        UInteger value = new UInteger(0);
        assertThat(value.doubleValue(), equalTo(0.0));
        assertThat(value.floatValue(), equalTo(0.0f));
        assertThat(value.longValue(), equalTo(0L));
        assertThat(value.intValue(), equalTo(0));
    }

    @Test
    public void new2() {
        UInteger value = new UInteger(-1);
        assertThat(value.doubleValue(), equalTo(4294967295.0));
        assertThat(value.floatValue(), equalTo(4294967295.0f));
        assertThat(value.longValue(), equalTo(4294967295L));
        assertThat(value.intValue(), equalTo(-1));
    }

    @Test
    public void equals1() {
        UInteger value1 = new UInteger(-1);
        UInteger value2 = new UInteger(-1);
        UInteger value3 = new UInteger(0);
        assertThat(value1, equalTo(value2));
        assertThat(value1, not(equalTo(value3)));
        assertThat(value1, not(equalTo(null)));
        assertThat(value1, not(equalTo(new Object())));
    }

    @Test
    public void hashCode1() {
        UInteger value1 = new UInteger(-1);
        UInteger value2 = new UInteger(-1);
        UInteger value3 = new UInteger(0);
        assertThat(value1.hashCode(), equalTo(value2.hashCode()));
        assertThat(value1.hashCode(), not(equalTo(value3.hashCode())));
    }

    @Test
    public void toString1() {
        UInteger value1 = new UInteger(0);
        UInteger value2 = new UInteger(-1);
        assertThat(value1.toString(), equalTo("0"));
        assertThat(value2.toString(), equalTo("4294967295"));
    }
}
