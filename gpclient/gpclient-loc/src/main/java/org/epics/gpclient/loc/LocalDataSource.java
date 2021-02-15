/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.loc;

import org.epics.gpclient.datasource.ChannelHandler;
import org.epics.gpclient.datasource.DataSource;
import org.epics.gpclient.datasource.ReadSubscription;
import org.epics.gpclient.datasource.WriteSubscription;
import org.epics.util.text.FunctionParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Data source for locally written data. Each instance of this
 * data source will have its own separate channels and values.
 *
 * @author carcassi
 */
public class LocalDataSource extends DataSource {

    /**
     * Creates a new data source.
     */
    public LocalDataSource() {
    }

    private final String CHANNEL_SYNTAX_ERROR_MESSAGE =
            "Syntax for local channel must be either name, name(Double) or name(String) (e.g \"foo\", \"foo(2.0)\" or \"foo(\"bar\")";

    @Override
    protected ChannelHandler createChannel(String channelName) {
        // Parse the channel name
        List<Object> parsedTokens = parseName(channelName);

        LocalChannelHandler channel = new LocalChannelHandler(parsedTokens.get(0).toString());
        return channel;
    }

    private List<Object> parseName(String channelName) {
        List<Object> tokens = FunctionParser.parseFunctionWithScalarOrArrayArguments(".+", channelName, CHANNEL_SYNTAX_ERROR_MESSAGE);
        String nameAndType = tokens.get(0).toString();
        String name = nameAndType;
        String type = null;
        int index = nameAndType.lastIndexOf('<');
        if (nameAndType.endsWith(">") && index != -1) {
            name = nameAndType.substring(0, index);
            type = nameAndType.substring(index + 1, nameAndType.length() - 1);
        }
        List<Object> newTokens = new ArrayList<Object>();
        newTokens.add(name);
        newTokens.add(type);
        if (tokens.size() > 1) {
            newTokens.addAll(tokens.subList(1, tokens.size()));
        }
        return newTokens;
    }

    @Override
    protected String channelHandlerLookupName(String channelName) {
        List<Object> parsedTokens = parseName(channelName);
        return parsedTokens.get(0).toString();
    }

    private void initialize(final String channelName) {
        exec.submit(new Runnable() {
            public void run() {
                List<Object> parsedTokens = parseName(channelName);

                LocalChannelHandler channel = (LocalChannelHandler) getChannels().get(channelHandlerLookupName(channelName));
                channel.setType((String) parsedTokens.get(1));
                if (parsedTokens.size() > 2) {
                    channel.setInitialValue(parsedTokens.get(2));
                }
            }
        });
    }

    @Override
    public void startRead(ReadSubscription subscription) {
        super.startRead(subscription);

        // Initialize value
        initialize(subscription.getChannelName());
    }

    @Override
    public void stopWrite(WriteSubscription subscription) {
        super.stopWrite(subscription);

        // Initialize value
        initialize(subscription.getChannelName());
    }

}
