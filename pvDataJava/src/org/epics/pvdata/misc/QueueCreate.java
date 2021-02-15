/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.misc;

/**
 * A factory for creating a finite queue.
 * @author mrk
 *
 */
public class QueueCreate<T> {

    /**
     * Create a Queue.
     *
     * @param queueElements the queue elements
     * @return the Queue
     */
    public Queue<T> create(QueueElement<T>[] queueElements) {
        return new QueueImpl<T>(queueElements);
    }

    /**
     * Create a QueueElement.
     *
     * @param object the data the queueElement contains
     * @return the QueueElement
     */
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
        public int capacity() {
            return number;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.Queue#getNumberFree()
         */
        public int getNumberFree() {
            return numberFree;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.Queue#getFree()
         */
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
        public QueueElement<T> getUsed() {
            if(numberUsed==0) return null;
            QueueElement<T> queueElement = queueElements[nextGetUsed++];
            if(nextGetUsed>=number) nextGetUsed = 0;
            return queueElement;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.Queue#releaseUsed(org.epics.pvdata.misc.QueueElement)
         */
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
        public T getObject() {
            return object;
        }
    }

}
