/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

/**
 *
 * @author carcassi
 */
public class VLongTest extends FeatureTestVNumber<Long, VLong> {

    @Override
    Long getValue() {
        return 1L;
    }

    @Override
    Long getAnotherValue() {
        return 0L;
    }

    @Override
    VLong of(Long value, Alarm alarm, Time time, Display display) {
        return VLong.of(value, alarm, time, display);
    }

    @Override
    String getToString() {
        // Modified precision of test to match joda time's millisecond precision
        return "VLong[1, MINOR(DB) - LOW, 2012-12-05T14:57:21.521Z]";
    }

}
