/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.array.ArrayLong;
import org.epics.util.array.ListLong;
import org.epics.util.array.ListInteger;

/**
 *
 * @author carcassi
 */
public class VLongArrayTest extends FeatureTestVNumberArray<ListLong, VLongArray> {

    @Override
    ListLong getData() {
        return ArrayLong.of(new long[] {0,1,2,3,4,5,6,7,8,9});
    }

    @Override
    ListLong getOtherData() {
        return ArrayLong.of(new long[] {0,-1,-2,-3,-4,-5,-6,-7,-8,-9});
    }

    @Override
    VLongArray of(ListLong data, Alarm alarm, Time time, Display display) {
        return VLongArray.of(data, alarm, time, display);
    }

    @Override
    VLongArray of(ListLong data, ListInteger sizes, Alarm alarm, Time time, Display display) {
        return VLongArray.of(data, sizes, alarm, time, display);
    }

    @Override
    String getToString() {
        // Modified precision of test to match joda time's millisecond precision
        return "VLongArray[[0, 1, 2, 3, 4, 5, 6, 7, 8, 9], size [5, 2], MINOR(DB) - LOW, 2012-12-05T14:57:21.521Z]";
    }

}
