/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import org.epics.gpclient.datasource.DataSource;
import org.epics.util.compat.legacy.functional.Consumer;
import org.epics.util.compat.legacy.functional.Supplier;
import org.epics.util.compat.legacy.lang.Objects;
import org.epics.vtype.VType;
import org.joda.time.Duration;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Orchestrates the different elements of pvmanager to make a reader functional.
 * <p>
 * This class is responsible for the correct read operation, including:
 * <ul>
 * <li>Setting up the collector for notifications</li>
 * <li>Setting up the collector for connection notification</li>
 * <li>Building connection recipes and forwarding them to the datasource<li>
 * <li>Managing the scanning task and notification for new values, connection status
 * or errors</li>
 * <li>Disconnecting the expressions from the datasources if the reader is closed
 * or if it's garbage collected</li>
 * </ul>
 *
 * @param <R> the read object type
 * @param <W> the write object type
 * @author carcassi
 */
public class PVDirector<R, W> {

    private static final Logger log = Logger.getLogger(PVDirector.class.getName());

    // Required for connection and exception notification

    /**
     * Executor used to notify of new values/connection/exception
     */
    final Executor notificationExecutor;
    /**
     * Executor used to scan the connection/exception queues
     */
    final ScheduledExecutorService scannerExecutor;
    /**
     * PVReader to update during the notification
     */
    private final WeakReference<PVImpl<R, W>> pvRef;
    /**
     * Function for the new value
     */
    final Supplier<R> readFunction;
    /**
     * Function to write values
     */
    final Consumer<W> writeFunction;
    /**
     * Creation for stack trace
     */
    private final Exception creationStackTrace = new Exception("Open PV was garbage collected: see stack trace for where it was created");
    /**
     * Used to ignore duplicated errors
     */
    private final AtomicReference<Notification> lastNotification = new AtomicReference<Notification>();
    /**
     * Maximum rate for notification
     */
    final Duration maxRate;

    private class Notification {
        final R readValue;
        final boolean readConnection;
        final boolean writeConnection;
        final PVEvent event;

        Notification(R readValue, boolean readConnection, boolean writeConnection, PVEvent event) {
            this.readValue = readValue;
            this.readConnection = readConnection;
            this.writeConnection = writeConnection;
            this.event = event;
        }

        @Override
        public String toString() {
            Map<String, Object> properties = new LinkedHashMap<String, Object>();
            properties.put("event", event);
            properties.put("readConnection", readConnection);
            properties.put("writeConnection", writeConnection);
            properties.put("readValue", readValue);
            return properties.toString();
        }
    }

    private RateDecoupler scanStrategy;


    // Required to connect/disconnect expressions
    private final DataSource dataSource;
    private final Object lock = new Object();
    private final Set<Expression<?, ?>> readExpressions = new HashSet<Expression<?, ?>>();
    private final Set<Expression<?, ?>> writeExpressions = new HashSet<Expression<?, ?>>();

    // Required for multiple operations
    /**
     * Collector required to connect/disconnect expressions and for connection calculation
     */
    private final Set<ReadCollector<?, ?>> readCollectors = new HashSet<ReadCollector<?, ?>>();
    /**
     * Collector required to connect/disconnect expressions and for connection calculation
     */
    private final Set<WriteCollector<?>> writeCollectors = new HashSet<WriteCollector<?>>();

    void setScanner(final RateDecoupler scanStrategy) {
        synchronized (lock) {
            this.scanStrategy = scanStrategy;
        }
    }

    public void registerCollector(ReadCollector<?, ?> collector) {
        collector.setUpdateListener(scanStrategy.getUpdateListener());
        readCollectors.add(collector);
    }

    public void registerCollector(WriteCollector<?> collector) {
        collector.setUpdateListener(scanStrategy.getUpdateListener());
        writeCollectors.add(collector);
    }

    public void deregisterCollector(ReadCollector<?, ?> collector) {
        collector.setUpdateListener(null);
        readCollectors.remove(collector);
    }

    public void deregisterCollector(WriteCollector<?> collector) {
        collector.setUpdateListener(null);
        writeCollectors.remove(collector);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    private boolean calculateConnection() {
        synchronized (lock) {
            boolean connection = true;
            for (ReadCollector<?, ?> readCollector : readCollectors) {
                connection = connection && readCollector.getConnection();
            }
            return connection;
        }
    }

    private boolean calculateWriteConnection() {
        synchronized (lock) {
            boolean writeConnection = true;
            for (WriteCollector<?> writeCollector : writeCollectors) {
                writeConnection = writeConnection && writeCollector.getConnection();
            }
            return writeConnection;
        }
    }

    /**
     * Connects the given expression.
     * <p>
     * This can be used for dynamic expression to add and connect child expressions.
     * The added expression will be automatically closed when the associated
     * reader is closed, if it's not disconnected first.
     *
     * @param expression the expression to connect
     */
    public void connectReadExpression(Expression<?, ?> expression) {
        synchronized (lock) {
            expression.startRead(this);
            readExpressions.add(expression);
        }
    }

    public void connectWriteExpression(Expression<?, ?> expression) {
        synchronized (lock) {
            expression.startWrite(this);
            writeExpressions.add(expression);
        }
    }

    /**
     * Disconnects the given expression.
     * <p>
     * This can be used for dynamic expression, to remove and disconnects child
     * expressions.
     *
     * @param expression the expression to disconnect
     */
    public void disconnectReadExpression(Expression<?, ?> expression) {
        synchronized (lock) {
            if (!readExpressions.remove(expression)) {
                log.log(Level.SEVERE, "Director was asked to disconnect expression '" + expression + "' which was not found.");
            }
            expression.stopRead(this);
        }
    }

    public void disconnectWriteExpression(Expression<?, ?> expression) {
        synchronized (lock) {
            if (!writeExpressions.remove(expression)) {
                log.log(Level.SEVERE, "Director was asked to disconnect expression '" + expression + "' which was not found.");
            }
            expression.stopWrite(this);
        }
    }

    private volatile boolean closed = false;

    void close() {
        closed = true;
        disconnect();
    }

    /**
     * Close and disconnects all the child expressions.
     */
    private void disconnect() {
        synchronized (lock) {
            while (!readExpressions.isEmpty()) {
                Expression<?, ?> expression = readExpressions.iterator().next();
                disconnectReadExpression(expression);
            }
        }
    }

    /**
     * Creates a new notifier. The new notifier will notifier the given pv
     * with new values calculated by the function, and will use onThread to
     * perform the notifications.
     * <p>
     * After construction, one MUST set the pvRecipe, so that the
     * dataSource is appropriately closed.
     *
     * @param pv the pv on which to notify
     */
    PVDirector(PVImpl<R, W> pv, PVConfiguration<R, W> pvConf) {
        this.pvRef = new WeakReference<PVImpl<R, W>>(pv);
        this.readFunction = pvConf.expression.getFunction();
        if (pvConf.mode == PVConfiguration.Mode.WRITE || pvConf.mode == PVConfiguration.Mode.READ_WRITE) {
            this.writeFunction = pvConf.expression.getWriteFunction();
        } else {
            this.writeFunction = null;
        }
        this.notificationExecutor = pvConf.notificationExecutor;
        this.scannerExecutor = pvConf.gpClient.dataProcessingThreadPool;
        this.dataSource = pvConf.dataSource;
        this.maxRate = pvConf.maxRate;
        if (pvConf.connectionTimeout != null) {
            this.readTimeout(pvConf.connectionTimeout, pvConf.connectionTimeoutMessage);
        }
    }

    /**
     * Determines whether the notifier is active or not.
     * <p>
     * The notifier becomes inactive if the PVReader is closed or is garbage collected.
     * The first time this function determines that the notifier is inactive,
     * it will ask the data source to close all channels relative to the
     * pv.
     *
     * @return true if new notification should be performed
     */
    private boolean isActive() {
        // Making sure to getValue the reference once for thread safety
        final PV<R, W> pv = pvRef.get();
        if (pv != null && !pv.isClosed()) {
            return true;
        } else if (pv == null && closed != true) {
            log.log(Level.WARNING, "Open PVReader/Writer was garbage collected: always keep a handle and close it. Disconnecting and cleaning up.", creationStackTrace);
            return false;
        } else {
            return false;
        }
    }

    void pause() {
        scanStrategy.pause();
    }

    void resume() {
        scanStrategy.resume();
    }

    private volatile boolean notificationInFlight = false;

    /**
     * Notifies the PVReader of a new value.
     */
    private void notifyPv(PVEvent event) {
        // This function should not be called when another even is in flight.
        // In principle, this should never happen since the scan rate
        // decoupler should not fire an event if the previous was not
        // concluded. For now, we have a safeguard to double check this never
        // happens.
        if (notificationInFlight) {
            log.log(Level.SEVERE, "Called notifyPV while an event was in flight");
            return;
        }

        Notification previousNotification = lastNotification.get();

        // Calculate new value if it is a value event
        R newValue = previousNotification != null ? previousNotification.readValue : null;
        if (event.isType(PVEvent.Type.VALUE)) {
            try {
                // Tries to calculate the value
                newValue = readFunction.get();
                if (newValue != null && !(newValue instanceof VType) && !(newValue instanceof List)) {
                    throw new RuntimeException("Notification is only currently supported for VTypes (was " + newValue + ")");
                }
            } catch (RuntimeException ex) {
                // Calculation failed
                event = event.removeType(PVEvent.Type.VALUE);
                event = event.addEvent(PVEvent.exceptionEvent(ex));
            } catch (Throwable ex) {
                log.log(Level.SEVERE, "Unrecoverable error during scanning", ex);
            }
        }

        // Calculate new connection if it is a connection value
        boolean newConnection = previousNotification != null ? previousNotification.readConnection : false;
        if (event.isType(PVEvent.Type.READ_CONNECTION)) {
            newConnection = calculateConnection();

            // If we are connected after a timeout, ignore the timeout
            if (newConnection && event.getException() instanceof TimeoutException) {
                event = event.removeType(PVEvent.Type.EXCEPTION);
            }
        }

        // Calculate new write connection if it is a connection value
        boolean newWriteConnection = previousNotification != null ? previousNotification.writeConnection : false;
        if (event.isType(PVEvent.Type.WRITE_CONNECTION)) {
            newWriteConnection = calculateWriteConnection();
        }

        // Don't repeat notifications
        if (previousNotification != null) {
            // Compare new connection
            if (event.isType(PVEvent.Type.READ_CONNECTION) && previousNotification.readConnection == newConnection) {
                event = event.removeType(PVEvent.Type.READ_CONNECTION);
            }

            if (event.isType(PVEvent.Type.WRITE_CONNECTION) && previousNotification.writeConnection == newWriteConnection) {
                event = event.removeType(PVEvent.Type.WRITE_CONNECTION);
            }

            if (event.isType(PVEvent.Type.VALUE) && previousNotification.readValue == newValue) {
                event = event.removeType(PVEvent.Type.VALUE);
            }

            Exception previousReadException = previousNotification.event.getException();
            Exception currentReadException = event.getException();
            if (event.isType(PVEvent.Type.EXCEPTION) && previousReadException != null && currentReadException != null &&
                    currentReadException.getClass().equals(previousReadException.getClass()) &&
                    Objects.equals(currentReadException.getMessage(), previousReadException.getMessage())) {
                event = event.removeType(PVEvent.Type.EXCEPTION);
            }
        } else {
            if (event.isType(PVEvent.Type.READ_CONNECTION) && newConnection == false) {
                event = event.removeType(PVEvent.Type.READ_CONNECTION);
            }
            if (event.isType(PVEvent.Type.WRITE_CONNECTION) && newWriteConnection == false) {
                event = event.removeType(PVEvent.Type.WRITE_CONNECTION);
            }
            if (event.isType(PVEvent.Type.VALUE) && newValue == null) {
                event = event.removeType(PVEvent.Type.VALUE);
            }
        }

        if (event.getType().isEmpty()) {
            scanStrategy.readyForNextEvent();
            return;
        }

        // Prepare values to ship to the other thread.
        lastNotification.set(new Notification(newValue, newConnection, newWriteConnection, event));

        notificationInFlight = true;
        notificationExecutor.execute(new Runnable() {

            public void run() {
                try {
                    PVImpl<R, W> pv = pvRef.get();
                    Notification notification = lastNotification.get();
                    // Proceed with notification only if the PV was not garbage
                    // collected
                    if (pv != null) {

                        // Atomicity guaranteed by:
                        //  - all the modification on the PV
                        //    are done here, on the same thread where the listeners will be called.
                        //    This means the callbacks are guaranteed to run after all
                        //    changes are done.
                        //  - rate decoupler will make sure that a new notification
                        //    will start only the previous is finished. This means
                        //    the next event is serialized after the end of this one.
                        //    notificationInFlight double checks this for now and
                        //    can be removed in a second phase.

                        pv.fireEvent(notification.event, notification.readConnection, notification.writeConnection, notification.readValue);
                    }
                } finally {
                    notificationInFlight = false;
                    scanStrategy.readyForNextEvent();
                }
            }
        });
    }

    /**
     * Posts a readTimeout exception in the exception queue.
     *
     * @param timeoutMessage the message for the readTimeout
     */
    private void processReadTimeout(String timeoutMessage) {
        PVImpl<R, W> pv = pvRef.get();
        if (pv != null && !pv.isConnected()) {
            scanStrategy.getUpdateListener().accept(PVEvent.exceptionEvent(new TimeoutException(timeoutMessage)));
        }
    }

    private void readTimeout(Duration timeout, final String timeoutMessage) {
        scannerExecutor.schedule(new Runnable() {
            public void run() {
                processReadTimeout(timeoutMessage);
            }
        }, timeout.getMillis()*1000000, TimeUnit.NANOSECONDS);
    }

    private final Consumer<PVEvent> desiredRateEventListener = new Consumer<PVEvent>() {
        @Override
        public void accept(PVEvent event) {
            try {
                if (isActive()) {
                    notifyPv(event);
                } else {
                    close();
                }
            } catch (Exception ex) {
                log.log(Level.SEVERE, "GPClient fatal error", ex);
            }
        }
    };

    Consumer<PVEvent> getDesiredRateEventListener() {
        return desiredRateEventListener;
    }

    private static final Random rand = new Random();

    /**
     * Implements the asynchronous write. Starts the process for
     * writing the value so that the result will be notified on the given
     * callback.
     *
     * @param value    the value to be written; can be null
     * @param callback the callback for the write result; if null, notify
     *                 with the other events
     */
    void submitWrite(final W value, Consumer<PVEvent> callback) {
        if (writeFunction == null) {
            throw new IllegalStateException("This pv is read only");
        }

        final Consumer<PVEvent> finalCallback;
        if (callback == null) {
            finalCallback = scanStrategy.getUpdateListener();
        } else {
            finalCallback = callback;
        }

        // TODO: the current implementation writes all values. We may want to skip if there is a
        // write burst.

        // Design for the write communication
        //
        // The write value is in general something that will be decomposed into
        // the actual write values for each channel (i.e. a key/value pair of
        // channel names and channel values). Also, not all the channel will
        // need to be actually writte (i.e. some keys/values are not added).
        // Therefore the pvDirector breaks down the write in three phases:
        // - prepare all collectors to receive a value
        // - write the value, which the expression logic will decompose and write
        //   into each WriteCollector
        // - tell all collectors to send the write request
        // This division allows the logic of how the write is decomposed to the
        // different channel to be still general while we can still keep track
        // of which channel write corresponds to the value being written.
        // Once the write requests are sent, the WriteTab collects the write
        // results for each channel and constructs a single event for the
        // write callback.

        scannerExecutor.execute(new Runnable() {
            public void run() {
                try {
                    // Take a copy of the write collectors so that if there is a dynamic
                    // change of the connected expressions we at least have a stable
                    // snapshot
                    Set<WriteCollector<?>> writeCollectorsCopy;
                    synchronized (lock) {
                        writeCollectorsCopy = new HashSet<WriteCollector<?>>(writeCollectors);

                        int id = rand.nextInt();
                        for (WriteCollector<?> writeCollector : writeCollectorsCopy) {
                            writeCollector.prepareWrite(id);
                        }

                        // Use writeFunction to prepare the values in the WriteCollectors.
                        // If writeFunction fails, clear the collectors and return failure.
                        try {
                            writeFunction.accept(value);
                        } catch (Exception ex) {
                            for (WriteCollector<?> writeCollector : writeCollectorsCopy) {
                                writeCollector.cancelWrite(id);
                            }
                            finalCallback.accept(PVEvent.writeFailedEvent(ex));
                            return;
                        }

                        WriteTab tab = new WriteTab(writeCollectorsCopy.size(), finalCallback);
                        for (WriteCollector<?> writeCollector : writeCollectorsCopy) {
                            writeCollector.sendWriteRequest(id, tab);
                        }
                    }
                } catch (Exception ex) {
                    log.log(Level.SEVERE, "Error while processing write", ex);
                }
            }
        });
    }

    private class WriteTab extends Consumer<PVEvent> {
        private final Object lock = new Object();
        private int counter;
        private boolean done;
        private final Consumer<PVEvent> callback;

        public WriteTab(int nCalls, Consumer<PVEvent> callback) {
            this.callback = callback;
            this.counter = nCalls;
        }

        @Override
        public void accept(PVEvent event) {
            synchronized (lock) {
                // If we are done, we ignore incoming events
                if (done) {
                    return;
                }

                if (event.isType(PVEvent.Type.WRITE_SUCCEEDED)) {
                    // Decerement counter. If we are not zero we have nothing to do
                    counter--;
                    int value = counter;
                    if (value != 0) {
                        return;
                    }
                }

                // If we haven't returned, then this event should be sent, and
                // no other shoule be sent after this
                done = true;
            }

            callback.accept(event);
        }


    }

}
