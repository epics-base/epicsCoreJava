/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import org.epics.util.array.ArrayFloat;
import org.epics.util.array.ListFloat;
import org.epics.util.array.ListInt;

/**
 *
 * @author carcassi
 */
public class VFloatArrayTest extends FeatureTestVNumberArray<ListFloat, VFloatArray> {

    @Override
    ListFloat getData() {
        return ArrayFloat.of(0,1,2,3,4,5,6,7,8,9);
    }

    @Override
    VFloatArray of(ListFloat data, Alarm alarm, Time time, Display display) {
        return VFloatArray.of(data, alarm, time, display);
    }

    @Override
    VFloatArray of(ListFloat data, ListInt sizes, Alarm alarm, Time time, Display display) {
        return VFloatArray.of(data, sizes, alarm, time, display);
    }

    @Override
    String getToString() {
        return "VFloatArray[[0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0], size [5, 2], MINOR(DB) - LOW, 2012-12-05T14:57:21.521786982Z]";
    }
    
}
