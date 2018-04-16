/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author carcassi
 */
public class WriteCollector<T> {
    
    public static class WriteRequest<T> {
        private final T value;
        private final Consumer<PVEvent> writeCallback;

        WriteRequest(T value, Consumer<PVEvent> writeCallback) {
            this.value = value;
            this.writeCallback = writeCallback;
        }

        
        public T getValue() {
            return value;
        }
        
        public void writeSuccessful() {
            writeCallback.accept(PVEvent.writeSucceededEvent());
        }
        
        public void writeFailed(Exception ex) {
            writeCallback.accept(PVEvent.writeFailedEvent(ex));
        }
    }
    
    class CollectorConsumer implements Consumer<T> {
        @Override
        public void accept(T value) {
            queueValue(value);
        }
    }
    
    private final Object lock = new Object();
    private boolean connection = false;
    private Consumer<PVEvent> collectorListener;
    private Consumer<WriteRequest<?>> writeListener;
    private final Consumer<T> writeFunction = new WriteCollector.CollectorConsumer();
    private Optional<T> value;
    private Integer writeId;
    
    WriteCollector() {
    }

    Consumer<T> getWriteFunction() {
        return writeFunction;
    }
    
    boolean getConnection() {
        synchronized(lock) {
            return connection;
        }
    }
   
    void setUpdateListener(Consumer<PVEvent>  collectorListener) {
        synchronized (lock) {
            this.collectorListener = collectorListener;
        }
    }
    
    public void setWriteNotification(Consumer<WriteRequest<?>> writeListener) {
        synchronized (lock) {
            this.writeListener = writeListener;
        }
    }
    
    void prepareWrite(int writeId) {
        synchronized(lock) {
            if (this.writeId != null) {
                throw new IllegalStateException("Asked to prepare for writeId " + writeId + " while haven't submitted request for " + this.writeId);
            } else {
                this.writeId = writeId;
            }
        }
    }
    
    void queueValue(T newValue) {
        synchronized(lock) {
            if (this.writeId == null) {
                throw new IllegalStateException("Received unexpected value to write");
            } else {
                this.value = Optional.ofNullable(newValue);
            }
        }
    }
    
    void sendWriteRequest(int writeId, Consumer<PVEvent> writeCallback) {
        Consumer<WriteRequest<?>> listener;
        WriteRequest<T> request;
        synchronized (lock) {
            if (this.writeId == null) {
                throw new IllegalStateException("Received unexpected send write request");
            }
            if (this.value != null) {
                request = new WriteRequest<>(this.value.get(), writeCallback);
            } else {
                request = null;
            }
            listener = writeListener;
        }
        // If no value was sent to be written, return successful
        if (request == null) {
            new WriteRequest<>(null, writeCallback).writeSuccessful();
        }
        
        // Run the task without holding the lock
        if (listener != null) {
            listener.accept(request);
        } else {
            request.writeFailed(new RuntimeException("No listener was registered to process write for value " + request.getValue()));
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
    
}
