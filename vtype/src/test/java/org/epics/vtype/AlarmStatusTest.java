/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import java.util.Arrays;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class AlarmStatusTest {

    @Test
    public void labels1() {
        assertThat(AlarmStatus.labels(), equalTo(Arrays.asList("NONE", "DEVICE", "DRIVER", "RECORD", "DB", "CONF", "UNDEFINED", "CLIENT")));
    }
}
