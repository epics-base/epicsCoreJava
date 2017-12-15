/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
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
}
