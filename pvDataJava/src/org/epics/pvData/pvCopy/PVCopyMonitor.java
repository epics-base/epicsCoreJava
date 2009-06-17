/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;
import java.util.BitSet;

import org.epics.pvData.pv.PVStructure;

/**
 * PVCopyMonitor is a PVListener for the PVRecord to which PVCopy is attached.
 * It updates two bitSets when it receives PVListener.dataPut callbacks.
 * changeBit shows all fields that have changed between calls to updateCopy.
 * overrunBitSet shows all fields that have changed value more than once between calls
 * to updateCopy.
 * It notifies the PVCopyMonitorRequester when data has changed.
 * @author mrk
 *
 */
public interface PVCopyMonitor {
    /**
     * Start monitoring.
     * @param pvStructure The PVStructure that holds the monitored data.
     * @param changeBitSet The initial changeBitSet.
     * @param overrunBitSet The overrun bitSet.
     */
    void startMonitoring(PVStructure pvStructure, BitSet changeBitSet, BitSet overrunBitSet);
    /**
     * Stop monitoring.
     */
    void stopMonitoring();
    /**
     * If the pvStructure is not shared all fields that changeBitSet shows were changed are
     * copied from the corresponding PVField of the PVRecord to the PVField of the PVStructure.
     * Then the bitSets are replaced by the new bitSets.
     * Note that a client needs just two instances of the bitSets and can just cycle between the
     * two sets.
     * Even if the PVStructure is shared this method is important since the caller will not miss data changes.
     * @param newChangeBitSet The new changeBitSet.
     * @param newOverrunBitSet The new overrun bitSert.
     * @param lockRecord This should be true unless the caller knows that the record is
     * already locked.
     */
    void updateCopy(BitSet newChangeBitSet,BitSet newOverrunBitSet, boolean lockRecord);
}
