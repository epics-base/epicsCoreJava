/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource;

import org.epics.gpclient.ReadCollector;

/**
 * Matches and sends notifies {@link ReadCollector}s with the data from connection and message payloads.
 * This optional class helps the writer of a datasource to manage the
 * type matching and conversions.
 *
 * @param <ConnectionPayload> the type of payload given at connection
 * @param <MessagePayload> the type of payload for each message
 * @author carcassi
 */
public interface DataSourceTypeAdapter<ConnectionPayload, MessagePayload> {

    /**
     * Determines whether the converter can take values from the channel
     * described by the connection payload and transform them in a
     * type required by the cache.
     *
     * @param cache the cache where data will need to be written
     * @param connection the connection information
     * @return zero if there is no match, or the position of the type matched
     */
    boolean match(ReadCollector<?, ?> cache, ConnectionPayload connection);

    /**
     * The parameters required to open a monitor for the channel. The
     * type of the parameters will be datasource specific.
     * <p>
     * For channels multiplexed on a single subscription, this method
     * is never used.
     *
     * @param cache the cache where data will need to be written
     * @param connection the connection information
     * @return datasource specific subscription information
     */
    Object getSubscriptionParameter(ReadCollector<?, ?> cache, ConnectionPayload connection);

    /**
     * Takes the information in the message and updates the cache.
     *
     * @param cache cache to be updated
     * @param connection the connection information
     * @param message the payload of each message
     */
    void updateCache(ReadCollector<?, ?> cache, ConnectionPayload connection, MessagePayload message);
}
