/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient.datasource.sim;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.epics.util.array.ArrayDouble;
import org.epics.util.array.CollectionNumbers;
import org.epics.util.array.ListDouble;
import org.epics.util.array.ListNumbers;
import org.epics.vtype.Alarm;
import org.epics.vtype.Display;
import org.epics.vtype.VDouble;
import org.hamcrest.Matchers;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author carcassi
 */
public class FeatureTestSimFunction {
    
    void testVDoubleSimFunction(SimFunction<VDouble> function, ListDouble expectedValues, List<Alarm> expectedAlarms, Display referenceDisplay) {
        List<VDouble> values = function.createValuesBefore(Instant.now().plus(function.getTimeBetweenSamples().multipliedBy(expectedValues.size())));
        for (int i = 0; i < expectedValues.size(); i++) {
            VDouble value = values.get(i);
            assertThat(value.getValue(), closeTo(expectedValues.getDouble(i), 0.000001));
            assertThat(value.getAlarm(), equalTo(expectedAlarms.get(i)));
            assertThat(value.getDisplay(), equalTo(referenceDisplay));
        }
    }
    
    void testVDoubleDistributionSimFunction(SimFunction<VDouble> function, int nValues, ListDouble cutoffValues, ListDouble expectedFraction, Display referenceDisplay) {
        Instant currentTime = Instant.now();
        double[] count = new double[expectedFraction.size()];
        for (int i = 0; i < nValues; i++) {
            VDouble value = function.nextValue(currentTime);
            assertThat(value.getDisplay(), equalTo(referenceDisplay));
            int bin = ListNumbers.binarySearchValueOrHigher(cutoffValues, value.getValue());
            count[bin]++;
            currentTime.plus(function.getTimeBetweenSamples());
        }

        double sum = 0;
        for (int i = 0; i < count.length; i++) {
            assertThat(count[i]/nValues, lessThan(expectedFraction.getDouble(i) + 0.01));
            sum+=count[i];
        }
        assertThat(sum, equalTo((double) nValues));
    }
    
}
