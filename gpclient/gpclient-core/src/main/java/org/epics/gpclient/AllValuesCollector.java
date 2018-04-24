/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A collector that queues all the values.
 *
 * @param <T> the type stored in the collector
 * @author carcassi
 */
public class AllValuesCollector<T> extends ReadCollector<T, List<T>> {
    
    private List<T> currentValues = new ArrayList<>();

    public AllValuesCollector(Class<T> type) {
        super(type);
    }

    @Override
    public List<T> getValue() {
        synchronized (lock) {
            List<T> values = currentValues;
            currentValues = new ArrayList<>();
            return values;
        }
    }

    @Override
    public void updateValue(T newValue) {
        Consumer<PVEvent> listener;
        synchronized (lock) {
            currentValues.add(newValue);
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
            currentValues.add(newValue);
            connection = newConnection;
            listener = collectorListener;
        }
        // Run the task without holding the lock
        if (listener != null) {
            listener.accept(PVEvent.readConnectionValueEvent());
        }
    }
    
}
