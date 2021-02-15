/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.monitor;

import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.misc.Queue;
import org.epics.pvdata.misc.QueueCreate;
import org.epics.pvdata.misc.QueueElement;
import org.epics.pvdata.pv.PVStructure;

/**
 * Factory that creates a MonitorQueue.
 * @author mrk
 *
 */
public class MonitorQueueFactory {

    /**
     * Create a MonitorQueue.
     *
     * @param monitorElements a MonitorElement array. Each monitorElement
     * must becreated by calling MonitorQueueFactory.createMonitorElement.
     * @return the MonitorQueue interface.
     * @throws IllegalStateException if the queue size is not at least 2
     */
    public static MonitorQueue create(MonitorElement[] monitorElements) {
        int length = monitorElements.length;
        if(length<2) {
            throw new IllegalStateException("queueSize must be at least 2 ");
        }
        QueueElement<MonitorElement>[] queueElements = new QueueElement[length];
        for(int i=0; i<length; i++) {
            MonitorElementImlp monitorElement = (MonitorElementImlp)monitorElements[i];
            QueueElement<MonitorElement> queueElement = queueCreate.createQueueElement(monitorElement);
            monitorElement.setQueueElement(queueElement);
            queueElements[i] = queueElement;
        }
        Queue<MonitorElement> queue = queueCreate.create(queueElements);
        return new MonitorQueueImpl(queue);
    }

    /**
     * Create a MonitorElement.
     *
     * @param pvStructure The data structure for the monitorElement
     * @return the monitorElement
     */
    public static MonitorElement createMonitorElement(PVStructure pvStructure) {
        return new MonitorElementImlp(pvStructure);
    }

    private static final QueueCreate<MonitorElement> queueCreate = new QueueCreate<MonitorElement>();

    private static class MonitorElementImlp implements MonitorElement {

    	MonitorElementImlp(PVStructure pvStructure) {
    		this.pvStructure = pvStructure;
    		if(pvStructure!=null) {
    			int numberFields = pvStructure.getNumberFields();
    			changedBitSet = new BitSet(numberFields);
    			overrunBitSet = new BitSet(numberFields);
    		} else {
    			changedBitSet = null;
    			overrunBitSet = null;
    		}
    	}

        private final PVStructure pvStructure;
        private final BitSet changedBitSet;
        private final BitSet overrunBitSet;
        private QueueElement<MonitorElement> queueElement = null;

        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue.MonitorQueueElement#getChangedBitSet()
         */
        public BitSet getChangedBitSet() {
            return changedBitSet;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue.MonitorQueueElement#getOverrunBitSet()
         */
        public BitSet getOverrunBitSet() {
            return overrunBitSet;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue.MonitorQueueElement#getPVStructure()
         */
        public PVStructure getPVStructure() {
            return pvStructure;
        }

        private void setQueueElement(QueueElement<MonitorElement> queueElement) {
            this.queueElement = queueElement;
        }

        private QueueElement<MonitorElement> getQueueElement() {
            return queueElement;
        }
    }

    private static class MonitorQueueImpl implements MonitorQueue {
        private final Queue<MonitorElement> queue;

        MonitorQueueImpl(Queue<MonitorElement> queue) {
           this.queue = queue;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue#clear()
         */
        public void clear() {
           queue.clear();
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue#capacity()
         */
        public int capacity() {
            return queue.capacity();
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.MonitorQueue#getNumberFree()
         */
        public int getNumberFree() {
            return queue.getNumberFree();
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.monitor.MonitorQueue#getFree()
         */
        public MonitorElement getFree() {
            QueueElement<MonitorElement> queueElement = queue.getFree();
            if(queueElement==null) return null;
            return queueElement.getObject();
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.monitor.MonitorQueue#setUsed(org.epics.pvdata.monitor.MonitorElement)
         */
        public void setUsed(MonitorElement monitorElement) {
            MonitorElementImlp temp = (MonitorElementImlp)monitorElement;
            queue.setUsed(temp.getQueueElement());
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.monitor.MonitorQueue#getUsed()
         */
        public MonitorElement getUsed() {
            QueueElement<MonitorElement> queueElement = queue.getUsed();
            if(queueElement==null) return null;
            return queueElement.getObject();
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.monitor.MonitorQueue#releaseUsed(org.epics.pvdata.monitor.MonitorElement)
         */
        public void releaseUsed(MonitorElement monitorElement) {
            MonitorElementImlp temp = (MonitorElementImlp)monitorElement;
            queue.releaseUsed(temp.getQueueElement());
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return queue.toString();
        }
    }
}
