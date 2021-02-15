/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.gpclient.datasource.MultiplexedChannelHandler;
import org.joda.time.Instant;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Takes a simulation object and use it as a source of data for the channel.
 *
 * @author carcassi
 */
class SimulationChannelHandler<T> extends MultiplexedChannelHandler<Simulation<T>, T> {

    /**
     * Creates a new simulation channel.
     *
     * @param channelName the name of the channel
     * @param simulation the source of the simulated data
     * @param exec the thread pool to use for data generation
     */
    SimulationChannelHandler(String channelName, Simulation<T> simulation, ScheduledExecutorService exec) {
        super(channelName, true);
        this.simulation = simulation;
        this.exec = exec;
    }

    private final Simulation<T> simulation;
    private final ScheduledExecutorService exec;
    private final Runnable task = new Runnable() {

        public void run() {
            // Protect the timer thread for possible problems.
            try {
                // Creates all the new vlues and process them one by one
                List<T> newValues = simulation.createValuesBefore(Instant.now());
                for (T newValue : newValues) {
                    processMessage(newValue);
                }
            } catch (Exception ex) {
                log.log(Level.WARNING, "Data simulation problem", ex);
            }
        }
    };
    private static final Logger log = Logger.getLogger(SimulationChannelHandler.class.getName());
    private ScheduledFuture<?> taskFuture;

    @Override
    public void connect() {
        simulation.reset();
//        simulation.lastTime = Instant.now();
//        if (simulation instanceof SimFunction) {
//            simulation.lastTime = simulation.lastTime.minus(((SimFunction<?>) simulation).getTimeBetweenSamples());
//        }
        taskFuture = exec.scheduleWithFixedDelay(task, 0, 10, TimeUnit.MILLISECONDS);
        processConnection(simulation);
    }

    @Override
    public void disconnect() {
        taskFuture.cancel(false);
        taskFuture = null;
        processConnection(null);
    }

    @Override
    public void write(Object newValue) {
        throw new UnsupportedOperationException("Can't write to simulation channel.");
    }

    @Override
    public boolean isConnected(Simulation<T> sim) {
        return taskFuture != null;
    }
}
