/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.ca.types;

import org.epics.gpclient.ReadCollector;
import org.epics.gpclient.datasource.DataSourceTypeSupport;
import org.epics.gpclient.datasource.ca.CAConnectionPayload;

/**
 * Given a set of {@link CATypeAdapter} prepares type support for the
 * JCA data source.
 *
 * @author carcassi
 */
public class CATypeSupport extends DataSourceTypeSupport {

    private final CATypeAdapterSet adapters;

    /**
     * A new type support for the jca type support.
     *
     * @param adapters a set of jca adapters
     */
    public CATypeSupport(CATypeAdapterSet adapters) {
        this.adapters = adapters;
    }

    /**
     * Returns a matching type adapter for the given
     * cache and channel.
     *
     * @param cache the cache that will store the data
     * @param connection the ca channel
     * @return the matched type adapter
     */
    public CATypeAdapter find(ReadCollector<?, ?> cache, CAConnectionPayload connection) {
        return find(adapters.getAdapters(), cache, connection);
    }
}
