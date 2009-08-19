/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor;


/**
 * Interface for Monitor.
 * @author mrk
 *
 */
public interface Monitor  {
    /**
     * Start monitoring.
     */
    void start();
    /**
     * Stop Monitoring.
     */
    void stop();
    /**
     * Get original data.
     */
    void get();
    /**
     * If monitor has occurred return data.
     * @return monitorElement for modified data on null if no monitors have occurred.
     */
    MonitorElement poll();
    /**
     * Release a MonitorElement that was returned by poll.
     * @param monitorElement
     */
    void release(MonitorElement monitorElement);
}
