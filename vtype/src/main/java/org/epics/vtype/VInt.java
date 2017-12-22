/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

/**
 * Scalar integer with alarm, timestamp, display and control information.
 * Auto-unboxing makes the extra method for the primitive type
 * unnecessary.
 * 
 * @author carcassi
 */
public abstract class VInt extends VNumber {
    
    /**
     * {@inheritDoc }
     */
    @Override
    public abstract Integer getValue();
    
    /**
     * Creates a new VInt.
     * 
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VInt of(final Integer value, final Alarm alarm, final Time time, final Display display) {
        return new IVInt(value, alarm, time, display);
    }
}
