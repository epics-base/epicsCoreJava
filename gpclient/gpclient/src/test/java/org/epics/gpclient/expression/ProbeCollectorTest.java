/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.expression;

import java.util.function.Consumer;
import org.epics.gpclient.expression.LatestValueCollector;
import org.epics.gpclient.expression.ReadEvent;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.mockito.InOrder;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.epics.gpclient.expression.ProbeCollector.*;

/**
 *
 * @author carcassi
 */
public class ProbeCollectorTest {

    public ProbeCollectorTest() {
    }
    
    @Test
    public void forAnEvent1() throws InterruptedException {
        ProbeCollector probe = ProbeCollector.create();
        
        probe.getCollector().updateValue(new Object());
        
        probe.wait(400, forAnEvent());
        
        assertThat(probe.getEvents().size(), equalTo(1));
        assertThat(probe.getEvents().get(0), equalTo(new ReadEvent(null, ReadEvent.Type.VALUE)));
    }
    
    @Test(expected = AssertionError.class)
    public void forAnEvent2() {
        ProbeCollector probe = ProbeCollector.create();
        
        probe.wait(400, forAnEvent());
    }
    
    @Test
    public void forAnEvent3() {
        final ProbeCollector probe = ProbeCollector.create();
        
        Thread thread = new Thread(() -> {
            probe.getCollector().updateValue(new Object());
        });
        thread.start();
        
        
        probe.wait(400, forAnEvent());
        
        assertThat(probe.getEvents().size(), equalTo(1));
        assertThat(probe.getEvents().get(0), equalTo(new ReadEvent(null, ReadEvent.Type.VALUE)));
    }
    
    @Test
    public void forEventCount1() throws InterruptedException {
        ProbeCollector probe = ProbeCollector.create();
        
        probe.getCollector().updateValue(new Object());

        Thread thread1 = new Thread(() -> {
            probe.getCollector().updateValue(new Object());
        });
        thread1.start();

        Thread thread2 = new Thread(() -> {
            probe.getCollector().updateValue(new Object());
        });
        thread2.start();
        
        probe.wait(400, forEventCount(3));
        
        assertThat(probe.getEvents().size(), equalTo(3));
        assertThat(probe.getEvents().get(0), equalTo(new ReadEvent(null, ReadEvent.Type.VALUE)));
        assertThat(probe.getEvents().get(0), equalTo(new ReadEvent(null, ReadEvent.Type.VALUE)));
        assertThat(probe.getEvents().get(0), equalTo(new ReadEvent(null, ReadEvent.Type.VALUE)));
    }
}
