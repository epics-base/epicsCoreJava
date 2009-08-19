/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor;

import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pvCopy.PVCopy;

/**
 * Factory that creates a MonitorQueue.
 * @author mrk
 *
 */
public class MonitorQueueFactory {
    /**
     * Create a MonitorQueue.
     * @param pvCopy The PVCopy to create the data in each queue element.
     * @param queueSize The queue size which must be at least 2.
     * @throws IllegalStateException if the queue size is not at least 2.
     * @return The MonitorQueue interface.
     */
    public static MonitorQueue create(PVCopy pvCopy, int queueSize) {
        if(queueSize<2) {
            throw new IllegalStateException("queueSize must be at least 2");
        }
        MonitorQueue.MonitorQueueElement[] monitorQueueElements = new MonitorQueue.MonitorQueueElement[queueSize];
        for(int i=0; i<monitorQueueElements.length; i++) {
            PVStructure pvStructure = pvCopy.createPVStructure();
            monitorQueueElements[i] = new MonitorQueueElementImlp(pvStructure);
            
        }
        return new MonitorQueueImpl(monitorQueueElements);
    }
    
    
    private static class MonitorQueueElementImlp implements MonitorQueue.MonitorQueueElement {
        MonitorQueueElementImlp(PVStructure pvStructure) {
            this.pvStructure = pvStructure;
            changedBitSet = new BitSet(pvStructure.getNumberFields());
            overrunBitSet = new BitSet(pvStructure.getNumberFields());
        }
        
        private final PVStructure pvStructure;
        private final BitSet changedBitSet;
        private final BitSet overrunBitSet;
        
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue.MonitorQueueElement#getChangedBitSet()
         */
        @Override
        public BitSet getChangedBitSet() {
            return changedBitSet;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue.MonitorQueueElement#getOverrunBitSet()
         */
        @Override
        public BitSet getOverrunBitSet() {
            return overrunBitSet;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue.MonitorQueueElement#getPVStructure()
         */
        @Override
        public PVStructure getPVStructure() {
            return pvStructure;
        }
    }
    private static class MonitorQueueImpl implements MonitorQueue {
        private final MonitorQueueElement[] monitorQueueElements;
        private final int number;
        private int numberFree = 0;
        private int numberUsed = 0;
        private int nextGetFree = 0;
        private int nextSetUsed = 0;
        private int nextGetUsed = 0;
        private int nextReleaseUsed = 0;


        MonitorQueueImpl(MonitorQueueElement[] monitorQueueElements) {
            this.monitorQueueElements = monitorQueueElements;
            number = monitorQueueElements.length;
            numberFree = number;
        }
       
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue#clear()
         */
        @Override
        public void clear() {
            numberFree = number;
            numberUsed = 0;
            nextGetFree = 0;
            nextSetUsed = 0;
            nextGetUsed = 0;
            nextReleaseUsed = 0;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue#capacity()
         */
        @Override
        public int capacity() {
            return number;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue#getNumberFree()
         */
        @Override
        public int getNumberFree() {
            return numberFree;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue#getFree()
         */
        @Override
        public MonitorQueueElement getFree() {
            if(numberFree==0) return null;
            numberFree--;
            MonitorQueueElement monitorQueueElement = monitorQueueElements[nextGetFree++];
            if(nextGetFree>=number) nextGetFree = 0;
            return monitorQueueElement;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue#setUsed(org.epics.ioc.channelAccess.MonitorQueue.MonitorQueueElement)
         */
        @Override
        public void setUsed(MonitorQueueElement monitorQueueElement) {
            if(monitorQueueElement!=monitorQueueElements[nextSetUsed++]) {
                throw new IllegalStateException("not correct monitorQueueElement");
            }
            numberUsed++;
            if(nextSetUsed>=number) nextSetUsed = 0;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue#getUsed()
         */
        @Override
        public MonitorQueueElement getUsed() {
            if(numberUsed==0) return null;
            MonitorQueueElement monitorQueueElement = monitorQueueElements[nextGetUsed++];
            if(nextGetUsed>=number) nextGetUsed = 0;
            return monitorQueueElement;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue#releaseUsed(org.epics.ioc.channelAccess.MonitorQueue.MonitorQueueElement)
         */
        @Override
        public void releaseUsed(MonitorQueueElement monitorQueueElement) {
            if(monitorQueueElement!=monitorQueueElements[nextReleaseUsed++]) {
                throw new IllegalStateException("not monitorQueueElement returned by last call to getUsed");
            }
            if(nextReleaseUsed>=number) nextReleaseUsed = 0;
            numberUsed--;
            numberFree++;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "number " + number + " numberFree " + numberFree + " numberUsed " + numberUsed
                 + " nextGetFree " + nextGetFree + " nextSetUsed " + nextSetUsed
                 + " nextGetUsed " + nextGetUsed + " nextReleaseUsed " + nextReleaseUsed;
        }
    }
}
