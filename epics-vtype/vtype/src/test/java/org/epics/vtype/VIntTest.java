/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

/**
 *
 * @author carcassi
 */
public class VIntTest extends FeatureTestVNumber<Integer, VInt> {

    @Override
    Integer getValue() {
        return 1;
    }

    @Override
    Integer getAnotherValue() {
        return 0;
    }

    @Override
    VInt of(Integer value, Alarm alarm, Time time, Display display) {
        return VInt.of(value, alarm, time, display);
    }

    @Override
    String getToString() {
        // Modified precision of test to match joda time's millisecond precision
        return "VInt[1, MINOR(DB) - LOW, 2012-12-05T14:57:21.521Z]";
    }

}
