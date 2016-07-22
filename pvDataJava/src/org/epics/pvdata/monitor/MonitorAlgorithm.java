/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.monitor;

/**
 * The algorithm for monitoring.
 * @author mrk
 *
 */
public interface MonitorAlgorithm {
    /**
     * Get the name of the algorithm.
     *
     * @return the name
     */
    String getAlgorithmName();

    /**
     * Should the current value cause a monitor?
     *
     * @return (false,true) if a monitor (should not, should) be raised
     */
    boolean causeMonitor();

    /**
     * A monitor was raised. The implementation should update to the latest value.
     */
    void monitorIssued();
}
