/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource;

import org.epics.gpclient.ProbeCollector;
import org.epics.gpclient.PVEvent;
import org.epics.gpclient.PVEventRecorder;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
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
        DataSource dataSource = new DataSource() {
            @Override
            protected ChannelHandler createChannel(final String channelName) {
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

        dataSource.startRead(new ReadSubscription("first", probe.getReadCollector()));

        recorder.wait(1000, forEventCount(2));

        assertThat(recorder.getEvents().size(), equalTo(2));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.readConnectionEvent()));
        assertThat(recorder.getEvents().get(1), equalTo(PVEvent.valueEvent()));
    }

    @Test
    public void simpleSubscription2() throws InterruptedException {
        ProbeCollector probe = ProbeCollector.create();
        PVEventRecorder recorder = probe.getRecorder();
        final RuntimeException ex = new RuntimeException("Connection problem");
        DataSource dataSource = new DataSource() {
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

        dataSource.startRead(new ReadSubscription("first", probe.getReadCollector()));

        recorder.wait(1000, forAnEvent());

        assertThat(recorder.getEvents().size(), equalTo(1));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.exceptionEvent(ex)));
    }

    @Test
    public void readWrite1() throws InterruptedException {
        ProbeCollector probe = ProbeCollector.create();
        PVEventRecorder recorder = probe.getRecorder();
        DataSource dataSource = new DataSource() {
            @Override
            protected ChannelHandler createChannel(String channelName) {
                return new MultiplexedChannelHandler(channelName) {
                    @Override
                    protected void connect() {
                        this.processConnection(true);
                        this.processMessage("Initial value");
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

        dataSource.startRead(new ReadSubscription("first", probe.getReadCollector()));
        dataSource.startWrite(new WriteSubscription("first", probe.getWriteCollector()));

        recorder.wait(1000, forEventCount(3));

        probe.writeValue("Second value");

        recorder.wait(1000, forEventCount(2));

        assertThat(recorder.getEvents().size(), equalTo(5));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.readConnectionEvent()));
        assertThat(recorder.getEvents().get(1), equalTo(PVEvent.valueEvent()));
        assertThat(recorder.getEvents().get(2), equalTo(PVEvent.writeConnectionEvent()));
        assertThat(recorder.getEvents().get(3), equalTo(PVEvent.valueEvent()));
        assertThat(recorder.getEvents().get(4), equalTo(PVEvent.writeSucceededEvent()));
    }

    @Test
    public void readWrite2() throws InterruptedException {
        ProbeCollector probe = ProbeCollector.create();
        PVEventRecorder recorder = probe.getRecorder();
        final RuntimeException ex = new RuntimeException("Read failed");
        DataSource dataSource = new DataSource() {
            @Override
            protected ChannelHandler createChannel(String channelName) {
                return new MultiplexedChannelHandler(channelName) {
                    @Override
                    protected void connect() {
                        this.processConnection(true);
                        this.processMessage("Initial value");
                    }

                    @Override
                    protected void disconnect() {
                    }

                    @Override
                    protected void write(Object newValue) {
                        throw ex;
                    }
                };
            }
        };

        dataSource.startRead(new ReadSubscription("first", probe.getReadCollector()));
        dataSource.startWrite(new WriteSubscription("first", probe.getWriteCollector()));

        recorder.wait(1000, forEventCount(3));

        probe.writeValue("Second value");

        recorder.wait(1000, forEventCount(1));

        assertThat(recorder.getEvents().size(), equalTo(4));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.readConnectionEvent()));
        assertThat(recorder.getEvents().get(1), equalTo(PVEvent.valueEvent()));
        assertThat(recorder.getEvents().get(2), equalTo(PVEvent.writeConnectionEvent()));
        assertThat(recorder.getEvents().get(3), equalTo(PVEvent.writeFailedEvent(ex)));
    }
}
