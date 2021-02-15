/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.array.ArrayULong;
import org.epics.util.array.ListInteger;
import org.epics.util.array.ListULong;

/**
 *
 * @author carcassi
 */
public class VULongArrayTest extends FeatureTestVNumberArray<ListULong, VULongArray> {

    @Override
    ListULong getData() {
        return ArrayULong.of(0,1,2,3,4,5,6,7,8,9);
    }

    @Override
    ListULong getOtherData() {
        return ArrayULong.of(0,-1,-2,-3,-4,-5,-6,-7,-8,-9);
    }

    @Override
    VULongArray of(ListULong data, Alarm alarm, Time time, Display display) {
        return VULongArray.of(data, alarm, time, display);
    }

    @Override
    VULongArray of(ListULong data, ListInteger sizes, Alarm alarm, Time time, Display display) {
        return VULongArray.of(data, sizes, alarm, time, display);
    }

    @Override
    String getToString() {
        // Modified precision of test to match joda time's millisecond precision
        return "VULongArray[[0, 1, 2, 3, 4, 5, 6, 7, 8, 9], size [5, 2], MINOR(DB) - LOW, 2012-12-05T14:57:21.521Z]";
    }

}
