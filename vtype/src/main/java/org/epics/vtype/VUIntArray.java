/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import org.epics.util.array.ArrayInt;
import org.epics.util.array.ListInt;
import org.epics.util.array.ListUInt;

/**
 * Scalar unsigned byte array with alarm, timestamp, display and control information.
 * 
 * @author carcassi
 */
public abstract class VUIntArray extends VNumberArray {
    
    /**
     * {@inheritDoc }
     */
    @Override
    public abstract ListUInt getData();
    
    /**
     * Creates a new VUIntArray.
     * 
     * @param data the value
     * @param sizes the sizes
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VUIntArray of(final ListUInt data, final ListInt sizes, final Alarm alarm, final Time time, final Display display) {
        return new IVUIntArray(data, sizes, alarm, time, display);
    }
    
    /**
     * Creates a new VUIntArray.
     * 
     * @param data the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VUIntArray of(final ListUInt data, final Alarm alarm, final Time time, final Display display) {
        return of(data, ArrayInt.of(data.size()), alarm, time, display);
    }
}
