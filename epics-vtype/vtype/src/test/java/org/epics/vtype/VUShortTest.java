/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import org.epics.util.number.UShort;

/**
 *
 * @author carcassi
 */
public class VUShortTest extends FeatureTestVNumber<UShort, VUShort> {

    @Override
    UShort getValue() {
        return UShort.valueOf((short) -1);
    }

    @Override
    UShort getAnotherValue() {
        return UShort.valueOf((short) -0);
    }

    @Override
    VUShort of(UShort value, Alarm alarm, Time time, Display display) {
        return VUShort.of(value, alarm, time, display);
    }

    @Override
    String getToString() {
        return "VUShort[65535, MINOR(DB) - LOW, 2012-12-05T14:57:21.521786982Z]";
    }
    
}
