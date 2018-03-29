/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient;

import org.epics.gpclient.PVEvent;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A collector can be written from one thread and read from another and provides
 * the point where two subsystems and their rate can be decoupled.
 *
 * @param <I> the type written in the collector
 * @param <O> the type read from the collector
 * @author carcassi
 */
public abstract class ReadCollector<I, O> {
    
    class CollectorSupplier implements Supplier<O> {

        @Override
        public O get() {
            return getValue();
        }
        
    }
    
    protected final Object lock = new Object();
    protected Consumer<PVEvent> collectorListener;
    protected boolean connection = false;
    private final Class<I> type;
    private final Supplier<O> readFunction = new CollectorSupplier();

    public ReadCollector(Class<I> type) {
        // TODO check null
        this.type = type;
    }
    
    void setUpdateListener(Consumer<PVEvent> notification) {
        synchronized (lock) {
            this.collectorListener = notification;
        }
    }
    
    Supplier<O> getReadFunction() {
        return readFunction;
    }
    
    abstract O getValue();
    
    boolean getConnection() {
        synchronized(lock) {
            return connection;
        }
    }
    
    public Class<I> getType() {
        return type;
    }
    
    public abstract void updateValue(I value);

    public abstract void updateValueAndConnection(I value, boolean connection);

    public void updateConnection(boolean newConnection) {
        Consumer<PVEvent> listener;
        synchronized (lock) {
            connection = newConnection;
            listener = collectorListener;
        }
        // Run the task without holding the lock
        if (listener != null) {
            listener.accept(PVEvent.connectionEvent());
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
    
}