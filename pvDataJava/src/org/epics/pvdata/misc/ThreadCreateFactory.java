/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.misc;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Factory for creating threads.
 * @author mrk
 *
 */
public class ThreadCreateFactory {
    /**
     * Get the single instance of ThreadCreate.
     * @return threadCreate interface.
     */
    public static ThreadCreate getThreadCreate() {
        return threadCreate;
    }

    private static ThreadCreateImpl threadCreate = new ThreadCreateImpl();

    private static class ThreadCreateImpl implements ThreadCreate {

        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.ThreadCreate#create(java.lang.String, int, org.epics.pvdata.misc.RunnableReady)
         */
        public Thread create(String name, int priority, RunnableReady runnableReady) {
            RunnableImpl runnableImpl = new RunnableImpl(name,priority,runnableReady);
            return runnableImpl.start();
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.ThreadCreate#getThreads()
         */
        public synchronized Thread[] getThreads() {
            int length = threadList.size();
            Thread[] threads = new Thread[length];
            ListIterator<Thread> iter = threadList.listIterator();
            for(int i=0; i<length; i++) {
                threads[i] = iter.next();
            }
            return threads;
        }
        
        private synchronized void addThread(Thread thread) {
            if(threadList.contains(thread)) {
                throw new IllegalStateException("addThread but already on list");
            }
            threadList.add(thread);
        }
        
        private synchronized void removeThread(Thread thread) {
            threadList.remove(thread);
        }
        
        private List<Thread> threadList = new LinkedList<Thread>();

        private static class RunnableImpl implements Runnable,ThreadReady {

            private RunnableImpl(String name, int priority, RunnableReady runnable) {
                this.runnable = runnable;
                thread = new Thread(this,name);
                thread.setPriority(priority);
            }

            private Thread start() {
                thread.start();
                lock.lock();
                try {
                    if(!isReady) waitForReady.await(10, TimeUnit.SECONDS);
                } catch(InterruptedException e) {
                    System.err.println(
                            e.getMessage()
                            + " thread " + thread.getName() + " did not call ready");
                } finally {
                    lock.unlock();
                }
                return thread;
            }

            private RunnableReady runnable;
            private Thread thread;
            private ReentrantLock lock = new ReentrantLock();
            private Condition waitForReady = lock.newCondition();
            private volatile boolean isReady = false;
            /* (non-Javadoc)
             * @see java.lang.Runnable#run()
             */
            public void run() {
                threadCreate.addThread(thread);
                runnable.run(this);
                threadCreate.removeThread(thread);
            }
            /* (non-Javadoc)
             * @see org.epics.pvdata.misc.ThreadReady#ready()
             */
            public void ready() {
                lock.lock();
                try {
                    isReady = true;
                    waitForReady.signal();
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
