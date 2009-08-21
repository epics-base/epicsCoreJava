/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor;


import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pvCopy.BitSetUtil;
import org.epics.pvData.pvCopy.BitSetUtilFactory;
import org.epics.pvData.pvCopy.PVCopy;
import org.epics.pvData.pvCopy.PVCopyMonitor;
import org.epics.pvData.pvCopy.PVCopyMonitorRequester;
/**
 * A Base class for implementing a ChannelMonitor.
 * @author mrk
 *
 */
abstract public class BaseMonitor implements Monitor,PVCopyMonitorRequester{
    /**
     * Constructor for BaseMonitor
     * @param pvRecord The record;
     * @param monitorRequester The requester.
     * @param pvCopy The PVCopy for creating data and bit sets.
     * @param queueSize The queueSize.
     * @param executor The executor for calling requester.
     */
    protected BaseMonitor(
            PVRecord pvRecord,
            MonitorRequester monitorRequester,
            PVCopy pvCopy,
            int queueSize)
    {
        this.pvRecord = pvRecord;
        this.monitorRequester = monitorRequester;
        this.pvCopy = pvCopy;
        // a queueSize of 2 can cause a race condition.
        if(queueSize==2) queueSize = 3;
        this.queueSize = queueSize;
        pvRecord = pvCopy.getPVRecord();
        pvCopyMonitor = pvCopy.createPVCopyMonitor(this);
        if(queueSize<2)  {
            pvStructure = pvCopy.createPVStructure();
            monitorElements = new MonitorElement[2];
            for(int i=0; i<2; i++) {
                BitSet changeBitSet = new BitSet(pvStructure.getNumberFields());
                BitSet overrunBitSet = new BitSet(pvStructure.getNumberFields());
                monitorElements[i] = new MonitorElementImpl(pvStructure,changeBitSet,overrunBitSet);
            }
        } else {
            monitorQueue = MonitorQueueFactory.create(pvCopy, queueSize);
        }
    }
    protected static final Convert convert = ConvertFactory.getConvert();
    protected static final BitSetUtil bitSetUtil = BitSetUtilFactory.getCompressBitSet();
    protected PVRecord pvRecord;
    protected MonitorRequester monitorRequester;
    protected PVCopy pvCopy;
    private PVCopyMonitor pvCopyMonitor;
    private boolean firstMonitor = false;
    private int queueSize;
    // following only used if queueSize <=1
    private PVStructure pvStructure = null;
    private int indexMonitorElement = 0;
    private MonitorElement[] monitorElements = null;
    
    // following only used if queueSize>=2
    private MonitorQueue monitorQueue = null;
    private MonitorElement monitorElement = null;
    private BitSet saveChangeBitSet = null;
    private boolean overrunInProgress = false;
    
    
    /**
     * A method that must be implemented by a derived class.
     * When this class gets notified that data has changed it calls this method to see
     * if it should notify the ChannelMonitorRequester that a monitor has occurred.
     * @param changeBitSet The change bit set.
     * @return (false,true) if the ChannelMonitorRequester should be notified of a new monitor.
     */
    abstract protected boolean generateMonitor(BitSet changeBitSet);
    /* (non-Javadoc)
     * @see org.epics.pvData.monitor.Monitor#destroy()
     */
    @Override
    public void destroy() {
        stop();
    }
    /* (non-Javadoc)
     * @see org.epics.ca.channelAccess.client.ChannelMonitor#start()
     */
    @Override
    public void start() {
        BitSet changeBitSet = null;
        BitSet overrunBitSet = null;
        pvRecord.lock();
        try {
            if(queueSize<2) {
                indexMonitorElement = 0;
            } else {
                monitorQueue.clear();
                monitorElement = monitorQueue.getFree();
                changeBitSet = monitorElement.getChangedBitSet();
                overrunBitSet = monitorElement.getOverrunBitSet();
            }
        } finally {
            pvRecord.unlock();
        }
        firstMonitor = true;
        pvCopyMonitor.startMonitoring(changeBitSet,overrunBitSet);
    }
    /* (non-Javadoc)
     * @see org.epics.ca.channelAccess.client.ChannelMonitor#stop()
     */
    @Override
    public void stop() {
        pvCopyMonitor.stopMonitoring();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pvCopy.PVCopyMonitorRequester#dataChanged()
     */
    @Override
    public void dataChanged() {
        if(queueSize<2) {
            if(!firstMonitor && !generateMonitor(monitorElements[indexMonitorElement].getChangedBitSet())) return;
        } else {
            PVStructure pvStructure = monitorElement.getPVStructure();
            BitSet changedBitSet = monitorElement.getChangedBitSet();
            BitSet overrunBitSet = monitorElement.getOverrunBitSet();
            pvCopy.updateCopyFromBitSet(pvStructure, changedBitSet, false);
            if(!firstMonitor &&!generateMonitor(monitorElement.getChangedBitSet())) return;
            MonitorElement newElement = null;
            synchronized(monitorQueue) {
                    newElement = monitorQueue.getFree();
            }
            if(newElement==null) {
                if(saveChangeBitSet==null) {
                    saveChangeBitSet = new BitSet(changedBitSet.length());
                } else if(!overrunInProgress) {
                    saveChangeBitSet.clear();
                }
                overrunInProgress = true;
            }
            if(overrunInProgress) {
                int offset = 0;
                while(true) {
                    int nextSet = changedBitSet.nextSetBit(offset++);
                    if(nextSet<0) break;
                    if(saveChangeBitSet.get(nextSet)) {
                        overrunBitSet.set(nextSet);
                    } else {
                        saveChangeBitSet.set(nextSet);
                    }
                }
                if(newElement==null) return;
                overrunInProgress = false;
            }
            bitSetUtil.compress(changedBitSet, pvStructure);
            bitSetUtil.compress(overrunBitSet, pvStructure);
            pvStructure = monitorElement.getPVStructure();
            PVStructure pvNext = newElement.getPVStructure();
            convert.copy(pvStructure, pvNext);
            changedBitSet = newElement.getChangedBitSet();
            overrunBitSet = newElement.getOverrunBitSet();
            changedBitSet.clear();
            overrunBitSet.clear();
            pvCopyMonitor.switchBitSets(changedBitSet,overrunBitSet , false);
            synchronized(monitorQueue) {
                monitorQueue.setUsed(monitorElement);
            }
            monitorElement = newElement;
        }
        firstMonitor = false;
        monitorRequester.monitorEvent(this);
    }
    
    /* (non-Javadoc)
     * @see org.epics.pvData.monitor.Monitor#poll()
     */
    @Override
    public MonitorElement poll() {
        if(queueSize<2) {
            MonitorElement monitorElement = monitorElements[indexMonitorElement];
            BitSet changeBitSet = monitorElement.getChangedBitSet();
            BitSet overrunBitSet = monitorElement.getOverrunBitSet();
            int nextIndex = (indexMonitorElement + 1) % 2;
            BitSet nextChangeBitSet = monitorElements[nextIndex].getChangedBitSet();
            BitSet nextOverrunBitSet = monitorElements[nextIndex].getOverrunBitSet();
            nextChangeBitSet.clear();
            nextOverrunBitSet.clear();
            PVRecord pvRecord = pvCopy.getPVRecord();
            pvRecord.lock();
            try {
                pvCopy.updateCopyFromBitSet(pvStructure, changeBitSet, false);
                pvCopyMonitor.switchBitSets(nextChangeBitSet, nextOverrunBitSet, false);
                indexMonitorElement = nextIndex;
            } finally {
                pvRecord.unlock();
            }
            bitSetUtil.compress(changeBitSet, pvStructure);
            bitSetUtil.compress(overrunBitSet, pvStructure);
            return monitorElement;
        } else { // using queue
            while(true) {
                MonitorElement monitorElement = null;
                synchronized(monitorQueue) {
                    monitorElement = monitorQueue.getUsed();
                }
                return monitorElement;
            }
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.monitor.Monitor#release(org.epics.pvData.monitor.MonitorElement)
     */
    @Override
    public void release(MonitorElement monitorElement) {
        if(queueSize<2) return;
        synchronized(monitorQueue) {
            monitorQueue.releaseUsed(monitorElement);
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pvCopy.PVCopyMonitorRequester#unlisten()
     */
    @Override
    public void unlisten() {
        monitorRequester.unlisten();
    }
    
    private static class MonitorElementImpl implements MonitorElement {
        private PVStructure pvStructure;
        private BitSet changedBitSet;
        private BitSet overrunBitSet;
        

        MonitorElementImpl(PVStructure pvStructure,BitSet changedBitSet, BitSet overrunBitSet) {
            super();
            this.pvStructure = pvStructure;
            this.changedBitSet = changedBitSet;
            this.overrunBitSet = overrunBitSet;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.monitor.MonitorElement#getChangedBitSet()
         */
        @Override
        public BitSet getChangedBitSet() {
            return changedBitSet;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.monitor.MonitorElement#getOverrunBitSet()
         */
        @Override
        public BitSet getOverrunBitSet() {
            return overrunBitSet;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.monitor.MonitorElement#getPVStructure()
         */
        @Override
        public PVStructure getPVStructure() {
            return pvStructure;
        }
        
    }
}

