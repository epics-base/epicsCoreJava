/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

/**
 *
 * @author carcassi
 */
public class VFloatTest extends FeatureTestVNumber<Float, VFloat> {

    @Override
    Float getValue() {
        return 1.0f;
    }

    @Override
    VFloat of(Float value, Alarm alarm, Time time, Display display) {
        return VFloat.of(value, alarm, time, display);
    }

    @Override
    String getToString() {
        return "VFloat[1.0, MINOR(DB) - LOW, 2012-12-05T14:57:21.521786982Z]";
    }
    
}