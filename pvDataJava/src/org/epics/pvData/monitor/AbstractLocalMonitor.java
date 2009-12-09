/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor;

import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pvCopy.PVCopy;
import org.epics.pvData.pvCopy.PVCopyMonitor;
import org.epics.pvData.pvCopy.PVCopyMonitorRequester;
/**
 * A Base class for implementing a ChannelMonitor.
 * @author mrk
 *
 */
abstract public class AbstractLocalMonitor extends AbstractMonitor implements PVCopyMonitorRequester{
    /**
     * Constructor for BaseMonitor
     * @param pvRecord The record;
     * @param monitorCreator The create caller.
     * @param monitorRequester The requester.
     * @param pvCopy The PVCopy for creating data and bit sets.
     * @param queueSize The queueSize.
     * @param executor The executor for calling requester.
     */
    protected AbstractLocalMonitor(
            PVRecord pvRecord,
            MonitorCreator monitorCreator,
            MonitorRequester monitorRequester,
            PVCopy pvCopy,
            int queueSize)
    {
        super(monitorCreator,monitorRequester,queueSize);
        this.pvRecord = pvRecord;
        this.pvCopy = pvCopy;
        pvCopyMonitor = pvCopy.createPVCopyMonitor(this);
        super.init(pvCopy.createPVStructure());
    }
   
    protected PVRecord pvRecord;
    
    protected PVCopy pvCopy;
    private PVCopyMonitor pvCopyMonitor;
    
    @Override
    protected PVStructure createPVStructure() {
        return pvCopy.createPVStructure();
    }
    @Override
    protected void startMonitoring() {
        pvCopyMonitor.startMonitoring();
    }
    @Override
    protected void startMonitoring(BitSet changeBitSet,BitSet overrunBitSet) {
        pvCopyMonitor.startMonitoring(changeBitSet,overrunBitSet);
    }
    @Override
    protected void updateBitSet(PVStructure pvStructure,BitSet bitSet) {
        pvCopy.updateCopySetBitSet(pvStructure, bitSet, false);
    }
    @Override
    protected void updateFromBitSet(PVStructure pvStructure,BitSet bitSet) {
        pvCopy.updateCopyFromBitSet(pvStructure, bitSet, false);
    }
    @Override
    protected void switchBitSets(BitSet changedBitSet,BitSet overrunBitSet) {
    	pvCopyMonitor.switchBitSets(changedBitSet, overrunBitSet, false);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.monitor.Monitor#destroy()
     */
    @Override
    public void destroy() {
        stop();
        monitorRequester.unlisten();
        monitorCreator.remove(this);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.monitor.Monitor#stop()
     */
    @Override
    public Status stop() {
        pvCopyMonitor.stopMonitoring();
        return okStatus;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pvCopy.PVCopyMonitorRequester#dataChanged()
     */
    @Override
    public void dataChanged() {
        super.dataChanged();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pvCopy.PVCopyMonitorRequester#unlisten()
     */
    @Override
    public void unlisten() {
        monitorRequester.unlisten();
    }
}

