/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
     * @return The PVStructure.
     */
    PVStructure getPVStructure();
    /**
     * Get the bitSet showing which fields have changed.
     * @return The bitSet.
     */
    BitSet getChangedBitSet();
    /**
     * Get the bitSet showing which fields have been changed more than once.
     * @return The bitSet.
     */
    BitSet getOverrunBitSet();
}
