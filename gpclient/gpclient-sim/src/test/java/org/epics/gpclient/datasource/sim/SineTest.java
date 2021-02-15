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
public class SineTest extends FeatureTestSimFunction {

    @Test
    public void values() {
        Display referenceDisplay = Display.of(Range.of(0, 10), Range.of(0.5, 9.5), Range.of(1, 9), Range.undefined(), "", Display.defaultNumberFormat());
        ListDouble expectedValues = ArrayDouble.of(5.0, 10.0, 5.0, 0.0, 5.0, 10.0, 5.0, 0.0, 5.0);
        List<Alarm> expectedAlarms = Arrays.asList(Alarm.none(), Alarm.hihi(), Alarm.none(), Alarm.lolo(), Alarm.none(), Alarm.hihi(), Alarm.none(), Alarm.lolo(), Alarm.none());

        Sine sine = new Sine(0.0, 10.0, 4.0, 1.0);

        testVDoubleSimFunction(sine, expectedValues, expectedAlarms, referenceDisplay);
    }

}
