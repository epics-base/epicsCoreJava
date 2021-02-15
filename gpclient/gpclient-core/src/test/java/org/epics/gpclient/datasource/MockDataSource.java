/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author carcassi
 */
public class MockDataSource extends DataSource {

    private final List<ReadSubscription> connectedReadRecipes = new CopyOnWriteArrayList<ReadSubscription>();
    private final List<WriteSubscription> connectedWriteRecipes = new CopyOnWriteArrayList<WriteSubscription>();
    private volatile ReadSubscription readRecipe;
    private volatile WriteSubscription writeRecipe;
    private volatile WriteSubscription writeRecipeForWrite;

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
