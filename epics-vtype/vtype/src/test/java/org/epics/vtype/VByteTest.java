/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

/**
 *
 * @author carcassi
 */
public class VByteTest extends FeatureTestVNumber<Byte, VByte> {

    @Override
    Byte getValue() {
        return 1;
    }

    @Override
    Byte getAnotherValue() {
        return 0;
    }

    @Override
    VByte of(Byte value, Alarm alarm, Time time, Display display) {
        return VByte.of(value, alarm, time, display);
    }

    @Override
    String getToString() {
        return "VByte[1, MINOR(DB) - LOW, 2012-12-05T14:57:21.521786982Z]";
    }
    
}
