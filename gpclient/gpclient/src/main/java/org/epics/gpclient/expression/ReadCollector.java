/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.expression;

import java.util.function.Consumer;

/**
 * A collector can be written from one thread and read from another and provides
 * the point where two subsystems and their rate can be decoupled.
 *
 * @param <I> the type written in the collector
 * @param <O> the type read from the collector
 * @author carcassi
 */
public abstract class ReadCollector<I, O> {
    
    protected final Object lock = new Object();
    protected Consumer<ReadEvent> collectorListener;
    protected boolean connection = false;
    private final Class<I> type;

    public ReadCollector(Class<I> type) {
        // TODO check null
        this.type = type;
    }
    
    void setUpdateListener(Consumer<ReadEvent> notification) {
        synchronized (lock) {
            this.collectorListener = notification;
        }
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
        Consumer<ReadEvent> listener;
        synchronized (lock) {
            connection = newConnection;
            listener = collectorListener;
        }
        // Run the task without holding the lock
        if (listener != null) {
            listener.accept(new ReadEvent(null, ReadEvent.Type.READ_CONNECTION));
        }
    }

    public void notifyError(Exception ex) {
        Consumer<ReadEvent> listener;
        synchronized (lock) {
            listener = collectorListener;
        }
        // Run the task without holding the lock
        listener.accept(new ReadEvent(ex, ReadEvent.Type.READ_EXCEPTION));
    }
    
}
