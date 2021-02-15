/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.number.UByte;

/**
 *
 * @author carcassi
 */
public class VUByteTest extends FeatureTestVNumber<UByte, VUByte> {

    @Override
    UByte getValue() {
        return UByte.valueOf((byte) -1);
    }

    @Override
    UByte getAnotherValue() {
        return UByte.valueOf((byte) 0);
    }

    @Override
    VUByte of(UByte value, Alarm alarm, Time time, Display display) {
        return VUByte.of(value, alarm, time, display);
    }

    @Override
    String getToString() {
        // Modified precision of test to match joda time's millisecond precision
        return "VUByte[255, MINOR(DB) - LOW, 2012-12-05T14:57:21.521Z]";
    }

}
