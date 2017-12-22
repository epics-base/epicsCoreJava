/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import org.epics.util.array.ArrayInt;
import org.epics.util.array.ListInt;
import org.epics.util.array.ListUByte;

/**
 * Scalar unsigned byte array with alarm, timestamp, display and control information.
 * 
 * @author carcassi
 */
public abstract class VUByteArray extends VNumberArray {
    
    /**
     * {@inheritDoc }
     */
    @Override
    public abstract ListUByte getData();
    
    /**
     * Creates a new VUByteArray.
     * 
     * @param data the value
     * @param sizes the sizes
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VUByteArray of(final ListUByte data, final ListInt sizes, final Alarm alarm, final Time time, final Display display) {
        return new IVUByteArray(data, sizes, alarm, time, display);
    }
    
    /**
     * Creates a new VUByteArray.
     * 
     * @param data the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VUByteArray of(final ListUByte data, final Alarm alarm, final Time time, final Display display) {
        return of(data, ArrayInt.of(data.size()), alarm, time, display);
    }
}
