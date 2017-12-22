/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import org.epics.util.array.ListInt;
import org.epics.util.array.ListUInt;

/**
 * Immutable VUIntArray implementation.
 *
 * @author carcassi
 */
class IVUIntArray extends VUIntArray {
    
    private final ListUInt data;
    private final ListInt sizes;
    private final Alarm alarm;
    private final Time time;
    private final Display display;

    IVUIntArray(ListUInt data, ListInt sizes, Alarm alarm, Time time, Display display) {
        VType.argumentNotNull("data", data);
        VType.argumentNotNull("sizes", sizes);
        VType.argumentNotNull("alarm", alarm);
        VType.argumentNotNull("time", time);
        VType.argumentNotNull("display", display);
        this.data = data;
        this.alarm = alarm;
        this.time = time;
        this.display = display;
        this.sizes = sizes;
    }

    @Override
    public ListInt getSizes() {
        return sizes;
    }

    @Override
    public ListUInt getData() {
        return data;
    }

    @Override
    public Alarm getAlarm() {
        return alarm;
    }

    @Override
    public Time getTime() {
        return time;
    }

    @Override
    public Display getDisplay() {
        return display;
    }

}
