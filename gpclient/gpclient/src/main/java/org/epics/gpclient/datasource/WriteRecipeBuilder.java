/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.epics.gpclient.WriteCollector;

/**
 * A builder for {@link WriteRecipe }.
 *
 * @author carcassi
 */
public class WriteRecipeBuilder {

    private final Collection<ChannelWriteRecipe> recipes = new ArrayList<>();
    
    public WriteRecipeBuilder addChannel(String channelName, WriteCollector<?> collector) {
        recipes.add(new ChannelWriteRecipe(channelName, new ChannelHandlerWriteSubscription(collector)));
        return this;
    }

    /**
     * Builds the recipe.
     * @return a new WriteRecipe
     */
    public WriteRecipe build() {
        return new WriteRecipe(Collections.unmodifiableCollection(recipes));
    }
    
}
