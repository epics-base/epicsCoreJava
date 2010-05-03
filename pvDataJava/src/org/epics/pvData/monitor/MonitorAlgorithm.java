/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor;

/**
 * The algorithm for monitoring.
 * @author mrk
 *
 */
public interface MonitorAlgorithm {
    /**
     * Get the name of the algorithm.
     * @return The name.
     */
    String getAlgorithmName();
    /**
     * Should the current value cause a monitor?
     * @return (false,true) if a monitor (should not, should) be raised.
     */
    boolean causeMonitor();
    /**
     * A monitor was raised. The implementation should update to the latest value.
     */
    void monitorIssued();
}
