/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource;

import java.util.Collection;

/**
 * A set of type adapters. This optional class is provided to help
 * create a more flexible type support in a datasource, so that support
 * for individual types is done through runtime configuration.
 *
 * @author carcassi
 */
public interface DataSourceTypeAdapterSet {

    /**
     * Returns a collation of adapters. The collection must be
     * immutable.
     *
     * @return a collection; not null
     */
    Collection<? extends DataSourceTypeAdapter<?, ?>> getAdapters();
}
