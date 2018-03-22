/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource;

import org.epics.gpclient.ProbeCollector;
import org.epics.gpclient.expression.*;
import java.util.function.Consumer;
import org.epics.gpclient.LatestValueCollector;
import org.epics.gpclient.PVEvent;
import org.epics.gpclient.PVEventRecorder;
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
public class DataSourceImplementationTest {

    public DataSourceImplementationTest() {
    }
    
    @Test
    public void simpleSubscription1() throws InterruptedException {
        ProbeCollector probe = ProbeCollector.create();
        PVEventRecorder recorder = probe.getRecorder();
        DataSource dataSource = new DataSource(false) {
            @Override
            protected ChannelHandler createChannel(String channelName) {
                return new MultiplexedChannelHandler(channelName) {
                    @Override
                    protected void connect() {
                        this.processConnection(true);
                        this.processMessage("Value for " + channelName);
                    }
                    
                    @Override
                    protected void disconnect() {
                    }
                    
                    @Override
                    protected void write(Object newValue) {
                        processMessage(newValue);
                    }
                };
            }
        };
        
        dataSource.connectRead(new ReadRecipeBuilder().addChannel("first", probe.getCollector()).build());
        
        recorder.wait(100000, forEventCount(2));
        
        assertThat(recorder.getEvents().size(), equalTo(2));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.connectionEvent()));
        assertThat(recorder.getEvents().get(1), equalTo(PVEvent.valueEvent()));
    }
    
    @Test
    public void simpleSubscription2() throws InterruptedException {
        ProbeCollector probe = ProbeCollector.create();
        PVEventRecorder recorder = probe.getRecorder();
        RuntimeException ex = new RuntimeException("Connection problem");
        DataSource dataSource = new DataSource(false) {
            @Override
            protected ChannelHandler createChannel(String channelName) {
                return new MultiplexedChannelHandler(channelName) {
                    @Override
                    protected void connect() {
                        throw ex;
                    }
                    
                    @Override
                    protected void disconnect() {
                    }
                    
                    @Override
                    protected void write(Object newValue) {
                        processMessage(newValue);
                    }
                };
            }
        };
        
        dataSource.connectRead(new ReadRecipeBuilder().addChannel("first", probe.getCollector()).build());
        
        recorder.wait(100000, forAnEvent());
        
        assertThat(recorder.getEvents().size(), equalTo(1));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.exceptionEvent(ex)));
    }
}
