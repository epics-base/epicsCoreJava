/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor;

import java.util.concurrent.atomic.AtomicInteger;

import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.factory.StatusFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.StatusCreate;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pvCopy.BitSetUtil;
import org.epics.pvData.pvCopy.BitSetUtilFactory;
/**
 * An abstract base class for implementing a ChannelMonitor.
 * @author mrk
 *
 */
abstract public class AbstractMonitor implements Monitor{
    protected static final StatusCreate statusCreate = StatusFactory.getStatusCreate();
    protected static final Status okStatus = statusCreate.getStatusOK();
    /**
     * Create a structure to hold data.
     * @return The interface.
     */
    abstract PVStructure createPVStructure();
    /**
     * Start monitoring for notify only, i.e. no data will be transfered.
     */
    abstract void startMonitoring();
    /**
     * Start monitoring and data will be transfered.
     * @param changeBitSet The initial change bitSet.
     * @param overrunBitSet The initial overrun bitSet.
     */
    abstract void startMonitoring(BitSet changeBitSet,BitSet overrunBitSet);
    /**
     * Update the pvStructure and set bits in the bitSet for all changed fields.
     * @param pvStructure The structure holding the data.
     * @param bitSet The bitSet for changed fields.
     */
    abstract void updateBitSet(PVStructure pvStructure,BitSet bitSet);
    /**
     * Update the destination from the pvStructure and the bitSet.
     * @param pvStructure The structure holding the new data.
     * @param bitSet The bitSet showing which fields to change.
     */
    abstract void updateFromBitSet(PVStructure pvStructure,BitSet bitSet);
    /**
     * Switch bit sets.
     * @param changedBitSet The new bitSet for changes.
     * @param overrunBitSet The new bitSet for overrun.
     */
    abstract void switchBitSets(BitSet changedBitSet,BitSet overrunBitSet);
    /**
     * A method that must be implemented by a derived class.
     * When this class gets notified that data has changed it calls this method to see
     * if it should notify the ChannelMonitorRequester that a monitor has occurred.
     * @param changeBitSet The change bit set.
     * @return (false,true) if the ChannelMonitorRequester should be notified of a new monitor.
     */
    abstract protected boolean generateMonitor(BitSet changeBitSet);
    /**
     * Constructor for BaseMonitor
     * @param pvRecord The record;
     * @param monitorCreator The create caller.
     * @param monitorRequester The requester.
     * @param pvCopy The PVCopy for creating data and bit sets.
     * @param queueSize The queueSize.
     * @param executor The executor for calling requester.
     */
    protected AbstractMonitor(
            MonitorCreator monitorCreator,
            MonitorRequester monitorRequester,
            int queueSize)
    {
        this.monitorCreator = monitorCreator;
        this.monitorRequester = monitorRequester;
        if(queueSize<-1) queueSize = -1;
        this.queueSize = queueSize;
        if(queueSize==-1) {
            monitorType = MonitorType.notify;
        } else if(queueSize==0) {
            monitorType = MonitorType.entire;
        } else if(queueSize==1) {
            monitorType = MonitorType.single;
        } else {
            monitorType = MonitorType.queue;
        }
    }
    /**
     * This must be called by derived class after calling constructor AbstractMonitor
     * @param structure
     */
    protected void init(Structure structure) {
        switch(monitorType) {
        case notify:
            numberMonitors = new AtomicInteger(0);
            monitorElement = MonitorQueueFactory.createMonitorElement(null);
            break;
        case entire:
            numberMonitors = new AtomicInteger(0);
            monitorElement = MonitorQueueFactory.createMonitorElement(createPVStructure());
            break;
        case single:
            numberMonitors = new AtomicInteger(0);
            monitorElement = MonitorQueueFactory.createMonitorElement(createPVStructure());
            break;
        case queue:
            MonitorElement[] monitorElements = new MonitorElement[queueSize];
            for(int i=0; i<queueSize; i++) {
                PVStructure pvStructure = pvDataCreate.createPVStructure(null, structure);
                monitorElements[i] = MonitorQueueFactory.createMonitorElement(pvStructure);
            }
            monitorQueue = MonitorQueueFactory.create(monitorElements);
        }
    }
    static enum MonitorType {
        notify,
        entire,
        single,
        queue
    }
    
    protected static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    protected static final Convert convert = ConvertFactory.getConvert();
    protected static final BitSetUtil bitSetUtil = BitSetUtilFactory.getCompressBitSet();
    protected final MonitorCreator monitorCreator;
    protected final MonitorRequester monitorRequester;
    protected boolean firstMonitor = false;
    protected final int queueSize;
    private final MonitorType monitorType;
    // following used if queueSize is <= 1. It also uses monitorElement
    private AtomicInteger numberMonitors = null;
    // following only used if queueSize>=2
    private MonitorQueue monitorQueue = null;
    private MonitorElement monitorElement = null;
    private BitSet saveChangeBitSet = null;
    private boolean overrunInProgress = false;
    
    
    
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
    public Status start() {
        BitSet changeBitSet = null;
        BitSet overrunBitSet = null;
        firstMonitor = true;
        switch(monitorType) {
        case notify:
            startMonitoring();
            return okStatus;
        case entire:
        case single:
            changeBitSet = monitorElement.getChangedBitSet();
            overrunBitSet = monitorElement.getOverrunBitSet();
            break;
        case queue:
            monitorQueue.clear();
            monitorElement = monitorQueue.getFree();
            changeBitSet = monitorElement.getChangedBitSet();
            overrunBitSet = monitorElement.getOverrunBitSet();
            break;
        }
        startMonitoring(changeBitSet,overrunBitSet);
        return okStatus;
    }
    
    protected void dataChanged() {
        switch(monitorType) {
        case notify:
        case entire:
            numberMonitors.addAndGet(1);
            break;
        case single: {
            BitSet changedBitSet = monitorElement.getChangedBitSet();
            if(!firstMonitor && !generateMonitor(changedBitSet)) return;
            synchronized(monitorElement) {
                updateBitSet(monitorElement.getPVStructure(), changedBitSet);
            }
            numberMonitors.addAndGet(1);
            break;
        }
        case queue: {
            PVStructure pvStructure = monitorElement.getPVStructure();
            BitSet changedBitSet = monitorElement.getChangedBitSet();
            BitSet overrunBitSet = monitorElement.getOverrunBitSet();
            updateFromBitSet(pvStructure, changedBitSet);
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
            switchBitSets(changedBitSet,overrunBitSet);
            synchronized(monitorQueue) {
                monitorQueue.setUsed(monitorElement);
            }
            monitorElement = newElement;
        }
        }
        firstMonitor = false;
        monitorRequester.monitorEvent(this);
    }
    
    /* (non-Javadoc)
     * @see org.epics.pvData.monitor.Monitor#poll()
     */
    @Override
    public MonitorElement poll() {
        switch(monitorType) {
        case notify:
            if(numberMonitors.get()==0) return null;
            return monitorElement;
        case entire:
            if(numberMonitors.get()==0) return null;
            BitSet bitSet = monitorElement.getChangedBitSet();
            bitSet.clear();
            bitSet.set(0);
            bitSet = monitorElement.getOverrunBitSet();
            bitSet.clear();
            return monitorElement;
        case single:
            if(numberMonitors.get()==0) return null;
            return monitorElement;
        case queue:
            MonitorElement monitorElement = null;
            synchronized(monitorQueue) {
                monitorElement = monitorQueue.getUsed();
            }
            return monitorElement;
        }
        throw new IllegalStateException("logic error");
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.monitor.Monitor#release(org.epics.pvData.monitor.MonitorElement)
     */
    @Override
    public void release(MonitorElement monitorElement) {
        switch(monitorType) {
        case notify:
        case entire:
            numberMonitors.decrementAndGet();
            return;
        case single:
            synchronized(monitorElement) {
                monitorElement.getChangedBitSet().clear();
            }
            numberMonitors.decrementAndGet();
            return;
        case queue:
            synchronized(monitorQueue) {
                monitorQueue.releaseUsed(monitorElement);
            }
            return;
        }
        throw new IllegalStateException("logic error");
    }
}

