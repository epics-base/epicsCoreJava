/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.gpclient.datasource.MultiplexedChannelHandler;
import org.epics.util.text.FunctionParser;
import org.epics.vtype.VType;

import java.util.List;
import java.util.logging.Logger;

/**
 * Implementation for constant channel.
 *
 * @author carcassi
 */
class ConstantChannelHandler extends MultiplexedChannelHandler<Object, Object> {

    private static final Logger log = Logger.getLogger(ConstantChannelHandler.class.getName());

    public ConstantChannelHandler(String channelName) {
        super(channelName, true);
        List<Object> tokens = FunctionParser.parseFunctionWithScalarOrArrayArguments(channelName,
                "Wrong syntax. Correct examples: const(3.14), const(\"Bob\"), const(1,2,3), const(\"ON\", \"OFF\"");
        processMessage((Object) VType.toVTypeChecked(tokens.get(1)));
    }

    @Override
    public void connect() {
        processConnection(new Object());
    }

    @Override
    public void disconnect() {
        processConnection(null);
    }

    @Override
    protected boolean saveMessageAfterDisconnect() {
        return true;
    }

    @Override
    public void write(Object newValue) {
        throw new UnsupportedOperationException("Can't write to simulation channel.");
    }
}
