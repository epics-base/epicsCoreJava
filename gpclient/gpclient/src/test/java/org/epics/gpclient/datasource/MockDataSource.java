/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author carcassi
 */
public class MockDataSource extends DataSource {
    
    private final List<ChannelReadRecipe> connectedReadRecipes = new CopyOnWriteArrayList<>();
    private final List<ChannelWriteRecipe> connectedWriteRecipes = new CopyOnWriteArrayList<>();
    private volatile ChannelReadRecipe readRecipe;
    private volatile ChannelWriteRecipe writeRecipe;
    private volatile ChannelWriteRecipe writeRecipeForWrite;

    public MockDataSource() {
        super(true);
    }

    @Override
    protected ChannelHandler createChannel(String channelName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void connectRead(ChannelReadRecipe recipe) {
        this.readRecipe = recipe;
        connectedReadRecipes.add(recipe);
    }

    @Override
    public void disconnectRead(ChannelReadRecipe recipe) {
        this.readRecipe = recipe;
        connectedReadRecipes.remove(recipe);
    }

    @Override
    public void connectWrite(ChannelWriteRecipe writeRecipe) {
        this.writeRecipe = writeRecipe;
        connectedWriteRecipes.add(writeRecipe);
    }

    @Override
    public void disconnectWrite(ChannelWriteRecipe writeRecipe) {
        this.writeRecipe = writeRecipe;
        connectedWriteRecipes.remove(writeRecipe);
    }

    public ChannelReadRecipe getReadRecipe() {
        return readRecipe;
    }

    public ChannelWriteRecipe getWriteRecipe() {
        return writeRecipe;
    }

    public ChannelWriteRecipe getWriteRecipeForWrite() {
        return writeRecipeForWrite;
    }

    public List<ChannelReadRecipe> getConnectedReadRecipes() {
        return connectedReadRecipes;
    }

    public List<ChannelWriteRecipe> getConnectedWriteRecipes() {
        return connectedWriteRecipes;
    }
    
}
