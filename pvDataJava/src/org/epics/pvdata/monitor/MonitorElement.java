/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.monitor;

import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.PVStructure;

/**
 * A monitorElement.
 * If holds the data for a monitor and change and overflow bitSets.
 * @author mrk
 *
 */
public interface MonitorElement {
    /**
     * Get the PVStructure.
     *
     * @return The PVStructure.
     */
    PVStructure getPVStructure();

    /**
     * Get the bitSet showing which fields have changed.
     *
     * @return The bitSet.
     */
    BitSet getChangedBitSet();

    /**
     * Get the bitSet showing which fields have been changed more than once.
     *
     * @return The bitSet.
     */
    BitSet getOverrunBitSet();
}
