/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.epics.gpclient.ReadCollector;

/**
 * Builder class for {@link ReadRecipe}.
 *
 * @author carcassi
 */
public class ReadRecipeBuilder {

    private final List<ChannelReadRecipe> recipes = new ArrayList<>();

    public ReadRecipeBuilder addChannel(String channelName, ReadCollector<?, ?> collector) {
        recipes.add(new ChannelReadRecipe(channelName, new ChannelHandlerReadSubscription(collector)));
        return this;
    }

    /**
     * Builds the recipe.
     * 
     * @return a new recipe
     */
    public ReadRecipe build() {
        return new ReadRecipe(Collections.unmodifiableList(recipes));
    }
}
