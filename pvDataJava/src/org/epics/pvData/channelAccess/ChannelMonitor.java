/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

import java.util.BitSet;

/**
 * @author mrk
 *
 */
public interface ChannelMonitor {
    /**
     * Get the BitSet which describes which fields have been modified since the last monitor.
     * @return The BitSet.
     */
    BitSet getBitSet();
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
