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
public class UShortTest {

    public UShortTest() {
    }

    @Test
    public void new1() {
        UShort value = new UShort((short) 0);
        assertThat(value.doubleValue(), equalTo(0.0));
        assertThat(value.floatValue(), equalTo(0.0f));
        assertThat(value.longValue(), equalTo(0L));
        assertThat(value.intValue(), equalTo(0));
    }

    @Test
    public void new2() {
        UShort value = new UShort((short) -1);
        assertThat(value.doubleValue(), equalTo(65535.0));
        assertThat(value.floatValue(), equalTo(65535.0f));
        assertThat(value.longValue(), equalTo(65535L));
        assertThat(value.intValue(), equalTo(65535));
    }

    @Test
    public void equals1() {
        UShort value1 = new UShort((short) -1);
        UShort value2 = new UShort((short) -1);
        UShort value3 = new UShort((short) 0);
        assertThat(value1, equalTo(value2));
        assertThat(value1, not(equalTo(value3)));
        assertThat(value1, not(equalTo(null)));
        assertThat(value1, not(equalTo(new Object())));
    }

    @Test
    public void hashCode1() {
        UShort value1 = new UShort((short) -1);
        UShort value2 = new UShort((short) -1);
        UShort value3 = new UShort((short) 0);
        assertThat(value1.hashCode(), equalTo(value2.hashCode()));
        assertThat(value1.hashCode(), not(equalTo(value3.hashCode())));
    }

    @Test
    public void toString1() {
        UShort value1 = new UShort((short) 0);
        UShort value2 = new UShort((short) -1);
        assertThat(value1.toString(), equalTo("0"));
        assertThat(value2.toString(), equalTo("65535"));
    }
}
