/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.util.stats.TimeInterval;
import org.joda.time.Duration;
import org.joda.time.Instant;

import java.util.List;
import java.util.logging.Logger;

/**
 * Base class for all simulated signals.
 * <p>
 * The values will be calculated always on the same thread. This means that
 * each simulation does not need to synchronize its state.
 *
 * @author carcassi
 */
abstract class Simulation<T> {

    private static final Logger log = Logger.getLogger(Simulation.class.getName());

//    private final long intervalBetweenExecution;
//    private final Class<T> classToken;
    private volatile Instant lastTime = Instant.now();

    /**
     * Creates a new simulation.
     *
     */
    Simulation(Duration scanRate, Class<T> classToken) {
        // XXX: this is dead code at this point. If needed, use it when setting up
        // the scan job. Ignore otherwise.
        if (scanRate.compareTo(Duration.millis(1)) < 0) {
            throw new IllegalArgumentException("Scans must be at least every ms (was " + scanRate + ")");
        }
//        this.intervalBetweenExecution = Math.max(scanRate.toNanos() / 1000000, 1);
//        this.classToken = classToken;
    }

    /**
     * Computes all the new values in the given time slice.
     *
     * @param interval the interval where the data should be generated
     * @return the new values
     */
    abstract List<T> createValues(TimeInterval interval);

    final void reset() {
        this.lastTime = resetTime();
    }

    Instant resetTime() {
        return Instant.now();
    }

    List<T> createValuesBefore(Instant newTime) {
        List<T> newValues = createValues(TimeInterval.between(lastTime, newTime));
        this.lastTime = newTime;
        return newValues;
    }
}
