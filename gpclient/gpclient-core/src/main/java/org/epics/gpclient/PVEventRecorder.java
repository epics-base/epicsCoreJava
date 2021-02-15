/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.epics.util.compat.legacy.functional.Consumer;
import org.epics.util.compat.legacy.functional.Function;

/**
 * Utility class to record the flow of events mainly for debugging and testing
 * purposes. It allows to add test conditions that lock the thread until
 * the conditions is verified. It also allows to add some logic that is
 * executed when each event is received, prior to checking the test condition.
 *
 * @author carcassi
 */
public class PVEventRecorder extends Consumer<PVEvent> {

    private final Object lock = new Object();
    private final List<PVEvent> events = new ArrayList<PVEvent>();
    private Runnable test;

    @Override
    public final void accept(PVEvent event) {
        synchronized (lock) {
            events.add(event);
            onEvent(event);
            if (test != null) {
                test.run();
            }
        }
    }

    /**
     * Custom logic executed at each event. This method is executed after the event
     * is added to the list but before the check is performed.
     *
     * @param event a new event
     */
    protected void onEvent(PVEvent event) {
    }

    /**
     * Returns all the events collected so far.
     *
     * @return the events in the order recorded
     */
    public List<PVEvent> getEvents() {
        synchronized(lock) {
            return events;
        }
    }

    /**
     * Waits until the condition is met. If the conditions is already met, it
     * returns right away. If the condition is not met after the time specified,
     * an {@link AssertionError} is thrown.
     *
     * @param ms the timeout in millis
     * @param condition the condition
     */
    public void wait(int ms, final Function<List<PVEvent>, Boolean> condition) {
        final CountDownLatch latch = new CountDownLatch(1);
        Runnable newTest = new Runnable() {
            public void run() {
                if (condition.apply(events)) {
                    latch.countDown();
                }
            }
        };
        synchronized(lock) {
            test = newTest;
            test.run();
        }

        boolean success = false;

        try {
            success = latch.await(ms, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        }

        if (!success) {
            throw new AssertionError("Didn't receive " + condition + " within " + ms + " ms");
        }
    }

    /**
     * Checks that the condition is not met. The method returns successfully
     * only if the condition is not met within the timeout. If the conditions is
     * already met, or if it is met within the time specified, an
     * {@link AssertionError} is thrown.
     *
     * @param ms the timeout in millis
     * @param condition the condition
     */
    public void dontExpect(int ms, final Function<List<PVEvent>, Boolean> condition) {
        final CountDownLatch latch = new CountDownLatch(1);
        Runnable newTest = new Runnable() {
            public void run() {
                if (condition.apply(events)) {
                    latch.countDown();
                }
            }
        };
        synchronized(lock) {
            test = newTest;
            test.run();
        }

        boolean success = false;

        try {
            success = latch.await(ms, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        }

        if (success) {
            throw new AssertionError("Received " + condition + " against expectation");
        }
    }

    /**
     * Returns an {@link AssertionError} if the condition is not already met.
     *
     * @param condition the condition
     */
    public void hasReceived(Function<List<PVEvent>, Boolean> condition) {
        if (!condition.apply(events)) {
            throw new AssertionError("Didn't receive " + condition);
        }
    }

    /**
     * Returns an {@link AssertionError} if the condition is already met.
     *
     * @param condition the condition
     */
    public void hasNotReceived(Function<List<PVEvent>, Boolean> condition) {
        if (condition.apply(events)) {
            throw new AssertionError("Received " + condition);
        }
    }

    /**
     * Checks that any event was received.
     *
     * @return any event condition
     */
    public static Function<List<PVEvent>, Boolean> forAnEvent() {
        return new Function<List<PVEvent>, Boolean>() {
            @Override
            public Boolean apply(List<PVEvent> list) {
                return !list.isEmpty();
            }

            @Override
            public String toString() {
                return "an event";
            }
        };
    }

    /**
     * Checks that a connection event was received.
     *
     * @return connection event condition
     */
    public static Function<List<PVEvent>, Boolean> forAConnectionEvent() {
        return anEventOfType(PVEvent.Type.READ_CONNECTION);
    }

    /**
     * Checks that an event of the given type was received.
     *
     * @param type the event type
     * @return type of event condition
     */
    public static Function<List<PVEvent>, Boolean> anEventOfType(final PVEvent.Type type) {
        return new Function<List<PVEvent>, Boolean>() {
            @Override
            public Boolean apply(List<PVEvent> list) {
                return !list.isEmpty() && list.get(list.size() - 1).getType().contains(type);
            }

            @Override
            public String toString() {
                return "a " + type + " event";
            }
        };
    }

    /**
     * Checks the given number of events were received.
     *
     * @param count the number of events
     * @return event count condition
     */
    public static Function<List<PVEvent>, Boolean> forEventCount(final int count) {
        return new Function<List<PVEvent>, Boolean>() {
            @Override
            public Boolean apply(List<PVEvent> list) {
                return list.size() >= count;
            }

            @Override
            public String toString() {
                return count + " events";
            }
        };
    }

}
