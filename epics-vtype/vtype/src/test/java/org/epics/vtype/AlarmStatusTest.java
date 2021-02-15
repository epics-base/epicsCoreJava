/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

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
