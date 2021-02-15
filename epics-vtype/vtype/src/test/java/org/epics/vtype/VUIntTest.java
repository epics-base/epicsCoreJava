/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.number.UInteger;

/**
 *
 * @author carcassi
 */
public class VUIntTest extends FeatureTestVNumber<UInteger, VUInt> {

    @Override
    UInteger getValue() {
        return UInteger.valueOf(-1);
    }

    @Override
    UInteger getAnotherValue() {
        return UInteger.valueOf(0);
    }

    @Override
    VUInt of(UInteger value, Alarm alarm, Time time, Display display) {
        return VUInt.of(value, alarm, time, display);
    }

    @Override
    String getToString() {
        // Modified precision of test to match joda time's millisecond precision
        return "VUInt[4294967295, MINOR(DB) - LOW, 2012-12-05T14:57:21.521Z]";
    }

}
