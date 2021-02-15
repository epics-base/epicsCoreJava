/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import org.epics.util.compat.legacy.functional.Consumer;

/**
 * A collector that keeps only the latest value.
 *
 * @param <T> the type stored in the collector
 * @author carcassi
 */
class LatestValueCollector<T> extends ReadCollector<T, T> {

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
            listener.accept(PVEvent.readConnectionValueEvent());
        }
    }

}
