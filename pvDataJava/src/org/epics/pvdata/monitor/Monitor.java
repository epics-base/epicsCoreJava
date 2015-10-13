/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.monitor;

import org.epics.pvdata.misc.Destroyable;
import org.epics.pvdata.pv.Status;


/**
 * Interface for Monitor.
 * @author mrk
 *
 */
public interface Monitor extends Destroyable {
    /**
     * Start monitoring.
     *
     * @return completion status
     */
    Status start();

    /**
     * Stop Monitoring.
     *
     * @return completion status
     */
    Status stop();

    /**
     * If monitor has occurred return data.
     *
     * @return monitorElement for modified data on null if no monitors have occurred
     */
    MonitorElement poll();

    /**
     * Release a MonitorElement that was returned by poll.
     *
     * @param monitorElement the MonitorElement to release
     */
    void release(MonitorElement monitorElement);
}
