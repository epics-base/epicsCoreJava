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
 * Tests gaussian sim function
 *
 * @author carcassi
 */
public class GaussianNoiseTest extends FeatureTestSimFunction {

    @Test
    public void values1() {
        GaussianNoise gaussian = new GaussianNoise(10.0, 10.0, 1.0);
        testVDoubleDistributionSimFunction(gaussian, 100000, ArrayDouble.of(0.0,10.0,20.0,Double.MAX_VALUE), ArrayDouble.of(0.1587, 0.3413, 0.3413, 0.1587),
                Display.of(Range.of(-30, 50), Range.of(-10, 30), Range.of(0, 20), Range.undefined(), "", Display.defaultNumberFormat()));
    }

}
