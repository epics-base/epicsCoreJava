/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import org.epics.util.array.ArrayByte;
import org.epics.util.array.ListByte;
import org.epics.util.array.ListInteger;

/**
 *
 * @author carcassi
 */
public class VByteArrayTest extends FeatureTestVNumberArray<ListByte, VByteArray> {

    @Override
    ListByte getData() {
        return ArrayByte.of(new byte[] {0,1,2,3,4,5,6,7,8,9});
    }

    @Override
    ListByte getOtherData() {
        return ArrayByte.of(new byte[] {0,-1,-2,-3,-4,-5,-6,-7,-8,-9});
    }

    @Override
    VByteArray of(ListByte data, Alarm alarm, Time time, Display display) {
        return VByteArray.of(data, alarm, time, display);
    }

    @Override
    VByteArray of(ListByte data, ListInteger sizes, Alarm alarm, Time time, Display display) {
        return VByteArray.of(data, sizes, alarm, time, display);
    }

    @Override
    String getToString() {
        return "VByteArray[[0, 1, 2, 3, 4, 5, 6, 7, 8, 9], size [5, 2], MINOR(DB) - LOW, 2012-12-05T14:57:21.521786982Z]";
    }
    
}
