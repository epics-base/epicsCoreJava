/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
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
        return UInteger.valueOf((byte) -1);
    }

    @Override
    VUInt of(UInteger value, Alarm alarm, Time time, Display display) {
        return VUInt.of(value, alarm, time, display);
    }

    @Override
    String getToString() {
        return "VUInt[4294967295, MINOR(DB) - LOW, 2012-12-05T14:57:21.521786982Z]";
    }
    
}
