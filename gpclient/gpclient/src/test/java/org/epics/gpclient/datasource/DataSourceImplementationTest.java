/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource;

import org.epics.gpclient.expression.*;
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
public class DataSourceImplementationTest {

    public DataSourceImplementationTest() {
    }
    
    @Test
    public void simpleSubscription1() throws InterruptedException {
        ProbeCollector probe = ProbeCollector.create();
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
        
        probe.wait(100000, forEventCount(2));
        
        assertThat(probe.getEvents().size(), equalTo(2));
        assertThat(probe.getEvents().get(0), equalTo(new ReadEvent(null, ReadEvent.Type.READ_CONNECTION)));
        assertThat(probe.getEvents().get(1), equalTo(new ReadEvent(null, ReadEvent.Type.VALUE)));
    }
    
    @Test
    public void simpleSubscription2() throws InterruptedException {
        ProbeCollector probe = ProbeCollector.create();
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
        
        probe.wait(100000, forAnEvent());
        
        assertThat(probe.getEvents().size(), equalTo(1));
        assertThat(probe.getEvents().get(0), equalTo(new ReadEvent(ex, ReadEvent.Type.READ_EXCEPTION)));
    }
}
