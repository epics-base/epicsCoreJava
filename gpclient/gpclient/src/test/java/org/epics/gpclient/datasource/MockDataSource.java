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
    
    private final List<ReadSubscription> connectedReadRecipes = new CopyOnWriteArrayList<>();
    private final List<WriteSubscription> connectedWriteRecipes = new CopyOnWriteArrayList<>();
    private volatile ReadSubscription readRecipe;
    private volatile WriteSubscription writeRecipe;
    private volatile WriteSubscription writeRecipeForWrite;

    public MockDataSource() {
        super(true);
    }

    @Override
    protected ChannelHandler createChannel(String channelName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startRead(ReadSubscription recipe) {
        this.readRecipe = recipe;
        connectedReadRecipes.add(recipe);
    }

    @Override
    public void stopRead(ReadSubscription recipe) {
        this.readRecipe = recipe;
        connectedReadRecipes.remove(recipe);
    }

    @Override
    public void startWrite(WriteSubscription writeRecipe) {
        this.writeRecipe = writeRecipe;
        connectedWriteRecipes.add(writeRecipe);
    }

    @Override
    public void stopWrite(WriteSubscription writeRecipe) {
        this.writeRecipe = writeRecipe;
        connectedWriteRecipes.remove(writeRecipe);
    }

    public ReadSubscription getReadRecipe() {
        return readRecipe;
    }

    public WriteSubscription getWriteRecipe() {
        return writeRecipe;
    }

    public WriteSubscription getWriteRecipeForWrite() {
        return writeRecipeForWrite;
    }

    public List<ReadSubscription> getConnectedReadRecipes() {
        return connectedReadRecipes;
    }

    public List<WriteSubscription> getConnectedWriteRecipes() {
        return connectedWriteRecipes;
    }
    
}
