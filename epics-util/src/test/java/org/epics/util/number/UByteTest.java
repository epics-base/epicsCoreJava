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
public class UByteTest {

    public UByteTest() {
    }

    @Test
    public void new1() {
        UByte value = new UByte((byte) 0);
        assertThat(value.doubleValue(), equalTo(0.0));
        assertThat(value.floatValue(), equalTo(0.0f));
        assertThat(value.longValue(), equalTo(0L));
        assertThat(value.intValue(), equalTo(0));
    }

    @Test
    public void new2() {
        UByte value = new UByte((byte) -1);
        assertThat(value.doubleValue(), equalTo(255.0));
        assertThat(value.floatValue(), equalTo(255.0f));
        assertThat(value.longValue(), equalTo(255L));
        assertThat(value.intValue(), equalTo(255));
    }

    @Test
    public void equals1() {
        UByte value1 = new UByte((byte) -1);
        UByte value2 = new UByte((byte) -1);
        UByte value3 = new UByte((byte) 0);
        assertThat(value1, equalTo(value2));
        assertThat(value1, not(equalTo(value3)));
        assertThat(value1, not(equalTo(null)));
        assertThat(value1, not(equalTo(new Object())));
    }

    @Test
    public void hashCode1() {
        UByte value1 = new UByte((byte) -1);
        UByte value2 = new UByte((byte) -1);
        UByte value3 = new UByte((byte) 0);
        assertThat(value1.hashCode(), equalTo(value2.hashCode()));
        assertThat(value1.hashCode(), not(equalTo(value3.hashCode())));
    }

    @Test
    public void toString1() {
        UByte value1 = new UByte((byte) 0);
        UByte value2 = new UByte((byte) -1);
        assertThat(value1.toString(), equalTo("0"));
        assertThat(value2.toString(), equalTo("255"));
    }
}
