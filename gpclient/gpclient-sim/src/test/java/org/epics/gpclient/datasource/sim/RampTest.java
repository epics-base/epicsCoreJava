/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ListDouble;
import org.epics.util.stats.Range;
import org.epics.vtype.Alarm;
import org.epics.vtype.Display;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author carcassi
 */
public class RampTest extends FeatureTestSimFunction {

    @Test
    public void values1() {
        Display referenceDisplay = Display.of(Range.of(-10, 10), Range.of(-9, 9), Range.of(-8, 8), Range.undefined(), "", Display.defaultNumberFormat());
        ListDouble expectedValues = ArrayDouble.of(-10, -8, -6, -4, -2, 0, 2, 4, 6, 8, 10, -10);
        List<Alarm> expectedAlarms = Arrays.asList(Alarm.lolo(), Alarm.low(), Alarm.none(), Alarm.none(), Alarm.none(), Alarm.none(), Alarm.none(), Alarm.none(), Alarm.none(), Alarm.high(), Alarm.hihi(), Alarm.lolo());

        Ramp ramp = new Ramp(-10.0, 10.0, 2.0, 1.0);

        testVDoubleSimFunction(ramp, expectedValues, expectedAlarms, referenceDisplay);
    }

    @Test
    public void values2() {
        Display referenceDisplay = Display.of(Range.of(-10, 10), Range.of(-9, 9), Range.of(-8, 8), Range.undefined(), "", Display.defaultNumberFormat());
        ListDouble expectedValues = ArrayDouble.of(10, 8, 6, 4, 2, 0, -2, -4, -6, -8, -10, 10);
        List<Alarm> expectedAlarms = Arrays.asList(Alarm.hihi(), Alarm.high(), Alarm.none(), Alarm.none(), Alarm.none(), Alarm.none(), Alarm.none(), Alarm.none(), Alarm.none(), Alarm.low(), Alarm.lolo(), Alarm.hihi());

        Ramp ramp = new Ramp(-10.0, 10.0, -2.0, 1.0);

        testVDoubleSimFunction(ramp, expectedValues, expectedAlarms, referenceDisplay);
    }
}
