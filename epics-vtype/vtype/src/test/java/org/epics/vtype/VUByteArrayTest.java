/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.array.ArrayUByte;
import org.epics.util.array.ListInteger;
import org.epics.util.array.ListUByte;

/**
 *
 * @author carcassi
 */
public class VUByteArrayTest extends FeatureTestVNumberArray<ListUByte, VUByteArray> {

    @Override
    ListUByte getData() {
        return ArrayUByte.of(new byte[] {0,1,2,3,4,5,6,7,8,9});
    }

    @Override
    ListUByte getOtherData() {
        return ArrayUByte.of(new byte[] {0,-1,-2,-3,-4,-5,-6,-7,-8,-9});
    }

    @Override
    VUByteArray of(ListUByte data, Alarm alarm, Time time, Display display) {
        return VUByteArray.of(data, alarm, time, display);
    }

    @Override
    VUByteArray of(ListUByte data, ListInteger sizes, Alarm alarm, Time time, Display display) {
        return VUByteArray.of(data, sizes, alarm, time, display);
    }

    @Override
    String getToString() {
        // Modified precision of test to match joda time's millisecond precision
        return "VUByteArray[[0, 1, 2, 3, 4, 5, 6, 7, 8, 9], size [5, 2], MINOR(DB) - LOW, 2012-12-05T14:57:21.521Z]";
    }

}
