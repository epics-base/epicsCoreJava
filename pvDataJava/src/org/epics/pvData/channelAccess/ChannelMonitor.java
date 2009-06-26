/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;


/**
 * Interface for ChannelMonitor.
 * @author mrk
 *
 */
public interface ChannelMonitor {
    /**
     * Start monitoring.
     */
    void start();
    /**
     * Stop Monitoring.
     */
    void stop();
    /**
     * Destroy the ChannelMonitor;
     */
    void destroy();
}
