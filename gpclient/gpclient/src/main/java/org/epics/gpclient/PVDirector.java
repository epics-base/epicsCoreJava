/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.gpclient.datasource.DataSource;
import org.epics.gpclient.expression.Expression;
import org.epics.gpclient.expression.ReadExpression;
import org.epics.vtype.VType;

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
 * @author carcassi
 * @param <R> the read object type
 * @param <W> the write object type
 */
public class PVDirector<R, W> {
    
    private static final Logger log = Logger.getLogger(PVDirector.class.getName());

    // Required for connection and exception notification

    /** Executor used to notify of new values/connection/exception */
    final Executor notificationExecutor;
    /** Executor used to scan the connection/exception queues */
    final ScheduledExecutorService scannerExecutor;
    /** PVReader to update during the notification */
    private final WeakReference<PVImpl<R, W>> pvRef;
    /** Function for the new value */
    final Supplier<R> readFunction;
    /** Creation for stack trace */
    private final Exception creationStackTrace = new Exception("PV was never closed (stack trace for creation)");
    /** Used to ignore duplicated errors */
    private final AtomicReference<Notification> lastNotification = new AtomicReference<>();
    /** Maximum rate for notification */
    final Duration maxRate;
    
    private class Notification {
        final R readValue;
        final boolean readConnection;
        final PVEvent event;

        Notification(R readValue, boolean readConnection, PVEvent event) {
            this.readValue = readValue;
            this.readConnection = readConnection;
            this.event = event;
        }

        @Override
        public String toString() {
            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("event", event);
            properties.put("readConnection", readConnection);
            properties.put("readValue", readValue);
            return properties.toString();
        }
    }
    
    private RateDecoupler scanStrategy;

    
    // Required to connect/disconnect expressions
    private final DataSource dataSource;
    private final Object lock = new Object();
    private final Set<Expression<?, ?>> readExpressions = new HashSet<>();
    private final Set<Expression<?, ?>> writeExpressions = new HashSet<>();

    // Required for multiple operations
    /** Collector required to connect/disconnect expressions and for connection calculation */
    private final Set<ReadCollector<?,?>> readCollectors = new HashSet<>();
    /** Collector required to connect/disconnect expressions and for connection calculation */
    private final Set<WriteCollector<?>> writeCollectors = new HashSet<>();
    
    void setScanner(final RateDecoupler scanStrategy) {
        synchronized(lock) {
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
        synchronized(lock) {
            boolean connection = true;
            for (ReadCollector<?, ?> readCollector : readCollectors) {
                connection = connection && readCollector.getConnection();
            }
            return connection;
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
        synchronized(lock) {
            expression.startRead(this);
            readExpressions.add(expression);
        }
    }

    public void connectWriteExpression(Expression<?, ?> expression) {
        synchronized(lock) {
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
        synchronized(lock) {
            if (!readExpressions.remove(expression)) {
                log.log(Level.SEVERE, "Director was asked to disconnect expression '" + expression + "' which was not found.");
            }
            expression.stopRead(this);
        }
    }

    public void disconnectWriteExpression(Expression<?, ?> expression) {
        synchronized(lock) {
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
        synchronized(lock) {
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
     * @param function the function used to calculate new values
     * @param notificationExecutor the thread switching mechanism
     */
    PVDirector(PVImpl<R, W> pv, PVConfiguration<R, W> pvConf) {
        this.pvRef = new WeakReference<>(pv);
        this.readFunction = pvConf.expression.getFunction();
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
            log.log(Level.WARNING, "PVReader wasn't properly closed and it was garbage collected. Closing the associated connections...", creationStackTrace);
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
                if (newValue != null && !(newValue instanceof VType)) {
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

        // Don't repeat notifications
        if (previousNotification != null) {
            // Compare new connection
            if (event.isType(PVEvent.Type.READ_CONNECTION) && previousNotification.readConnection == newConnection) {
                event = event.removeType(PVEvent.Type.READ_CONNECTION);
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
            if (event.isType(PVEvent.Type.VALUE) && newValue == null) {
                event = event.removeType(PVEvent.Type.VALUE);
            }
        }
        
        if (event.getType().isEmpty()) {
            scanStrategy.readyForNextEvent();
            return;
        }
        
        // Prepare values to ship to the other thread.
        lastNotification.set(new Notification(newValue, newConnection, event));
        
        notificationInFlight = true;
        notificationExecutor.execute(new Runnable() {

            @Override
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
                        
                        if (notification.event.isType(PVEvent.Type.READ_CONNECTION) && notification.event.isType(PVEvent.Type.VALUE)) {
                            pv.fireConnectionValueUpdate(notification.event, notification.readConnection, notification.readValue);
                        } else if (notification.event.isType(PVEvent.Type.READ_CONNECTION)) {
                            pv.fireConnectionUpdate(notification.event, notification.readConnection);
                        } else if (notification.event.isType(PVEvent.Type.VALUE)) {
                            pv.fireValueUpdate(notification.event, notification.readValue);
                        } else {
                            pv.fireEvent(notification.event);
                        }
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
            @Override
            public void run() {
                processReadTimeout(timeoutMessage);
            }
        }, timeout.toNanos(), TimeUnit.NANOSECONDS);
    }
    
    private final Consumer<PVEvent> desiredRateEventListener = (PVEvent event) -> {
        try {
            if (isActive()) {
                notifyPv(event);
            } else {
                close();
            }
        } catch(Exception ex) {
            log.log(Level.SEVERE, "GPClient fatal error", ex);
        }
    };

    Consumer<PVEvent> getDesiredRateEventListener() {
        return desiredRateEventListener;
    }
    
    
}
