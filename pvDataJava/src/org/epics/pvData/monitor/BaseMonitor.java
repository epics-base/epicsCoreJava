/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor;


import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.misc.Executor;
import org.epics.pvData.misc.ExecutorNode;
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
     * @param channel The channel;
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
        callRequester = new CallRequester();
        pvCopyMonitor = pvCopy.createPVCopyMonitor(this);
        if(queueSize<2)  {
            pvStructure = pvCopy.createPVStructure();
            changeBitSets = new BitSet[2];
            overrunBitSets = new BitSet[2];
            for(int i=0; i<2; i++) {
                changeBitSets[i] = new BitSet(pvStructure.getNextFieldOffset());
                overrunBitSets[i] = new BitSet(pvStructure.getNextFieldOffset());
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
    protected Executor executor;
    private CallRequester callRequester;
    private PVCopyMonitor pvCopyMonitor;
    private boolean isMonitoring = false;
    private boolean firstMonitor = false;
    private int queueSize;
    // following only used if queueSize <=1
    private PVStructure pvStructure = null;
    private int indexBitSet = 0;
    private BitSet[] changeBitSets = null;
    private BitSet[] overrunBitSets = null;
    // following only used if queueSize>=2
    private MonitorQueue monitorQueue = null;
    private MonitorQueue.MonitorQueueElement monitorQueueElement = null;
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
     * @see org.epics.ca.channelAccess.client.ChannelMonitor#start()
     */
    @Override
    public void start() {
        BitSet changeBitSet = null;
        BitSet overrunBitSet = null;
        pvRecord.lock();
        try {
            if(queueSize<2) {
                indexBitSet = 0;
                changeBitSet = changeBitSets[indexBitSet];
                overrunBitSet = overrunBitSets[indexBitSet];
                
            } else {
                monitorQueue.clear();
                monitorQueueElement = monitorQueue.getFree();
                changeBitSet = monitorQueueElement.getChangedBitSet();
                overrunBitSet = monitorQueueElement.getOverrunBitSet();
            }
        } finally {
            pvRecord.unlock();
        }
        firstMonitor = true;
        pvCopyMonitor.startMonitoring(changeBitSet,overrunBitSet);
        isMonitoring = true;
    }
    /* (non-Javadoc)
     * @see org.epics.ca.channelAccess.client.ChannelMonitor#stop()
     */
    @Override
    public void stop() {
        pvCopyMonitor.stopMonitoring();
        isMonitoring = false;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pvCopy.PVCopyMonitorRequester#dataChanged()
     */
    @Override
    public void dataChanged() {
        if(queueSize<2) {
            if(!firstMonitor && !generateMonitor(changeBitSets[indexBitSet])) return;
        } else {
            PVStructure pvStructure = monitorQueueElement.getPVStructure();
            BitSet changedBitSet = monitorQueueElement.getChangedBitSet();
            BitSet overrunBitSet = monitorQueueElement.getOverrunBitSet();
            pvCopy.updateCopyFromBitSet(pvStructure, changedBitSet, false);
            if(!firstMonitor &&!generateMonitor(monitorQueueElement.getChangedBitSet())) return;
            MonitorQueue.MonitorQueueElement newElement = null;
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
            pvStructure = monitorQueueElement.getPVStructure();
            PVStructure pvNext = newElement.getPVStructure();
            convert.copy(pvStructure, pvNext);
            changedBitSet = newElement.getChangedBitSet();
            overrunBitSet = newElement.getOverrunBitSet();
            changedBitSet.clear();
            overrunBitSet.clear();
            pvCopyMonitor.switchBitSets(changedBitSet,overrunBitSet , false);
            synchronized(monitorQueue) {
                monitorQueue.setUsed(monitorQueueElement);
            }
            monitorQueueElement = newElement;
        }
        firstMonitor = false;
        callRequester.call();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pvCopy.PVCopyMonitorRequester#unlisten()
     */
    @Override
    public void unlisten() {
        monitorRequester.unlisten();
    }
    
    private class CallRequester implements Runnable {
        private ExecutorNode executorNode = null;
        private CallRequester(){
            executorNode = executor.createNode(this);
        }         

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run() {
            if(queueSize<2) {
                BitSet changeBitSet = changeBitSets[indexBitSet];
                BitSet overrunBitSet = changeBitSets[indexBitSet];
                int nextIndex = (indexBitSet + 1) % 2;
                BitSet nextChangeBitSet = changeBitSets[nextIndex];
                BitSet nextOverrunBitSet = changeBitSets[nextIndex];
                nextChangeBitSet.clear();
                nextOverrunBitSet.clear();
                PVRecord pvRecord = pvCopy.getPVRecord();
                pvRecord.lock();
                try {
                    pvCopy.updateCopyFromBitSet(pvStructure, changeBitSet, false);
                    pvCopyMonitor.switchBitSets(nextChangeBitSet, nextOverrunBitSet, false);
                    indexBitSet = nextIndex;
                } finally {
                    pvRecord.unlock();
                }
                bitSetUtil.compress(changeBitSet, pvStructure);
                bitSetUtil.compress(overrunBitSet, pvStructure);
                monitorRequester.monitorEvent(pvStructure,changeBitSet,overrunBitSet);
            } else { // using queue
                while(true) {
                    PVStructure pvStructure = null;
                    MonitorQueue.MonitorQueueElement monitorQueueElement = null;
                    synchronized(monitorQueue) {
                        monitorQueueElement = monitorQueue.getUsed();
                        if(monitorQueueElement==null) {
                            return;
                        }
                    }
                    pvStructure = monitorQueueElement.getPVStructure();
                    BitSet changeBitSet = monitorQueueElement.getChangedBitSet();
                    BitSet overrunBitSet = monitorQueueElement.getOverrunBitSet();
                    monitorRequester.monitorEvent(pvStructure,changeBitSet,overrunBitSet);
                    synchronized(monitorQueue) {
                        monitorQueue.releaseUsed(monitorQueueElement);
                    }
                }
            }
        }
        private void call() {
            executor.execute(executorNode);
        }

    }
}

