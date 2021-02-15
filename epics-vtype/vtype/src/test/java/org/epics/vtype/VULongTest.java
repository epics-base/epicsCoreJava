/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.number.ULong;

/**
 *
 * @author carcassi
 */
public class VULongTest extends FeatureTestVNumber<ULong, VULong> {

    @Override
    ULong getValue() {
        return ULong.valueOf(-1);
    }

    @Override
    ULong getAnotherValue() {
        return ULong.valueOf(1);
    }

    @Override
    VULong of(ULong value, Alarm alarm, Time time, Display display) {
        return VULong.of(value, alarm, time, display);
    }

    @Override
    String getToString() {
        // Modified precision of test to match joda time's millisecond precision
        return "VULong[18446744073709551615, MINOR(DB) - LOW, 2012-12-05T14:57:21.521Z]";
    }

}
