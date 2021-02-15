/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import org.epics.util.compat.legacy.functional.Consumer;
import org.joda.time.Duration;
import org.joda.time.Instant;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author carcassi
 */
class PassiveRateDecoupler extends RateDecoupler {

    private static final Logger log = Logger.getLogger(PassiveRateDecoupler.class.getName());
    // TODO: this could be made configurable between FINEST and OFF, and the if
    // modified so that code elimination would remove the logging completely
    private static final Level logLevel = Level.FINEST;

    private PVEvent queuedEvent;
    private Instant lastSubmission;
    private boolean scanActive;

    public PassiveRateDecoupler(ScheduledExecutorService scannerExecutor,
                                Duration maxDuration, Consumer<PVEvent> listener, Consumer<Exception> exceptionHandler) {
        super(scannerExecutor, maxDuration, listener, exceptionHandler);
        synchronized (lock) {
            lastSubmission = Instant.now().minus(getMaxDuration());
        }
    }

    private final Runnable notificationTask = new Runnable() {

        public void run() {
            PVEvent nextEvent;
            synchronized (lock) {
                nextEvent = queuedEvent;
                queuedEvent = null;
                if (log.isLoggable(logLevel)) {
                    log.log(logLevel, "Submitted {0}", Instant.now());
                }
            }

            // If stopped, the event may be null. Skip the event.
            if (nextEvent != null) {
                sendDesiredRateEvent(nextEvent);
            } else {
                log.log(logLevel, "Skipping null event {0}", Instant.now());
            }
        }
    };

    @Override
    void onStart() {
        // XXX: This should not be needed at this point. The collector
        // Should take care of that.
        // When starting, send an event in case the expressions
        // are constants
        //newEvent(DesiredRateEvent.Type.READ_CONNECTION);
    }

    @Override
    void onStop() {
        synchronized (lock) {
            queuedEvent = null;
        }
    }

    @Override
    void onResume() {
        onDesiredEventProcessed();
    }

//    void newWriteSuccededEvent() {
//        DesiredRateEvent event = new DesiredRateEvent();
//        event.addType(DesiredRateEvent.Type.WRITE_SUCCEEDED);
//        scheduleWriteOutcome(event);
//    }
//
//    void newWriteFailedEvent(Exception ex) {
//        DesiredRateEvent event = new DesiredRateEvent();
//        event.addWriteFailed(new RuntimeException());
//        sendDesiredRateEvent(event);
//    }

    @Override
    void onDesiredEventProcessed() {
        Duration delay = null;
        synchronized (lock) {
            // If an event is pending submit it
            if (queuedEvent != null) {
                Instant nextSubmission = lastSubmission.plus(getMaxDuration());
                delay = Duration.millis(nextSubmission.minus(Instant.now().getMillis()).getMillis());
                if (delay.abs().equals(delay)) { // Positive
                    lastSubmission = nextSubmission;
                    if (log.isLoggable(logLevel)) {
                        log.log(logLevel, "Schedule next {0}", Instant.now());
                    }
                } else {
                    lastSubmission = Instant.now();
                    if (log.isLoggable(logLevel)) {
                        log.log(logLevel, "Schedule now {0}", Instant.now());
                    }
                }
            } else {
                scanActive = false;
                if (log.isLoggable(logLevel)) {
                    log.log(logLevel, "Do not schedule next {0}", Instant.now());
                }
            }
        }

        if (delay != null) {
            scheduleNext(delay);
        }
    }

    @Override
    protected void newEvent(PVEvent event) {
        boolean submit;
        Duration delay = null;

        synchronized (lock) {
            // Add event to the queue
            if (queuedEvent == null) {
                queuedEvent = event;
                if (log.isLoggable(logLevel)) {
                    log.log(logLevel, "Creating queued event {0}", Instant.now());
                }
            } else {
                queuedEvent = queuedEvent.addEvent(event);
            }

            // If scan is not active, submit the next scan
            if (!scanActive && !isPaused()) {
                submit = true;
                Instant currentTimestamp = Instant.now();
                Instant nextTimeSlot = lastSubmission.plus(getMaxDuration());
                if (currentTimestamp.compareTo(nextTimeSlot) < 0) {
                    delay = Duration.millis(nextTimeSlot.minus(currentTimestamp.getMillis()).getMillis());
                    lastSubmission = nextTimeSlot;
                    if (log.isLoggable(logLevel)) {
                        log.log(logLevel, "Submit delayed {0}", Instant.now());
                    }
                } else {
                    lastSubmission = currentTimestamp;
                    if (log.isLoggable(logLevel)) {
                        log.log(logLevel, "Submit now {0}", Instant.now());
                    }
                }
                scanActive = true;
            } else {
                submit = false;
                if (log.isLoggable(logLevel)) {
                    log.log(logLevel, "Do not submit {0}", Instant.now());
                }
            }

        }
        if (submit) {
            scheduleNext(delay);
        }
    }

    private void scheduleNext(Duration delay) {
        if (delay == null || !delay.abs().equals(delay)) { // null or negative
            getScannerExecutor().submit(notificationTask);
        } else {
            getScannerExecutor().schedule(notificationTask, delay.getMillis() * 1000000, TimeUnit.NANOSECONDS);
        }
    }


    /**
     * If possible, submit the event right away, otherwise try again later.
     *
     * @param event the event to submit
     */
    private void scheduleWriteOutcome(final PVEvent event) {
        if (!isEventProcessing()) {
            sendDesiredRateEvent(event);
        } else {
            getScannerExecutor().submit(new Runnable() {

                public void run() {
                    scheduleWriteOutcome(event);
                }
            });
        }
    }

}
