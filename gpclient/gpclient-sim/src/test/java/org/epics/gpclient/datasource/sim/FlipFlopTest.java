/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.util.array.ArrayBoolean;
import org.epics.util.array.ListBoolean;
import org.epics.vtype.Alarm;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author carcassi
 */
public class FlipFlopTest extends FeatureTestSimFunction {

    @Test
    public void values() {
        ListBoolean expectedValues = ArrayBoolean.of(true, false, true, false, true, false, true, false, true, false);
        List<Alarm> expectedAlarms = Arrays.asList(Alarm.none(), Alarm.none(), Alarm.none(), Alarm.none(), Alarm.none(), Alarm.none(), Alarm.none(), Alarm.none(), Alarm.none(), Alarm.none());

        Flipflop flipFlop = new Flipflop();

        testVBooleanSimFunction(flipFlop, expectedValues, expectedAlarms);
    }

}
