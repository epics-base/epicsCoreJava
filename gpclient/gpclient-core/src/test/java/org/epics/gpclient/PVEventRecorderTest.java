/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.epics.gpclient.PVEventRecorder.*;

/**
 *
 * @author carcassi
 */
public class PVEventRecorderTest {

    public PVEventRecorderTest() {
    }

    @Test
    public void forAnEvent1() throws InterruptedException {
        PVEventRecorder recorder = new PVEventRecorder();

        recorder.accept(PVEvent.valueEvent());

        recorder.wait(400, forAnEvent());

        assertThat(recorder.getEvents().size(), equalTo(1));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.valueEvent()));
    }

    @Test(expected = AssertionError.class)
    public void forAnEvent2() {
        PVEventRecorder recorder = new PVEventRecorder();

        recorder.wait(400, forAnEvent());
    }

    @Test
    public void forAnEvent3() {
        final PVEventRecorder recorder = new PVEventRecorder();

        Thread thread = new Thread(new Runnable() {
            public void run() {
                recorder.accept(PVEvent.valueEvent());
            }
        });
        thread.start();


        recorder.wait(400, forAnEvent());

        assertThat(recorder.getEvents().size(), equalTo(1));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.valueEvent()));
    }

    @Test
    public void forEventCount1() throws InterruptedException {
        final PVEventRecorder recorder = new PVEventRecorder();

        recorder.accept(PVEvent.valueEvent());

        Thread thread1 = new Thread(new Runnable() {
            public void run() {
                recorder.accept(PVEvent.valueEvent());
            }
        });
        thread1.start();

        Thread thread2 = new Thread(new Runnable() {
            public void run() {
                recorder.accept(PVEvent.valueEvent());
            }
        });
        thread2.start();

        recorder.wait(400, forEventCount(3));

        assertThat(recorder.getEvents().size(), equalTo(3));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.valueEvent()));
        assertThat(recorder.getEvents().get(1), equalTo(PVEvent.valueEvent()));
        assertThat(recorder.getEvents().get(2), equalTo(PVEvent.valueEvent()));
    }
}
