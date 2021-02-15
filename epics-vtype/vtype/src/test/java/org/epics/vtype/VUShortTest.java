/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.number.UShort;

/**
 *
 * @author carcassi
 */
public class VUShortTest extends FeatureTestVNumber<UShort, VUShort> {

    @Override
    UShort getValue() {
        return UShort.valueOf((short) -1);
    }

    @Override
    UShort getAnotherValue() {
        return UShort.valueOf((short) -0);
    }

    @Override
    VUShort of(UShort value, Alarm alarm, Time time, Display display) {
        return VUShort.of(value, alarm, time, display);
    }

    @Override
    String getToString() {
        // Modified precision of test to match joda time's millisecond precision
        return "VUShort[65535, MINOR(DB) - LOW, 2012-12-05T14:57:21.521Z]";
    }

}
