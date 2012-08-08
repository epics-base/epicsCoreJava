/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata;

import junit.framework.TestCase;

import org.epics.pvdata.misc.ThreadPriority;
import org.epics.pvdata.misc.Timer;
import org.epics.pvdata.misc.TimerFactory;

/**
 * JUnit test for Timer.
 * @author mrk
 *
 */
public class TimerTest extends TestCase {
    /**
     * test timer.
     */
    public static void testTimer() {
        int number = 10;
        double delayInc = .1;
        double delay = delayInc;
        TestDelay[] testDelays = new TestDelay[number];
        TestPeriodic[] testPeriodics = new TestPeriodic[number];
        for(int i=0; i<number; i++) {
            testDelays[i] = new TestDelay(delay);
            testPeriodics[i] = new TestPeriodic(delay);
            timer.scheduleAfterDelay(testDelays[i].timerNode, delay);
            timer.schedulePeriodic(testPeriodics[i].timerNode, .1, delay);
            delay += delayInc;
        }
       try {
           Thread.sleep(800);
       } catch (InterruptedException e) {}
       System.out.println();
       System.out.println("canceling first 5");
       for(int i=0; i<6; i++) {
           TestDelay testDelay = testDelays[i];
           testDelay.isCanceled = true;
           testDelay.timerNode.cancel();
           TestPeriodic testPeriodic = testPeriodics[i];
           testPeriodic.isCanceled = true;
           testPeriodic.timerNode.cancel();
       }
       try {
           Thread.sleep(200);
       } catch (InterruptedException e) {}
       System.out.println();
       System.out.println("adding back 5th");
       TestDelay testDelay = testDelays[5];
       testDelay.isCanceled = false;
       timer.scheduleAfterDelay(testDelay.timerNode,testDelay.delay);
       TestPeriodic testPeriodic = testPeriodics[5];
       testPeriodic.isCanceled = false;
       timer.schedulePeriodic(testPeriodic.timerNode, .1, testPeriodic.period);
       try {
           Thread.sleep(1000);
       } catch (InterruptedException e) {}
       System.out.println();
       System.out.println("calling stop");
       timer.stop();
    }
    
    private static final Timer timer = TimerFactory.create("testTimer", ThreadPriority.high);
    
    private static class TestDelay implements Timer.TimerCallback {
        private double delay;
        private Timer.TimerNode timerNode = TimerFactory.createNode(this);
        private boolean isCanceled = false;
        
        private TestDelay(double delay) {
            this.delay = delay;
        }
       
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.Timer.TimerCallback#callback()
         */
        public void callback() {
            System.out.println("   TestDelay.callback isCanceled " + isCanceled + " delay " + delay);
            if(!isCanceled) {
                timer.scheduleAfterDelay(timerNode, delay);
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.Timer.TimerCallback#timerStopped()
         */
        public void timerStopped() {
            System.out.println("   TestDelay.timerStopped delay " + delay);
            
        }
    }
    
    private static class TestPeriodic implements Timer.TimerCallback {
        private double period;
        private Timer.TimerNode timerNode = TimerFactory.createNode(this);
        private boolean isCanceled = false;

        private TestPeriodic(double period) {
            this.period = period;
        }

        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.Timer.TimerCallback#callback()
         */
        public void callback() {
            System.out.println("TestPeriodic.callback isCanceled " + isCanceled + " period " + period);
        }
        
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.Timer.TimerCallback#timerStopped()
         */
        public void timerStopped() {
            System.out.println("TestPeriodic.timerStopped " + period);
        }
    }
}

