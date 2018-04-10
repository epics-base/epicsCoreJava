/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient;

import java.util.function.Consumer;

/**
 * A collector that keeps only the latest value.
 *
 * @param <T> the type stored in the collector
 * @author carcassi
 */
public class LatestValueCollector<T> extends ReadCollector<T, T> {
    
    private T value;

    public LatestValueCollector(Class<T> type) {
        super(type);
    }

    @Override
    public T getValue() {
        synchronized (lock) {
            return value;
        }
    }

    @Override
    public void updateValue(T newValue) {
        Consumer<PVEvent> listener;
        synchronized (lock) {
            value = newValue;
            listener = collectorListener;
        }
        // Run the task without holding the lock
        if (listener != null) {
            listener.accept(PVEvent.valueEvent());
        }
    }

    @Override
    public void updateValueAndConnection(T newValue, boolean newConnection) {
        Consumer<PVEvent> listener;
        synchronized (lock) {
            value = newValue;
            connection = newConnection;
            listener = collectorListener;
        }
        // Run the task without holding the lock
        if (listener != null) {
            listener.accept(PVEvent.connectionValueEvent());
        }
    }
    
}
