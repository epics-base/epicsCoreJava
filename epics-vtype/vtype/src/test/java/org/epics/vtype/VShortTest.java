/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

/**
 *
 * @author carcassi
 */
public class VShortTest extends FeatureTestVNumber<Short, VShort> {

    @Override
    Short getValue() {
        return 1;
    }

    @Override
    Short getAnotherValue() {
        return 0;
    }

    @Override
    VShort of(Short value, Alarm alarm, Time time, Display display) {
        return VShort.of(value, alarm, time, display);
    }

    @Override
    String getToString() {
        // Modified precision of test to match joda time's millisecond precision
        return "VShort[1, MINOR(DB) - LOW, 2012-12-05T14:57:21.521Z]";
    }

}
