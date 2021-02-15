/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.gpclient.datasource.MultiplexedChannelHandler;
import org.epics.util.text.FunctionParser;
import org.epics.vtype.VType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implementation class for a channel that connects after a delay.
 *
 * @author carcassi
 */
class DelayedConnectionChannelHandler extends MultiplexedChannelHandler<Object, Object> {

    private final Object initialValue;
    private final double delayInSeconds;
    private final ScheduledExecutorService exec;

    DelayedConnectionChannelHandler(String channelName, ScheduledExecutorService exec) {
        super(channelName, true);
        String errorMessage = "Incorrect syntax. Must match delayedConnectionChannel(delayInSeconds, value)";
        List<Object> tokens = FunctionParser.parseFunctionAnyParameter(channelName);
        if (tokens == null || tokens.size() <= 1) {
            throw new IllegalArgumentException(errorMessage);
        }
        if (tokens.size() == 2) {
            initialValue = "Initial value";
        } else {
            Object value = FunctionParser.asScalarOrList(tokens.subList(2, tokens.size()));
            if (value == null) {
                throw new IllegalArgumentException(errorMessage);
            }
            initialValue = VType.toVTypeChecked(value);
        }
        delayInSeconds = (Double) tokens.get(1);
        this.exec = exec;
    }

    @Override
    public void connect() {
        exec.schedule(new Runnable() {

            public void run() {
                synchronized(DelayedConnectionChannelHandler.this) {
                    if (getUsageCounter() > 0) {
                        processConnection(new Object());
                        processMessage(initialValue);
                    }
                }
            }
        }, (long) delayInSeconds * 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void disconnect() {
        processConnection(null);
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("delayInSeconds", delayInSeconds);
        result.put("initialValue", initialValue);
        return result;
    }

    @Override
    public void write(Object newValue) {
        throw new UnsupportedOperationException("Can't write to simulation channel.");
    }

}
