/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource;

import org.epics.gpclient.ReadCollector;
import org.epics.gpclient.TypeMismatchException;
import org.epics.gpclient.WriteCollector;
import org.epics.util.compat.legacy.functional.Consumer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements a {@link ChannelHandler} on top of a single subscription and
 * multiplexes all reads on top of it.
 * <p>
 * This abstract handler takes care of forwarding the connection and message
 * events of a single connection to multiple readers and writers. One needs
 * to:
 * <ul>
 * <li>implement the {@link #connect() } and {@link #disconnect() } function
 * to add the protocol specific connection and disconnection logic; the resources
 * shared across multiple channels should be left in the datasource</li>
 * <li>every time the connection state changes, call {@link #processConnection(java.lang.Object) },
 * which will trigger the proper connection notification mechanism;
 * the type chosen as connection payload should be one that stores all the
 * information about the channel of communications</li>
 * <li>every time an event is sent, call {@link #processMessage(java.lang.Object) }, which
 * will trigger the proper value notification mechanism</li>
 * <li>implement {@link #isConnected(java.lang.Object) } and {@link #isWriteConnected(java.lang.Object) }
 * with the logic to extract the connection information from the connection payload</li>
 * <li>use {@link #reportExceptionToAllReadersAndWriters(java.lang.Exception) }
 * to report errors</li>
 * <li>implement a set of {@link DataSourceTypeAdapter} that can convert
 * the payload to types for pvmanager consumption; the connection payload and
 * message payload never leave this handler, only value types created by the
 * type adapters</li>
 * </ul>
 *
 * @param <ConnectionPayload> type of the payload for the connection
 * @param <MessagePayload> type of the payload for each message
 * @author carcassi
 */
public abstract class MultiplexedChannelHandler<ConnectionPayload, MessagePayload> extends ChannelHandler {

    private static final Logger log = Logger.getLogger(MultiplexedChannelHandler.class.getName());
    private final boolean readOnly;
    private int readUsageCounter = 0;
    private int writeUsageCounter = 0;
    private boolean connected = false;
    private boolean writeConnected = false;
    private MessagePayload lastMessage;
    private ConnectionPayload connectionPayload;
    private Map<ReadCollector, MonitorHandler> readers = new ConcurrentHashMap<ReadCollector, MonitorHandler>();
    private Map<WriteCollector, Consumer<WriteCollector.WriteRequest<?>>> writers = new ConcurrentHashMap<WriteCollector, Consumer<WriteCollector.WriteRequest<?>>>();
    private boolean processMessageOnDisconnect = true;
    private boolean processMessageOnReconnect = true;

    private class MonitorHandler {

        private final ReadCollector subscription;
        private DataSourceTypeAdapter<ConnectionPayload, MessagePayload> typeAdapter;

        public MonitorHandler(ReadCollector subscription) {
            this.subscription = subscription;
        }

        public final void processConnection(boolean connection) {
            subscription.updateConnection(connection);;
        }

        public final void processValue(MessagePayload payload) {
            if (typeAdapter == null)
                return;

            // Lock the collector and prepare the new value.
            try {
                typeAdapter.updateCache(subscription, getConnectionPayload(), payload);
            } catch (RuntimeException e) {
                subscription.notifyError(e);
            }
        }

        public final void findTypeAdapter() {
            if (getConnectionPayload() == null) {
                typeAdapter = null;
            } else {
                try {
                    typeAdapter = MultiplexedChannelHandler.this.findTypeAdapter(subscription, getConnectionPayload());
                } catch(RuntimeException ex) {
                    subscription.notifyError(ex);
                }
            }
        }

    }

    /**
     * Notifies all readers and writers of an error condition.
     *
     * @param ex the exception to notify
     */
    protected synchronized final void reportExceptionToAllReadersAndWriters(Exception ex) {
        for (ReadCollector subscription : readers.keySet()) {
            subscription.notifyError(ex);
        }
        for (WriteCollector subscription : writers.keySet()) {
            subscription.notifyError(ex);
        }
    }

    /**
     * Notifies all writers of an error condition.
     *
     * @param ex the exception to notify
     */
    protected synchronized final void reportExceptionToAllWriters(Exception ex) {
        for (WriteCollector subscription : writers.keySet()) {
            subscription.notifyError(ex);
        }
    }

    private void reportConnectionStatus(boolean connected) {
        for (MonitorHandler monitor : readers.values()) {
            monitor.processConnection(connected);
        }
    }

    private void reportWriteConnectionStatus(boolean writeConnected) {
        for (WriteCollector subscription : writers.keySet()) {
            subscription.updateConnection(writeConnected);
        }
    }

    /**
     * The last processes connection payload.
     *
     * @return the connection payload or null
     */
    protected synchronized final ConnectionPayload getConnectionPayload() {
        return connectionPayload;
    }

    /**
     * The last processed message payload.
     *
     * @return the message payload or null
     */
    protected synchronized final MessagePayload getLastMessagePayload() {
        return lastMessage;
    }

    /**
     * Process the next connection payload. This should be called whenever
     * the connection state has changed.
     *
     * @param connectionPayload connection payload; not null
     */
    protected synchronized final void processConnection(ConnectionPayload connectionPayload) {
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, "processConnection for channel {0} connectionPayload {1}", new Object[] {getChannelName(), connectionPayload});
        }

        this.connectionPayload = connectionPayload;
        setConnected(isConnected(connectionPayload));
        setWriteConnected(isWriteConnected(connectionPayload));

        for (MonitorHandler monitor : readers.values()) {
            monitor.findTypeAdapter();
        }

        if (isConnected() && lastMessage != null && processMessageOnReconnect) {
            processMessage(lastMessage);
        }
        if (!isConnected() && lastMessage != null && processMessageOnDisconnect) {
            processMessage(lastMessage);
        }
    }

    private static DataSourceTypeAdapter<?, ?> defaultTypeAdapter = new DataSourceTypeAdapter<Object, Object>() {

            public boolean match(ReadCollector<?, ?> cache, Object connection) {
                return true;
            }

            public Object getSubscriptionParameter(ReadCollector<?, ?> cache, Object connection) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @SuppressWarnings("unchecked")
            public void updateCache(ReadCollector cache, Object connection, Object message) {
                if (message == null || cache.getType().isInstance(message)) {
                    cache.updateValue(message);
                } else {
                    throw new TypeMismatchException("Payload " + message + " does not match " + cache.getType().getSimpleName());
                }
            }
        };

    /**
     * Finds the right adapter to use for the particular cache given the information
     * of the channels in the connection payload. By overriding this method
     * a datasource can implement their own matching logic. One
     * can use the logic provided in {@link DataSourceTypeSupport} as
     * a good first implementation.
     *
     * @param cache the cache that will store the data
     * @param connection the connection payload
     * @return the matched type adapter
     */
    @SuppressWarnings("unchecked")
    protected DataSourceTypeAdapter<ConnectionPayload, MessagePayload> findTypeAdapter(ReadCollector<?, ?> cache, ConnectionPayload connection) {
        return (DataSourceTypeAdapter<ConnectionPayload, MessagePayload>) (DataSourceTypeAdapter) defaultTypeAdapter;
    }

    /**
     * Creates a new channel handler.
     *
     * @param channelName the name of the channel this handler will be responsible of
     */
    public MultiplexedChannelHandler(String channelName) {
        this(channelName, false);
    }

    /**
     * Creates a new channel handler.
     *
     * @param channelName the name of the channel this handler will be responsible of
     * @param readOnly whether the channel is read-only
     */
    public MultiplexedChannelHandler(String channelName, boolean readOnly) {
        super(channelName);
        this.readOnly = readOnly;
    }

    @Override
    public synchronized int getUsageCounter() {
        return readUsageCounter + writeUsageCounter;
    }

    @Override
    public synchronized int getReadUsageCounter() {
        return readUsageCounter;
    }

    @Override
    public synchronized int getWriteUsageCounter() {
        return writeUsageCounter;
    }

    @Override
    protected synchronized void addReader(ReadCollector subscription) {
        readUsageCounter++;
        MonitorHandler monitor = new MonitorHandler(subscription);
        readers.put(subscription, monitor);
        monitor.findTypeAdapter();
        guardedConnect();
        if (getUsageCounter() > 1) {
            if (connectionPayload != null) {
                monitor.processConnection(isConnected());
            }
            if (lastMessage != null) {
                monitor.processValue(lastMessage);
            }
        }
    }

    @Override
    protected synchronized void removeReader(ReadCollector subscription) {
        readers.remove(subscription);
        readUsageCounter--;
        guardedDisconnect();
    }

    @Override
    protected synchronized void addWriter(final WriteCollector subscription) {
        if (!readOnly) {
            writeUsageCounter++;
            Consumer<WriteCollector.WriteRequest<?>> collectorListener = new Consumer<WriteCollector.WriteRequest<?>>() {
                @Override
                public void accept(WriteCollector.WriteRequest<?> writeRequest) {
                    processWriteRequest(writeRequest);
                }
            };
            subscription.setWriteNotification(collectorListener);
            writers.put(subscription, collectorListener);
            guardedConnect();
            if (connectionPayload != null) {
                subscription.updateConnection(isWriteConnected());
            }
        } else {
            subscription.notifyError(new ReadOnlyChannelException("Channel " + getChannelName() + " is read only"));
        }
    }

    /**
     * Process the write request. Override this method to implement writes asynchronously.
     * Take the value from the request and, when the response arrives, calls
     * either {@link WriteCollector.WriteRequest#writeSuccessful() } or
     * {@link WriteCollector.WriteRequest#writeFailed(java.lang.Exception) }.
     * <p>
     * To implement writes, either this method or {@link #write(java.lang.Object) }
     * should be overriden.
     *
     * @param request the request to be processed
     */
    protected void processWriteRequest(WriteCollector.WriteRequest<?> request) {
        try {
            write(request.getValue());
            request.writeSuccessful();
        } catch (Exception ex) {
            request.writeFailed(ex);
        }
    }


    /**
     * Write the value. Override this method to implement writes synchronously.
     * Simply return if the write was successful or throw an exception if it
     * wasn't. The exception is propagated up to the client code, so the
     * error message should be short but descriptive.
     * <p>
     * To implement writes, either this method or {@link #write(java.lang.Object) }
     * should be overridden.
     *
     * @param newValue the new value to write.
     */
    protected void write(Object newValue) {
        throw new RuntimeException("Write not implemented");
    }

    @Override
    protected synchronized void removeWriter(WriteCollector subscription) {
        if (!readOnly) {
            writeUsageCounter--;
            writers.remove(subscription);
            subscription.setWriteNotification(null);
            guardedDisconnect();
        }
    }

    /**
     * Resets the last message to null. This can be used to invalidate
     * the last message without triggering a notification. It is useful
     * when a reconnect should behave as the first connection.
     */
    protected synchronized final void resetMessage() {
        lastMessage = null;
    }

    /**
     * Process the payload for this channel. This should be called whenever
     * a new value needs to be processed. The handler will take care of
     * using the correct {@link DataSourceTypeAdapter}
     * for each read monitor that was setup.
     *
     * @param payload the payload of for this type of channel
     */
    protected synchronized final void processMessage(MessagePayload payload) {
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, "processMessage for channel {0} messagePayload {1}", new Object[]{getChannelName(), payload});
        }

        lastMessage = payload;
        for (MonitorHandler monitor : readers.values()) {
            monitor.processValue(payload);
        }
    }

    private void guardedConnect() {
        if (getUsageCounter() == 1) {
            try {
                connect();
            } catch(RuntimeException ex) {
                reportExceptionToAllReadersAndWriters(ex);
            }
        }
    }

    private void guardedDisconnect() {
        if (getUsageCounter() == 0) {
            try {
                disconnect();
                if (!saveMessageAfterDisconnect()) {
                    lastMessage = null;
                }
                connectionPayload = null;
            } catch (RuntimeException ex) {
                reportExceptionToAllReadersAndWriters(ex);
                log.log(Level.WARNING, "Couldn't disconnect channel " + getChannelName(), ex);
           }
        }
    }

    /**
     * Signals whether the last message received after the disconnect should
     * be kept so that it is available at reconnect.
     * <p>
     * By default, the message is discarded so that no memory is kept allocated.
     *
     * @return true if the message should be kept
     */
    protected boolean saveMessageAfterDisconnect() {
        return false;
    }

    /**
     * Used by the handler to open the connection. This is called whenever
     * the first read or write request is made.
     */
    protected abstract void connect();

    /**
     * Used by the handler to close the connection. This is called whenever
     * the last reader or writer is de-registered.
     */
    protected abstract void disconnect();

    private void setConnected(boolean connected) {
        this.connected = connected;
        reportConnectionStatus(connected);
    }

    private void setWriteConnected(boolean writeConnected) {
        this.writeConnected = writeConnected;
        reportWriteConnectionStatus(writeConnected);
    }

    /**
     * Determines from the payload whether the channel is connected or not.
     * <p>
     * By default, this uses the usage counter to determine whether it's
     * connected or not. One should override this to use the actual
     * connection payload to check whether the actual protocol connection
     * has been established.
     *
     * @param payload the connection payload
     * @return true if connected
     */
    protected boolean isConnected(ConnectionPayload  payload) {
        return getUsageCounter() > 0;
    }

    /**
     * Determines from the payload whether the channel can be written to.
     * <p>
     * By default, this always return false. One should override this
     * if it's implementing a write-able data source.
     *
     * @param payload connection payload; not null
     * @return true if ready for writes
     */
    protected boolean isWriteConnected(ConnectionPayload payload) {
        return false;
    }

    @Override
    public synchronized final boolean isConnected() {
        return connected;
    }

    /**
     * Returns true if it is channel can be written to.
     *
     * @return true if underlying channel is write ready
     */
    public synchronized final boolean isWriteConnected() {
        // TODO: push this in ChannleHandler?
        return writeConnected;
    }

    /**
     * Determines whether {@link #processConnection(java.lang.Object)} should
     * trigger {@link #processMessage(java.lang.Object)} with the same (non-null)
     * payload in case the channel has been disconnected. Default is true.
     *
     * @param processMessageOnDisconnect whether to process the message on disconnect
     */
    protected synchronized final void setProcessMessageOnDisconnect(boolean processMessageOnDisconnect) {
        this.processMessageOnDisconnect = processMessageOnDisconnect;
    }

    /**
     * Determines whether {@link #processConnection(java.lang.Object)} should
     * trigger {@link #processMessage(java.lang.Object)} with the same (non-null)
     * payload in case the channel has reconnected. Default is true.
     *
     * @param processMessageOnReconnect whether to process the message on disconnect
     */
    protected synchronized final void setProcessMessageOnReconnect(boolean processMessageOnReconnect) {
        this.processMessageOnReconnect = processMessageOnReconnect;
    }


}
