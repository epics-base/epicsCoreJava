/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

/**
 *
 * @author carcassi
 */
public class VDoubleTest extends FeatureTestVNumber<Double, VDouble> {

    @Override
    Double getValue() {
        return 1.0;
    }

    @Override
    Double getAnotherValue() {
        return 0.0;
    }

    @Override
    VDouble of(Double value, Alarm alarm, Time time, Display display) {
        return VDouble.of(value, alarm, time, display);
    }

    @Override
    String getToString() {
        return "VDouble[1.0, MINOR(DB) - LOW, 2012-12-05T14:57:21.521786982Z]";
    }
    
}
