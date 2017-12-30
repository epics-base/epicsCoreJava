/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

/**
 *
 * @author carcassi
 */
public class VIntTest extends FeatureTestVNumber<Integer, VInt> {

    @Override
    Integer getValue() {
        return 1;
    }

    @Override
    Integer getAnotherValue() {
        return 0;
    }

    @Override
    VInt of(Integer value, Alarm alarm, Time time, Display display) {
        return VInt.of(value, alarm, time, display);
    }

    @Override
    String getToString() {
        return "VInt[1, MINOR(DB) - LOW, 2012-12-05T14:57:21.521786982Z]";
    }
    
}