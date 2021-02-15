/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.number;

import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class ULongTest {

    public ULongTest() {
    }

    @Test
    public void new1() {
        ULong value = new ULong(0);
        assertThat(value.bigIntegerValue(), equalTo(BigInteger.ZERO));
        assertThat(value.doubleValue(), equalTo(0.0));
        assertThat(value.floatValue(), equalTo(0.0f));
        assertThat(value.longValue(), equalTo(0L));
        assertThat(value.intValue(), equalTo(0));
    }

    @Test
    public void new2() {
        ULong value = new ULong(-1);
        assertThat(value.bigIntegerValue(), equalTo(new BigInteger("18446744073709551615")));
        assertThat(value.doubleValue(), equalTo(18446744073709551615.0));
        assertThat(value.floatValue(), equalTo(18446744073709551615.0f));
        assertThat(value.longValue(), equalTo(-1L));
        assertThat(value.intValue(), equalTo(-1));
    }

    @Test
    public void equals1() {
        ULong value1 = new ULong(-1);
        ULong value2 = new ULong(-1);
        ULong value3 = new ULong(0);
        assertThat(value1, equalTo(value2));
        assertThat(value1, not(equalTo(value3)));
        assertThat(value1, not(equalTo(null)));
        assertThat(value1, not(equalTo(new Object())));
    }

    @Test
    public void hashCode1() {
        ULong value1 = new ULong(-1);
        ULong value2 = new ULong(-1);
        ULong value3 = new ULong(10);
        assertThat(value1.hashCode(), equalTo(value2.hashCode()));
        assertThat(value1.hashCode(), not(equalTo(value3.hashCode())));
    }

    @Test
    public void toString1() {
        ULong value1 = new ULong(0);
        ULong value2 = new ULong(-1);
        assertThat(value1.toString(), equalTo("0"));
        assertThat(value2.toString(), equalTo("18446744073709551615"));
    }
}
