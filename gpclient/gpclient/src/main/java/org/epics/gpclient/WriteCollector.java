/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient;

import java.util.function.Consumer;

/**
 * @author carcassi
 */
public class WriteCollector<T> {
    
    class CollectorConsumer implements Consumer<T> {
        @Override
        public void accept(T t) {
            queueValue(value);
        }
    }
    
    private final Object lock = new Object();
    private boolean connection = false;
    private Consumer<PVEvent> collectorListener;
    private Consumer<WriteCollector<?>> writeListener;
    private final Consumer<T> writeFunction = new WriteCollector.CollectorConsumer();
    private T value;
    
    WriteCollector() {
    }

    public Consumer<T> getWriteFunction() {
        return writeFunction;
    }
   
    void setUpdateListener(Consumer<PVEvent>  collectorListener) {
        synchronized (lock) {
            this.collectorListener = collectorListener;
        }
    }
    
    public void setWriteNotification(Consumer<WriteCollector<?>> writeListener) {
        synchronized (lock) {
            this.writeListener = writeListener;
        }
    }
    
    public T getValue() {
        synchronized (lock) {
            return value;
        }
    }
    
    void queueValue(T newValue) {
        Consumer<WriteCollector<?>> listener;
        synchronized (lock) {
            value = newValue;
            listener = writeListener;
        }
        // Run the task without holding the lock
        if (listener != null) {
            listener.accept(this);
        }
    }
    
    public void updateConnection(boolean newConnection) {
        Consumer<PVEvent> listener;
        synchronized (lock) {
            connection = newConnection;
            listener = collectorListener;
        }
        // Run the task without holding the lock
        if (listener != null) {
            listener.accept(PVEvent.writeConnectionEvent());
        }
    }
    
    public void notifyError(Exception ex) {
        Consumer<PVEvent> listener;
        synchronized (lock) {
            listener = collectorListener;
        }
        // Run the task without holding the lock
        if (listener != null) {
            listener.accept(PVEvent.exceptionEvent(ex));
        }
    }
    
    public void sendWriteSuccessful() {
        Consumer<PVEvent> listener;
        synchronized (lock) {
            listener = collectorListener;
        }
        // Run the task without holding the lock
        if (listener != null) {
            listener.accept(PVEvent.writeSucceededEvent());
        }
    }
    
    public void sendWriteFailed(Exception ex) {
        Consumer<PVEvent> listener;
        synchronized (lock) {
            listener = collectorListener;
        }
        // Run the task without holding the lock
        if (listener != null) {
            listener.accept(PVEvent.writeFailedEvent(ex));
        }
    }
    
}
