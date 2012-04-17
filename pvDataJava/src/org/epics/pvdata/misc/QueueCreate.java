/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.misc;

/**
 * @author mrk
 *
 */
public class QueueCreate<T> {
    
    public Queue<T> create(QueueElement<T>[] queueElements) {
        return new QueueImpl<T>(queueElements);
    }
    
    public QueueElement<T> createQueueElement(T object) {
        return new QueueElementImpl<T>(object);
    }
    
    
    private static class QueueImpl<T> implements Queue<T> {
        
        private final QueueElement<T>[] queueElements;
        private final int number;
        private int numberFree = 0;
        private int numberUsed = 0;
        private int nextGetFree = 0;
        private int nextSetUsed = 0;
        private int nextGetUsed = 0;
        private int nextReleaseUsed = 0;

        
        QueueImpl (QueueElement<T>[] queueElements) {
            this.queueElements = queueElements;
            number = queueElements.length;
            numberFree = number;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.Queue#clear()
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
         * @see org.epics.pvdata.misc.Queue#capacity()
         */
        @Override
        public int capacity() {
            return number;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.Queue#getNumberFree()
         */
        @Override
        public int getNumberFree() {
            return numberFree;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.Queue#getFree()
         */
        @Override
        public QueueElement<T> getFree() {
            if(numberFree==0) return null;
            numberFree--;
            QueueElement<T> queueElement = queueElements[nextGetFree++];
            if(nextGetFree>=number) nextGetFree = 0;
            return queueElement;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.Queue#getUsed()
         */
        @Override
        public QueueElement<T> getUsed() {
            if(numberUsed==0) return null;
            QueueElement<T> queueElement = queueElements[nextGetUsed++];
            if(nextGetUsed>=number) nextGetUsed = 0;
            return queueElement;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.Queue#releaseUsed(org.epics.pvdata.misc.QueueElement)
         */
        @Override
        public void releaseUsed(QueueElement<T> queueElement) {
            if(queueElement!=queueElements[nextReleaseUsed++]) {
                throw new IllegalStateException("not queueElement returned by last call to getUsed");
            }
            if(nextReleaseUsed>=number) nextReleaseUsed = 0;
            numberUsed--;
            numberFree++;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.Queue#setUsed(org.epics.pvdata.misc.QueueElement)
         */
        @Override
        public void setUsed(QueueElement<T> queueElement) {
            if(queueElement!=queueElements[nextSetUsed++]) {
                throw new IllegalStateException("not correct queueElement");
            }
            numberUsed++;
            if(nextSetUsed>=number) nextSetUsed = 0;
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
    
    private static class QueueElementImpl<T> implements QueueElement<T> {
        private T object = null;
        
        QueueElementImpl(T object) {
            this.object = object;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.QueueElement#getObject()
         */
        @Override
        public T getObject() {
            return object;
        }
    }

}
