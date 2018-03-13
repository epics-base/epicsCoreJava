/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.expression;

import org.epics.gpclient.datasource.DataSource;
import org.epics.gpclient.datasource.ReadRecipe;
import org.epics.gpclient.datasource.ReadRecipeBuilder;
import org.epics.gpclient.datasource.WriteRecipe;
import org.epics.gpclient.datasource.WriteRecipeBuilder;

/**
 * Represents a channel, which can be both read or written.
 *
 * @param <R> type of the read payload
 * @param <W> type of the write payload
 * @author carcassi
 */
public class ChannelExpression<R, W> extends ExpressionImpl<R, W> {
    
    private final String channelName;
    private final ReadCollector<?, R> readCollector;
    private final WriteCollector<W> writeCollector;

    public ChannelExpression(String channelName, Class<R> readClass, Class<W> writeClass, ReadCollector<?, R> readCollector, WriteCollector<W> writeCollector) {
        super(readCollector, writeCollector);
        if (channelName == null) {
            throw new NullPointerException("Channel name can't be null");
        }
        this.channelName = channelName;
        this.readCollector = readCollector;
        this.writeCollector = writeCollector;
    }

    @Override
    public void startRead(Object director) {
        ReadRecipe recipe = new ReadRecipeBuilder().addChannel(channelName, readCollector).build();
        DataSource dataSource = null; // get the Datasource
        dataSource.connectRead(recipe);
    }

    @Override
    public void startWrite(Object director) {
        WriteRecipe recipe = new WriteRecipeBuilder().addChannel(channelName, writeCollector).build();
        DataSource dataSource = null; // get the Datasource
        dataSource.connectWrite(recipe);
    }
    
}
