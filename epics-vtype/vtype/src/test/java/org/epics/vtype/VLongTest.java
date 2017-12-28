/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

/**
 *
 * @author carcassi
 */
public class VLongTest extends FeatureTestVNumber<Long, VLong> {

    @Override
    Long getValue() {
        return 1L;
    }

    @Override
    VLong of(Long value, Alarm alarm, Time time, Display display) {
        return VLong.of(value, alarm, time, display);
    }

    @Override
    String getToString() {
        return "VLong[1, MINOR(DB) - LOW, 2012-12-05T14:57:21.521786982Z]";
    }
    
}