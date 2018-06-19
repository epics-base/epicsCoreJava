/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
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
public class AlarmSeverityTest {

    @Test
    public void labels1() {
        assertThat(AlarmSeverity.labels(), equalTo(Arrays.asList("NONE", "MINOR", "MAJOR", "INVALID", "UNDEFINED")));
    }
}
