/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
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
    VULong of(ULong value, Alarm alarm, Time time, Display display) {
        return VULong.of(value, alarm, time, display);
    }

    @Override
    String getToString() {
        return "VULong[18446744073709551615, MINOR(DB) - LOW, 2012-12-05T14:57:21.521786982Z]";
    }
    
}
