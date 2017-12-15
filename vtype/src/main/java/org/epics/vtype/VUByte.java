/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import org.epics.util.number.UByte;

/**
 * Scalar byte with alarm, timestamp, display and control information.
 * Auto-unboxing makes the extra method for the primitive type
 * unnecessary.
 * 
 * @author carcassi
 */
public abstract class VUByte extends VNumber {
    
    /**
     * {@inheritDoc }
     */
    @Override
    public abstract UByte getValue();
    
    /**
     * Creates a new VUByte.
     * 
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VUByte of(final UByte value, final Alarm alarm, final Time time, final Display display) {
        return new IVUByte(value, alarm, time, display);
    }
}
