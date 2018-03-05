/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.expression;

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
class ActiveScanDecoupler extends SourceDesiredRateDecoupler {
    
    private volatile ScheduledFuture<?> scanTaskHandle;

    public ActiveScanDecoupler(ScheduledExecutorService scannerExecutor,
            Duration maxDuration, Consumer<ReadEvent> listener) {
        super(scannerExecutor, maxDuration, listener);
    }

    @Override
    void onStart() {
        scanTaskHandle = getScannerExecutor().scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                if (!isStopped() && !isPaused() && !isEventProcessing()) {
                    ReadEvent event = ReadEvent.connectionValueEvent();
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

    private final Consumer<ReadEvent> updateListener = new Consumer<ReadEvent>() {
        @Override
        public void accept(ReadEvent t) {
            // Do nothing
        }
        
    };
    
    @Override
    Consumer<ReadEvent> getUpdateListener() {
        return updateListener;
    }
    
//    @Override
//    void newWriteSuccededEvent() {
//        DesiredRateEvent event = new DesiredRateEvent();
//        event.addType(DesiredRateEvent.Type.WRITE_SUCCEEDED);
//        scheduleWriteOutcome(event);
//    }
//
//    @Override
//    void newWriteFailedEvent(Exception ex) {
//        DesiredRateEvent event = new DesiredRateEvent();
//        event.addWriteFailed(new RuntimeException());
//        sendDesiredRateEvent(event);
//    }
    
    /**
     * If possible, submit the event right away, otherwise try again later.
     * @param event the event to submit
     */
    private void scheduleWriteOutcome(final ReadEvent event) {
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
