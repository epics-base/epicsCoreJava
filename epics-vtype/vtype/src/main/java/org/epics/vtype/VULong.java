/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import org.epics.util.number.ULong;
import org.epics.util.number.UShort;

/**
 * Scalar unsigned long with alarm, timestamp, display and control information.
 * 
 * @author carcassi
 */
public abstract class VULong extends VNumber {
    
    /**
     * {@inheritDoc }
     */
    @Override
    public abstract ULong getValue();
    
    /**
     * Creates a new VULong.
     * 
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VULong of(final ULong value, final Alarm alarm, final Time time, final Display display) {
        return new IVULong(value, alarm, time, display);
    }
    
    /**
     * Creates a new VULong.
     * 
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VULong of(final Number value, final Alarm alarm, final Time time, final Display display) {
        return new IVULong(new ULong(value.longValue()), alarm, time, display);
    }
}
