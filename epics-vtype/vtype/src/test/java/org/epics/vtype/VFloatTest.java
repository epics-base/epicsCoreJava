/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

/**
 *
 * @author carcassi
 */
public class VFloatTest extends FeatureTestVNumber<Float, VFloat> {

    @Override
    Float getValue() {
        return 1.0f;
    }

    @Override
    Float getAnotherValue() {
        return 0.0f;
    }

    @Override
    VFloat of(Float value, Alarm alarm, Time time, Display display) {
        return VFloat.of(value, alarm, time, display);
    }

    @Override
    String getToString() {
        // Modified precision of test to match joda time's millisecond precision
        return "VFloat[1.0, MINOR(DB) - LOW, 2012-12-05T14:57:21.521Z]";
    }

}
