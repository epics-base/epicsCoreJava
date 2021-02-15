/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.util.array.ArrayDouble;
import org.epics.util.stats.Range;
import org.epics.vtype.Display;
import org.junit.Test;

/**
 * Tests uniform noise distribution function
 *
 * @author carcassi
 */
public class NoiseTest extends FeatureTestSimFunction {

    @Test
    public void values1() {
        Noise noise = new Noise(-10.0, 10.0, 1.0);
        testVDoubleDistributionSimFunction(noise, 100000, ArrayDouble.of(-5.0,0.0,5.0,Double.MAX_VALUE), ArrayDouble.of(0.25,0.25,0.25,0.25),
                Display.of(Range.of(-10, 10), Range.of(-9, 9), Range.of(-8, 8), Range.undefined(), "", Display.defaultNumberFormat()));
    }
}
