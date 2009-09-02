/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor;

import java.util.concurrent.atomic.*;

import org.epics.pvData.factory.*;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.*;
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
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    /**
     * Constructor for BaseMonitor
     * @param pvRecord The record;
     * @param monitorCreator The create caller.
     * @param monitorRequester The requester.
     * @param pvCopy The PVCopy for creating data and bit sets.
     * @param queueSize The queueSize.
     * @param executor The executor for calling requester.
     */
    protected BaseMonitor(
            PVRecord pvRecord,
            MonitorCreator monitorCreator,
            MonitorRequester monitorRequester,
            PVCopy pvCopy,
            int queueSize)
    {
        this.pvRecord = pvRecord;
        this.monitorCreator = monitorCreator;
        this.monitorRequester = monitorRequester;
        this.pvCopy = pvCopy;
        if(queueSize<0) queueSize = 0;
        // a queueSize of 2 can cause a race condition.
        if(queueSize==2) queueSize = 3;
        this.queueSize = queueSize;
        pvRecord = pvCopy.getPVRecord();
        pvCopyMonitor = pvCopy.createPVCopyMonitor(this);
        if(queueSize==0) {
            numberMonitors = new AtomicInteger(0);
            pvStructure = pvCopy.createPVStructure();
            BitSet changeBitSet = new BitSet(pvStructure.getNumberFields());
            BitSet overrunBitSet = new BitSet(pvStructure.getNumberFields());
            monitorElement = new MonitorElementImpl(pvStructure,changeBitSet,overrunBitSet);
        } else if(queueSize==1)  {
            pvStructure = pvCopy.createPVStructure();
            monitorElements = new MonitorElement[2];
            for(int i=0; i<2; i++) {
                BitSet changeBitSet = new BitSet(pvStructure.getNumberFields());
                BitSet overrunBitSet = new BitSet(pvStructure.getNumberFields());
                monitorElements[i] = new MonitorElementImpl(pvStructure,changeBitSet,overrunBitSet);
            }
        } else if(queueSize>2){
            MonitorElement[] monitorElements = new MonitorElement[queueSize];
            for(int i=0; i<queueSize; i++) {
                PVStructure pvStructure = pvDataCreate.createPVStructure(null, pvCopy.getStructure());
                monitorElements[i] = MonitorQueueFactory.createMonitorElement(pvStructure);
            }
            monitorQueue = MonitorQueueFactory.create(monitorElements);
        }
    }
    protected static final Convert convert = ConvertFactory.getConvert();
    protected static final BitSetUtil bitSetUtil = BitSetUtilFactory.getCompressBitSet();
    protected PVRecord pvRecord;
    protected MonitorCreator monitorCreator;
    protected MonitorRequester monitorRequester;
    protected PVCopy pvCopy;
    private PVCopyMonitor pvCopyMonitor;
    private boolean firstMonitor = false;
    private int queueSize;
    // following used if queueSize is 0. It also uses monitorElement
    private AtomicInteger numberMonitors = null;
    // following only used if queueSize ==1
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
        monitorRequester.unlisten();
        monitorCreator.remove(this);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.monitor.Monitor#start()
     */
    @Override
    public void start() {
        BitSet changeBitSet = null;
        BitSet overrunBitSet = null;
        // if queueSize==0 than changeBitSet and overrunBitSet will stay null
        if(queueSize==0) {
            
        } else {
            pvRecord.lock();
            try {
                if(queueSize==1) {
                    indexMonitorElement = 0;
                    changeBitSet = monitorElements[0].getChangedBitSet();
                    overrunBitSet = monitorElements[0].getOverrunBitSet();
                } else {
                    monitorQueue.clear();
                    monitorElement = monitorQueue.getFree();
                    changeBitSet = monitorElement.getChangedBitSet();
                    overrunBitSet = monitorElement.getOverrunBitSet();
                }
            } finally {
                pvRecord.unlock();
            }
        }
        firstMonitor = true;
        if(queueSize==0) {
            pvCopyMonitor.startMonitoring();
        } else {
            pvCopyMonitor.startMonitoring(changeBitSet,overrunBitSet);
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.monitor.Monitor#stop()
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
        if(queueSize==0) {
            BitSet changedBitSet = monitorElement.getChangedBitSet();
            if(!firstMonitor && !generateMonitor(changedBitSet)) return;
            synchronized(monitorElement) {
                pvCopy.updateCopySetBitSet(pvStructure, changedBitSet, false);
            }
            numberMonitors.addAndGet(1);
        } else if(queueSize==1) {
            BitSet changedBitSet = null;
            synchronized(monitorElements) {
                changedBitSet =monitorElements[indexMonitorElement].getChangedBitSet();
            }
            if(!firstMonitor && !generateMonitor(changedBitSet)) return;
        } else if(queueSize>2){
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
        if(queueSize==0) {
            if(numberMonitors.get()==0) return null;
            return monitorElement;
        }
        if(queueSize==1) {
            MonitorElement monitorElement = null;
            BitSet changeBitSet = null;
            BitSet overrunBitSet = null;
            synchronized(monitorElements) {
                monitorElement = monitorElements[indexMonitorElement];
                changeBitSet = monitorElement.getChangedBitSet();
                overrunBitSet = monitorElement.getOverrunBitSet();
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
            }
            bitSetUtil.compress(changeBitSet, pvStructure);
            bitSetUtil.compress(overrunBitSet, pvStructure);
            if(changeBitSet.nextSetBit(0)<0) return null;
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
        if(queueSize==0) {
            synchronized(monitorElement) {
                monitorElement.getChangedBitSet().clear();
            }
            numberMonitors.decrementAndGet();
        }
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

