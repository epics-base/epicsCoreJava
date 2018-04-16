/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource.sim;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import org.epics.gpclient.datasource.ChannelHandler;
import org.epics.gpclient.datasource.DataSource;
import static org.epics.util.concurrent.Executors.namedPool;

/**
 * Data source to produce simulated signals that can be using during development
 * and testing.
 *
 * @author carcassi
 */
public final class SimulationDataSource extends DataSource {

    public SimulationDataSource() {
        super(false);
    }

    /**
     * Data source instance.
     *
     * @return the data source instance
     */
    public static DataSource simulatedData() {
        return SimulationDataSource.instance;
    }

    private static final Logger log = Logger.getLogger(SimulationDataSource.class.getName());
    static final SimulationDataSource instance = new SimulationDataSource();

    /**
     * ExecutorService on which all simulated data is generated.
     */
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(namedPool("PVMgr Simulator "));

    @Override
    @SuppressWarnings("unchecked")
    protected ChannelHandler createChannel(String channelName) {
        if (channelName.startsWith("const(")) {
            return new ConstantChannelHandler(channelName);
        }
        if (channelName.startsWith("delayedConnectionChannel(")) {
            return new DelayedConnectionChannelHandler(channelName, exec);
        }
        if (channelName.startsWith("intermittentChannel(")) {
            return new IntermittentChannelHandler(channelName, exec);
        }
        
        SimFunction<?> simFunction = (SimFunction<?>) NameParser.createFunction(channelName);
        return new SimulationChannelHandler(channelName, simFunction, exec);
    }

    @Override
    public void close() {
        exec.shutdownNow();
        super.close();
    }

}