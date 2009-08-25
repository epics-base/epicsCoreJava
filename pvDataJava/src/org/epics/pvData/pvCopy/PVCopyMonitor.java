/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;

import org.epics.pvData.misc.BitSet;


/**
 * PVCopyMonitor is a PVListener for the PVRecord to which PVCopy is attached.
 * It updates two bitSets when it receives PVListener.dataPut callbacks.
 * changeBit shows all fields that have changed between calls to updateCopy.
 * overrunBitSet shows all fields that have changed value more than once between calls
 * to updateCopy.
 * It synchronizes on changeBitSet when it accesses the two bitSets.
 * It notifies the PVCopyMonitorRequester when data has changed.
 * The caller can use this for a queue of monitors, a shared PVStructure,
 * or just a single non-shared PVStructure.
 * @author mrk
 *
 */
public interface PVCopyMonitor {
    /**
     * Start monitoring.
     * Monitoring without bitsets is for clients that just want to know when monitored fields have changed.
     */
    void startMonitoring();
    /**
     * Start monitoring.
     * @param changeBitSet The initial changeBitSet.
     * @param overrunBitSet The overrun bitSet.
     */
    void startMonitoring(BitSet changeBitSet, BitSet overrunBitSet);
    /**
     * Stop monitoring.
     */
    void stopMonitoring();
    /**
     * The bitSets are replaced by the new bitSets.
     * Note that a client needs just two instances of the bitSets and can just cycle between the
     * two sets.
     * Even if the PVStructure is shared this method is important since the caller will not miss data changes.
     * @param newChangeBitSet The new changeBitSet.
     * @param newOverrunBitSet The new overrun bitSert.
     * @param lockRecord This should be true unless the caller knows that the record is
     * already locked.
     */
    void switchBitSets(BitSet newChangeBitSet,BitSet newOverrunBitSet, boolean lockRecord);
}
