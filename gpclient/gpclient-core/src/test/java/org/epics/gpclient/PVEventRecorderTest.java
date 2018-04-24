/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient;

import java.util.function.Consumer;
import org.epics.gpclient.PVEvent;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.mockito.InOrder;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
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
        
        Thread thread = new Thread(() -> {
            recorder.accept(PVEvent.valueEvent());
        });
        thread.start();
        
        
        recorder.wait(400, forAnEvent());
        
        assertThat(recorder.getEvents().size(), equalTo(1));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.valueEvent()));
    }
    
    @Test
    public void forEventCount1() throws InterruptedException {
        PVEventRecorder recorder = new PVEventRecorder();
        
        recorder.accept(PVEvent.valueEvent());

        Thread thread1 = new Thread(() -> {
            recorder.accept(PVEvent.valueEvent());
        });
        thread1.start();

        Thread thread2 = new Thread(() -> {
            recorder.accept(PVEvent.valueEvent());
        });
        thread2.start();
        
        recorder.wait(400, forEventCount(3));
        
        assertThat(recorder.getEvents().size(), equalTo(3));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.valueEvent()));
        assertThat(recorder.getEvents().get(1), equalTo(PVEvent.valueEvent()));
        assertThat(recorder.getEvents().get(2), equalTo(PVEvent.valueEvent()));
    }
}
