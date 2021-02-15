/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import org.epics.util.compat.legacy.functional.Consumer;
import org.joda.time.Duration;

import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represent a strategy to decouple desired rate events from source rate
 * events.
 *
 * @author carcassi
 */
abstract class RateDecoupler {

    private static final Logger log = Logger.getLogger(RateDecoupler.class.getName());
    private final Consumer<PVEvent> listener;
    private final Consumer<Exception> exceptionHandler;
    private final ScheduledExecutorService scannerExecutor;
    private final Duration maxDuration;

    protected final Object lock = new Object();
    private boolean eventProcessing = false;
    private boolean paused = false;
    private boolean stopped = false;

    /**
     * Creates a new rate decoupler that will send the events to the
     * given listener.
     *
     * @param scannerExecutor executor for the scanner tasks
     * @param maxDuration max interval between notifications
     * @param listener the event callback
     * @param exceptionHandler the exception handler
     */
    public RateDecoupler(ScheduledExecutorService scannerExecutor, Duration maxDuration,
            Consumer<PVEvent> listener, Consumer<Exception> exceptionHandler) {
        this.listener = listener;
        this.exceptionHandler = exceptionHandler;
        this.scannerExecutor = scannerExecutor;
        this.maxDuration = maxDuration;
    }

    public ScheduledExecutorService getScannerExecutor() {
        return scannerExecutor;
    }

    public Duration getMaxDuration() {
        return maxDuration;
    }

    /**
     * Starts the scanning. From this moment on, source rate events
     * may trigger desired rate events.
     */
    public final void start() {
        onStart();
    }

    /**
     * Initialization to be done when the pv is started.
     * <p>
     * Empty implementation to be overridden.
     */
    void onStart() {
    }

    /**
     * Pause the scanning. Events will be collected and delayed until a resume.
     */
    public final void pause() {
        synchronized(lock) {
            if (paused) {
                log.warning("Pausing an already paused scanner");
            }
            paused = true;
        }
    }

    /**
     * Resumes the scanning. If events were collected during the pause,
     * they will be sent right away.
     */
    public final void resume() {
        synchronized(lock) {
            if (!paused) {
                log.warning("Resuming a non paused scanner");
            }
            paused = false;
        }
        onResume();
    }

    /**
     * Task to be executed on pv resume.
     * <p>
     * Empty task to be overridden.
     */
    void onResume() {
    }

    /**
     * Stops the scanning. From this moment on, the pv will no longer be
     * notified. Can't be restarted.
     */
    final void stop() {
        synchronized(lock) {
            stopped = true;
        }
        onStop();
    }

    /**
     * Cleanup to be done when the pv is stopped.
     * <p>
     * Empty implementation to be overridden.
     */
    void onStop() {
    }

    private final Consumer<PVEvent> updateListener = new Consumer<PVEvent>() {
        @Override
        public void accept(PVEvent event) {
            newEvent(event);
            if (exceptionHandler != null && event.getException() != null) {
                try {
                    exceptionHandler.accept(event.getException());
                } catch(Exception ex) {
                    log.log(Level.SEVERE, "Exception handler " + exceptionHandler + " should not generate exceptions", ex);
                }
            }
        }

    };

    /**
     * Returns the listener on which to notify the new events from the
     * collectors.
     *
     * @return the listener for the events
     */
    public final Consumer<PVEvent> getUpdateListener() {
        return updateListener;
    }

    protected abstract void newEvent(PVEvent event);

    /**
     * Call when a new event should be triggered at the desired rate.
     * After calling this method, one should wait for the next {@link #readyForNextEvent() }
     * before calling it again.
     */
    final void sendDesiredRateEvent(PVEvent event) {
        synchronized(lock) {
            if (isEventProcessing()) {
                throw new RuntimeException("Previous event still in flight");
            }
            eventProcessing = true;
        }
        listener.accept(event);
    }

    /**
     * Called after a pv is notified. Once {@link #sendDesiredRateEvent(org.epics.gpclient.PVEvent) }
     * is called, it should not be called again before this method is called.
     */
    public final void readyForNextEvent() {
        synchronized(lock) {
            if (!isEventProcessing()) {
                log.warning("Event processing is done, but no event was in flight");
            }
            eventProcessing = false;
        }
        onDesiredEventProcessed();
    }

    /**
     * Called after an event was successfully processed.
     * <p>
     * Empty implementation to be overridden.
     */
    void onDesiredEventProcessed() {
    }

    /**
     * True if an event was sent, but the ready for next event wasn't received.
     *
     * @return ture if there is still an event in-flight
     */
    public boolean isEventProcessing() {
        synchronized(lock) {
            return eventProcessing;
        }
    }

    /**
     * Whether the scanning is currently paused.
     * @return true if paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Whether the scanning is currently stopped.
     * @return true if stopped
     */
    public boolean isStopped() {
        return stopped;
    }

}
