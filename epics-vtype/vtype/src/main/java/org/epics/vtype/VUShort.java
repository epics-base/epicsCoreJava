/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import org.epics.util.number.UShort;

/**
 * Scalar unsigned short with alarm, timestamp, display and control information.
 * 
 * @author carcassi
 */
public abstract class VUShort extends VNumber {
    
    /**
     * {@inheritDoc }
     */
    @Override
    public abstract UShort getValue();
    
    /**
     * Creates a new VUShort.
     * 
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VUShort of(final UShort value, final Alarm alarm, final Time time, final Display display) {
        return new IVUShort(value, alarm, time, display);
    }
    
    /**
     * Creates a new VUShort.
     * 
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VUShort of(final Number value, final Alarm alarm, final Time time, final Display display) {
        return new IVUShort(new UShort(value.shortValue()), alarm, time, display);
    }
}
