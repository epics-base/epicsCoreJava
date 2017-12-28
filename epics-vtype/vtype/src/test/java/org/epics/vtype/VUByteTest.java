/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
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
    VUByte of(UByte value, Alarm alarm, Time time, Display display) {
        return VUByte.of(value, alarm, time, display);
    }

    @Override
    String getToString() {
        return "VUByte[255, MINOR(DB) - LOW, 2012-12-05T14:57:21.521786982Z]";
    }
    
}
