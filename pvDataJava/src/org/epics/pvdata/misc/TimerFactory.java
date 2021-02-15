/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.misc;

import java.util.Comparator;
import java.util.PriorityQueue;

import org.epics.pvdata.misc.Timer.TimerCallback;
import org.epics.pvdata.misc.Timer.TimerNode;

/**
 * Factory that creates a Timer.
 * This implementation of a Timer provides the following features not provided by java.util.Timer
 * 1) Timer thread has a priority and 2) A TimerNode can be reused.
 * @author mrk
 *
 */
public class TimerFactory {
    /**
     * Create and return a Timer.
     *
     * @param threadName the thread name for the timer thread
     * @param priority the priority for the timer thread
     * @return the new timer
     */
    static public Timer create(String threadName, ThreadPriority priority) {
        return new TimerInstance(threadName,priority);
    }

    /**
     * Create a TimerNode that can be passed to the schedule methods.
     *
     * @param timerCallback the callbacks called when the timer expires or is stopped
     * @return the TimerNode created
     */
    static public TimerNode createNode(TimerCallback timerCallback) {
        return new TimerNodeImpl(timerCallback);
    }

    static private final ThreadCreate threadCreate = ThreadCreateFactory.getThreadCreate();

    static private class TimerInstance implements Timer, Comparator<TimerNodeImpl> {
        private PriorityQueue<TimerNodeImpl> queue = new PriorityQueue<TimerNodeImpl>(16,this);
        private ThreadInstance thread;
        private volatile boolean isStopped = false;

        private TimerInstance(String threadName, ThreadPriority priority) {
            thread = new ThreadInstance(threadName,priority.getJavaPriority());
        }
        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(TimerNodeImpl arg0, TimerNodeImpl arg1) {
            long a = arg0.timeToRun;
            long b = arg1.timeToRun;
            return (a < b)? -1 : ((a == b)? 0 : 1);
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.Timer#scheduleAfterDelay(org.epics.pvdata.misc.Timer.TimerNode, double)
         */
        public void scheduleAfterDelay(TimerNode timerNode, double delay) {
            schedulePeriodic(timerNode,delay,-.1);
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.Timer#schedulePeriodic(org.epics.pvdata.misc.Timer.TimerNode, double, double)
         */
        public void schedulePeriodic(TimerNode timerNode, double delayDouble,double periodDouble) {
            long delay = (long)(delayDouble*1000.0);
            long period = (long)(periodDouble*1000.0);
            TimerNodeImpl timerNodeImpl = (TimerNodeImpl)timerNode;
            if(timerNodeImpl.isQueued) {
                throw new IllegalStateException("already scheduled");
            }
            if(isStopped) {
                timerNodeImpl.timerCallback.timerStopped();
                return;
            }
            timerNodeImpl.isCanceled = false;
            if(delay<0) delay = 0;
            timerNodeImpl.timeToRun = System.currentTimeMillis() + delay;
            if(period<0) period = 0;
            timerNodeImpl.period = period;
            boolean isFirst = false;
            synchronized(queue) {
                if(!timerNodeImpl.isCanceled) {
                    timerNodeImpl.isQueued = true;
                    timerNodeImpl.timerInstance = this;
                    queue.add(timerNodeImpl);
                    TimerNodeImpl first = queue.peek();
                    if(first==timerNodeImpl) isFirst = true;
                }
            }
            if(isFirst) thread.wakeUp();
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.Executor#stop()
         */
        public void stop() {
            isStopped = true;
            thread.stop();
            TimerCallback timerCallback = null;
            while(true) {
                synchronized(queue) {
                    TimerNodeImpl timerNode = queue.poll();
                    if(timerNode==null) return;
                    timerCallback = timerNode.timerCallback;
                }
                timerCallback.timerStopped();
            }
        }


        private class ThreadInstance implements RunnableReady {

            private volatile boolean alive = true;

            private ThreadInstance(String name,int priority) {
                threadCreate.create(name, priority, this);
            }
            /* (non-Javadoc)
             * @see org.epics.pvdata.misc.RunnableReady#run(org.epics.pvdata.misc.ThreadReady)
             */
            public void run(ThreadReady threadReady) {
                threadReady.ready();
                while(alive) {
                    try {
                        long currentTime = System.currentTimeMillis();
                        long timeToRun = currentTime + 1000000;
                        long period = 0;
                        TimerNodeImpl nodeToCall = null;
                        synchronized(queue) {
                            TimerNodeImpl timerNode = queue.peek();
                            if(timerNode!=null) {
                                timeToRun = timerNode.timeToRun;
                                if(timeToRun<=currentTime) {
                                    if(!timerNode.isCanceled) {
                                        nodeToCall = timerNode;
                                    }
                                    queue.poll();
                                    period = timerNode.period;
                                    if(period>0 && !timerNode.isCanceled) {
                                        timerNode.timeToRun = currentTime + period;
                                        queue.add(timerNode);
                                    } else {
                                        timerNode.isQueued = false;
                                        timerNode.timerInstance = null;
                                    }
                                    timerNode = queue.peek();
                                    if(timerNode!=null) {
                                        timeToRun = timerNode.timeToRun;
                                    } else {
                                        timeToRun = currentTime + 1000000;
                                    }
                                }
                            }
                        }
                        if(nodeToCall!=null) {
                            nodeToCall.timerCallback.callback();
                        }
                        long delay = timeToRun - currentTime;
                        if(alive && delay>0) {
                        	synchronized (this) {
                        		if (!wokenUp)
                        			this.wait(delay);
                        		wokenUp = false;
                        	}
                        }
                    }catch(InterruptedException e) {}
                }
            }

            private boolean wokenUp = false;
            private void wakeUp() {
            	synchronized (this) {
	                wokenUp = true;
            		this.notifyAll();
            	}
            }

            private void stop() {
            	synchronized (this) {
	                alive = false;
	                wokenUp = true;
	                this.notifyAll();
            	}
            }
        }
    }

    private static class TimerNodeImpl implements Timer.TimerNode {
        private TimerInstance timerInstance = null;
        private TimerCallback timerCallback;
        private long timeToRun = 0;
        private long period = 0;
        private volatile boolean isQueued = false;
        private volatile boolean isCanceled = false;

        private TimerNodeImpl(TimerCallback timerCallback) {
            this.timerCallback = timerCallback;
        }

        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.Timer.TimerNode#cancel()
         */
        public void cancel() {
            isCanceled = true;
            TimerInstance timerInstance = this.timerInstance;
            if(timerInstance!=null) {
                synchronized(timerInstance.queue) {
                    timerInstance.queue.remove(this);
                    isQueued = false;
                }
            }
        }

		/* (non-Javadoc)
		 * @see org.epics.pvdata.misc.Timer.TimerNode#isScheduled()
		 */
		public boolean isScheduled() {
			return isQueued;
		}


    }
}
