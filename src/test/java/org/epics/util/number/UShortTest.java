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
public class UShortTest {

    public UShortTest() {
    }

    @Test
    public void new1() {
        UShort value = new UShort((byte) 0);
        assertThat(value.doubleValue(), equalTo(0.0));
        assertThat(value.floatValue(), equalTo(0.0f));
        assertThat(value.longValue(), equalTo(0L));
        assertThat(value.intValue(), equalTo(0));
    }

    @Test
    public void new2() {
        UShort value = new UShort((byte) -1);
        assertThat(value.doubleValue(), equalTo(65535.0));
        assertThat(value.floatValue(), equalTo(65535.0f));
        assertThat(value.longValue(), equalTo(65535L));
        assertThat(value.intValue(), equalTo(65535));
    }
}
