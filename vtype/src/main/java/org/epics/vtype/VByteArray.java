/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import org.epics.util.array.ArrayInt;
import org.epics.util.array.ListByte;
import org.epics.util.array.ListInt;

/**
 * Scalar double array with alarm, timestamp, display and control information.
 * 
 * @author carcassi
 */
public abstract class VByteArray extends VNumberArray {
    
    /**
     * {@inheritDoc }
     */
    @Override
    public abstract ListByte getData();
    
    /**
     * Creates a new VByte.
     * 
     * @param data the value
     * @param sizes the sizes
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VByteArray of(final ListByte data, final ListInt sizes, final Alarm alarm, final Time time, final Display display) {
        return new IVByteArray(data, sizes, alarm, time, display);
    }
    
    /**
     * Creates a new VByte.
     * 
     * @param data the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VByteArray of(final ListByte data, final Alarm alarm, final Time time, final Display display) {
        return of(data, ArrayInt.of(data.size()), alarm, time, display);
    }
}
