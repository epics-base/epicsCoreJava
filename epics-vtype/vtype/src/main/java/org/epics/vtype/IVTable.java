/**
 * Copyright (C) 2010-18 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import java.util.List;

import org.epics.util.array.ListBoolean;
import org.epics.util.array.ListNumber;

/**
 * An immutable table that extends {@link VTable}
 * 
 * @author carcassi, shroff
 */
class IVTable extends VTable {

    private final List<Class<?>> types;
    private final List<String> names;
    private final List<Object> values;
    private final int rowCount;

    IVTable(List<Class<?>> types, List<String> names, List<Object> values) {
        this.types = types;
        this.names = names;
        this.values = values;
        int maxCount = 0;
        for (Object array : values) {
            maxCount = Math.max(maxCount, getDataSize(array));
        }
        this.rowCount = maxCount;
    }

    private static int getDataSize(Object data) {
        if (data instanceof List) {
            return ((List) data).size();
        } else if (data instanceof ListNumber) {
            return ((ListNumber) data).size();
        } else if(data instanceof ListBoolean){
            return ((ListBoolean)data).size();
        }

        throw new IllegalArgumentException("Object " + data + " is not supported");
    }

    @Override
    public int getColumnCount() {
        return values.size();
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public Class<?> getColumnType(int column) {
        return types.get(column);
    }

    @Override
    public String getColumnName(int column) {
        return names.get(column);
    }

    @Override
    public Object getColumnData(int column) {
        return values.get(column);
    }

}
