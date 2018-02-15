/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource.sim;

import java.util.Arrays;
import java.util.List;
import org.epics.util.array.ArrayBoolean;
import org.epics.util.array.ListBoolean;
import org.epics.vtype.Alarm;
import org.junit.Test;

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
