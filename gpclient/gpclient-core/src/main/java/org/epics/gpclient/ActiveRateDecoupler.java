/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Decouples the rate by simply scanning the PV status at the given rate.
 * <p>
 * This type of scanning is necessary if there is time dependent computation
 * performed on the data after it's gathered in the collectors.
 *
 * @author carcassi
 */
class ActiveRateDecoupler extends RateDecoupler {
    
    private volatile ScheduledFuture<?> scanTaskHandle;

    public ActiveRateDecoupler(ScheduledExecutorService scannerExecutor,
            Duration maxDuration, Consumer<PVEvent> listener, Consumer<Exception> exceptionHandler) {
        super(scannerExecutor, maxDuration, listener, exceptionHandler);
    }

    @Override
    void onStart() {
        scanTaskHandle = getScannerExecutor().scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                if (!isStopped() && !isPaused() && !isEventProcessing()) {
                    PVEvent event = PVEvent.readConnectionValueEvent();
                    sendDesiredRateEvent(event);
                }
            }
        }, 0, getMaxDuration().toNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    void onStop() {
        if (scanTaskHandle != null) {
            scanTaskHandle.cancel(false);
            scanTaskHandle = null;
        } else {
            throw new IllegalStateException("Scan was never started");
        }
    }

    @Override
    protected void newEvent(PVEvent event) {
        // Do nothing
    }
    
    /**
     * If possible, submit the event right away, otherwise try again later.
     * @param event the event to submit
     */
    private void scheduleWriteOutcome(final PVEvent event) {
        if (!isEventProcessing()) {
            sendDesiredRateEvent(event);
        } else {
            getScannerExecutor().submit(new Runnable() {

                @Override
                public void run() {
                    scheduleWriteOutcome(event);
                }
            });
        }
    }
    
}
