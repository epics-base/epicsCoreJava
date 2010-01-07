/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor;

import java.util.concurrent.atomic.*;

import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.factory.StatusFactory;
import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.StatusCreate;
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
    protected static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    protected static final Convert convert = ConvertFactory.getConvert();
    protected static final BitSetUtil bitSetUtil = BitSetUtilFactory.getCompressBitSet();
    protected final MonitorCreator monitorCreator;
    protected final MonitorRequester monitorRequester;
    
    abstract public Status stop();
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
    }
    /**
     * This must be called by derived class after calling constructor AbstractMonitor
     * @param structure
     */
    protected void init(PVStructure pvStructure) {
    	if(queueSize==-1) {
    		monitorImpl = new MonitorNotify(this);
    	} else if(queueSize==0) {
    		monitorImpl = new MonitorEntire(this);
    	} else if(queueSize==1) {
    		monitorImpl = new MonitorSingle(this);
    	} else {
    		monitorImpl = new MonitorWithQueue(this,pvStructure,queueSize);
    	}
    }
    
    
    private final int queueSize;
    private MonitorImpl monitorImpl = null;    
    
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
    	return monitorImpl.start();
    }
    
    protected void dataChanged() {
    	monitorImpl.dataChanged();
    }
    
    /* (non-Javadoc)
     * @see org.epics.pvData.monitor.Monitor#poll()
     */
    @Override
    public MonitorElement poll() {
    	return monitorImpl.poll();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.monitor.Monitor#release(org.epics.pvData.monitor.MonitorElement)
     */
    @Override
    public void release(MonitorElement monitorElement) {
    	monitorImpl.release(monitorElement);
    }
    
    private interface MonitorImpl {
        public Status start();
    	public void dataChanged();
    	public MonitorElement poll();
    	public void release(MonitorElement monitorElement);
    }
    
    private final class MonitorNotify implements MonitorImpl {
    	private final AbstractMonitor monitor;
        private final MonitorElement monitorElement;
    	private volatile boolean gotMonitor = false;
        
    	MonitorNotify(AbstractMonitor monitor) {
    		this.monitor = monitor;
            monitorElement = MonitorQueueFactory.createMonitorElement(null);
    	}
    	public Status start() {
    		startMonitoring();
    		gotMonitor = false;
            return okStatus;
    	}
        public void dataChanged() {
        	gotMonitor = true;
        	monitorRequester.monitorEvent(monitor);
        }
        public MonitorElement poll() {
        	if (!gotMonitor) return null;
            return monitorElement;
        }
        public void release(MonitorElement monitorElement) {
        	gotMonitor = false;
        }
     }
    
    private final class MonitorEntire implements MonitorImpl {
    	private final AbstractMonitor monitor;
        private final MonitorElement monitorElement;
    	private volatile boolean gotMonitor = false;
    	private final BitSet dummyChangedBitSet;
    	private final BitSet dummyOverrunBitSet;
    	
        
    	MonitorEntire(AbstractMonitor monitor) {
    		this.monitor = monitor;
            monitorElement = MonitorQueueFactory.createMonitorElement(createPVStructure());
            // clear and set it once for all
    		BitSet changeBitSet = monitorElement.getChangedBitSet();
            BitSet overrunBitSet = monitorElement.getOverrunBitSet();
    		changeBitSet.clear();
    		changeBitSet.set(0);
            overrunBitSet.clear();
            
            // prepare dummies (clone cleared)
            dummyChangedBitSet = (BitSet)overrunBitSet.clone();
            dummyOverrunBitSet = (BitSet)overrunBitSet.clone();
    	}
    	public Status start() {
    		gotMonitor = false;
            startMonitoring(dummyChangedBitSet,dummyOverrunBitSet);
            return okStatus;
    	}
        public void dataChanged() {
    		gotMonitor = true;
        	monitorRequester.monitorEvent(monitor);
        }
        public MonitorElement poll() {
        	if (!gotMonitor) return null;
        	dummyChangedBitSet.clear();	// not to do too much work on setting overrun bitSet
        	return monitorElement;
        }
        public void release(MonitorElement monitorElement) {
    		gotMonitor = false;
        }
     }
    
    private final class MonitorSingle implements MonitorImpl {
    	private final AbstractMonitor monitor;
    	
    	private volatile boolean gotMonitor = false;
        private final MonitorElement monitorElement;
        private final PVStructure monitorElementStructure;
        private final BitSet monitorElementChangeBitSet;
        private final BitSet monitorElementOverrunBitSet;
        private final BitSet dataChangeBitSet;
        private final BitSet dataOverrunBitSet;
        private boolean firstMonitor = false;

        MonitorSingle(AbstractMonitor monitor) {
        	this.monitor = monitor;
        	monitorElement = MonitorQueueFactory.createMonitorElement(createPVStructure());
        	monitorElementStructure = monitorElement.getPVStructure();
        	monitorElementChangeBitSet = monitorElement.getChangedBitSet();
        	monitorElementOverrunBitSet = monitorElement.getOverrunBitSet();
        	dataChangeBitSet = (BitSet)monitorElement.getChangedBitSet().clone();
        	dataOverrunBitSet = (BitSet)monitorElement.getChangedBitSet().clone();
    	}
    	public Status start() {
        	synchronized(monitorElement) {
	    		gotMonitor = false;
	    		firstMonitor = true;
	    		dataChangeBitSet.clear();
	    		dataOverrunBitSet.clear();
	            startMonitoring(dataChangeBitSet,dataOverrunBitSet);
        	}
            return okStatus;
    	}
        public void dataChanged() {
        	
        	synchronized(monitorElement) {
        		if(firstMonitor) {
        			dataChangeBitSet.clear();
        			dataChangeBitSet.set(0);
        		}
        		if(!gotMonitor) { 
        			dataOverrunBitSet.clear();
        		} else {
        			dataOverrunBitSet.or_and(dataChangeBitSet, monitorElementChangeBitSet);
        		}
        		updateFromBitSet(monitorElementStructure, dataChangeBitSet);
        		gotMonitor = true;
        	}
            if(!firstMonitor && !generateMonitor(dataChangeBitSet)) return;
            firstMonitor = false;
            monitorRequester.monitorEvent(monitor);
        }
        public MonitorElement poll() {
            synchronized(monitorElement) {
            	if(!gotMonitor) return null;
            	monitorElementChangeBitSet.set(dataChangeBitSet);
            	monitorElementOverrunBitSet.set(dataOverrunBitSet);
            }
            return monitorElement;
        }
        public void release(MonitorElement monitorElement) {
        	synchronized(monitorElement) {
        		monitorElementChangeBitSet.xor(dataChangeBitSet);
                dataChangeBitSet.clear();
                dataOverrunBitSet.clear();
                gotMonitor = false;
            }
        }
     }
    
    private final class MonitorWithQueue implements MonitorImpl {
    	private final AbstractMonitor monitor;
        private final MonitorQueue monitorQueue;
        
        private volatile MonitorElement monitorElement = null;
        private BitSet overrunChangeBitSet = null;
        private volatile boolean firstMonitor = false;
        private volatile boolean overrunInProgress = false;
        
    	MonitorWithQueue(AbstractMonitor monitor,PVStructure pvStructure,int queueSize) {
    		this.monitor = monitor;
    		MonitorElement[] monitorElements = new MonitorElement[queueSize];
            for(int i=0; i<queueSize; i++) {
                PVStructure pvNew = pvDataCreate.createPVStructure(null, "", pvStructure);
                monitorElements[i] = MonitorQueueFactory.createMonitorElement(pvNew);
            }
            monitorQueue = MonitorQueueFactory.create(monitorElements);
    	}
    	public Status start() {
    		firstMonitor = true;
    		overrunInProgress = false;
    		monitorQueue.clear();
    		monitorElement = monitorQueue.getFree();
    		BitSet changedBitSet = monitorElement.getChangedBitSet();
            BitSet overrunBitSet = monitorElement.getOverrunBitSet();
            changedBitSet.clear();
            overrunBitSet.clear();
            startMonitoring(changedBitSet,overrunBitSet);
            return okStatus;
    	}
    	
        /* (non-Javadoc)
         * @see org.epics.pvData.monitor.AbstractMonitor.MonitorImpl#dataChanged()
         */
        public void dataChanged() {
        	PVStructure pvStructure = monitorElement.getPVStructure();
            BitSet changedBitSet = monitorElement.getChangedBitSet();
            BitSet overrunBitSet = monitorElement.getOverrunBitSet();
            updateFromBitSet(pvStructure, changedBitSet);
            if(!firstMonitor && !generateMonitor(changedBitSet)) return;
            firstMonitor = false;
            MonitorElement newElement = null;
            synchronized(monitorQueue) {
            	newElement = monitorQueue.getFree();
            	if(newElement==null) {
            		if(overrunChangeBitSet==null) {
            		    overrunChangeBitSet = new BitSet(changedBitSet.length());
            		} else {
            			overrunChangeBitSet.clear();
            		}
            	    overrunInProgress = true;
            	    
            	}
            }
            if(overrunInProgress) {
            	int nextSet = 0;
            	while(true) {
            		nextSet = changedBitSet.nextSetBit(nextSet);
            		if(nextSet<0) break;
            		if(overrunChangeBitSet.get(nextSet)) {
            			overrunBitSet.set(nextSet);
            		} else {
            			overrunChangeBitSet.set(nextSet);
            		}
            		nextSet++;
            	}
            	if(newElement==null) return;
            	overrunInProgress = false;
            }
            bitSetUtil.compress(changedBitSet, pvStructure);
            bitSetUtil.compress(overrunBitSet, pvStructure);
            convert.copy(pvStructure, newElement.getPVStructure());
            changedBitSet = newElement.getChangedBitSet();
            overrunBitSet = newElement.getOverrunBitSet();
            changedBitSet.clear();
            overrunBitSet.clear();
            switchBitSets(changedBitSet,overrunBitSet);
            synchronized(monitorQueue) {
            	monitorQueue.setUsed(monitorElement);
            	monitorElement = newElement;
            }
            monitorRequester.monitorEvent(monitor);
        }
        public MonitorElement poll() {
            synchronized(monitorQueue) {
                return monitorQueue.getUsed();
            }
        }
        public void release(MonitorElement currentElement) {
        	synchronized(monitorQueue) {
        		monitorQueue.releaseUsed(currentElement);
        		if(overrunInProgress) {
        			PVStructure pvStructure = monitorElement.getPVStructure();
        			BitSet changedBitSet = monitorElement.getChangedBitSet();
                    BitSet overrunBitSet = monitorElement.getOverrunBitSet();
                    MonitorElement newElement = monitorQueue.getFree();
                    bitSetUtil.compress(changedBitSet, pvStructure);
                    bitSetUtil.compress(overrunBitSet, pvStructure);
                    convert.copy(pvStructure, newElement.getPVStructure());
                    changedBitSet = newElement.getChangedBitSet();
                    overrunBitSet = newElement.getOverrunBitSet();
                    changedBitSet.clear();
                    overrunBitSet.clear();
                    switchBitSets(changedBitSet,overrunBitSet);
                    monitorQueue.setUsed(monitorElement);
                	monitorElement = newElement;
                	overrunInProgress = false;
        		}
        	}
        }
     }
}

