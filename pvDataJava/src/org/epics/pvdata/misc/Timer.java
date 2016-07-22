/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.misc;

/**
 * Schedule a callback to be called via a timer thread.
 * A Timer is created via TimerFactory.
 * @author mrk
 *
 */
public interface Timer {
    /**
     * An interface implemented by requester.
     *
     */
    public interface TimerCallback {
        /**
         * This is called when the timer expires of when the request was canceled.
         * If the call back is for a scheduled delay, the call back can again call a schedule method.
         * A request is canceled either because TimerNode.cancel() is called or because
         * Timer.stop() is called and the TimerNode is scheduled.
         */
        void callback();
        /**
         * The timer was stopped.
         * This is called if a request is queued when Timer.stop is called.
         */
        void timerStopped();
    }
    /**
     * An interface implemented by TimerFactory.
     * Code that wants to insert an element in a Timer must allocate a TimerNode.
     * A TimerNode can only appear in only one Timer at a time and at most once in a particular timer.
     */
    public interface TimerNode {
        /**
         * Cancel the current request.
         * The TimerNode can be reused by calling a schedule method.
         */
        void cancel();

        /**
         * Check if this TimerNode is being scheduled.
         *
         * @return the scheduled status flag
         */
        boolean isScheduled();
    }

    /**
     * Scheduler a call back after a specified delay.
     *
     * @param timerNode the TimerNode allocated via a call to TimerFactory.createNode
     * @param delay the delay in seconds
     */
    void scheduleAfterDelay(TimerNode timerNode,double delay);

    /**
     * Schedule a periodic call back.
     *
     * @param timerNode the TimerNode allocated via a call to createNode
     * @param delay the delay in seconds to the first callback
     * @param period the period in seconds
     */
    void schedulePeriodic(TimerNode timerNode,double delay,double period);

    /**
     * Stop the timer.
     */
    void stop();
}
