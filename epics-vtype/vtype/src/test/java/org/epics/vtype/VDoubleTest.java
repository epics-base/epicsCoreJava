/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

/**
 *
 * @author carcassi
 */
public class VDoubleTest extends FeatureTestVNumber<Double, VDouble> {

    @Override
    Double getValue() {
        return 1.0;
    }

    @Override
    Double getAnotherValue() {
        return 0.0;
    }

    @Override
    VDouble of(Double value, Alarm alarm, Time time, Display display) {
        return VDouble.of(value, alarm, time, display);
    }

    @Override
    String getToString() {
        // Modified precision of test to match joda time's millisecond precision
        return "VDouble[1.0, MINOR(DB) - LOW, 2012-12-05T14:57:21.521Z]";
    }

}
