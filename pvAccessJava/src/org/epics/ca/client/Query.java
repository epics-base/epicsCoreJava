/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca.client;

/**
 * @author mse
 *
 */
public interface Query {
     ChannelProvider getChannelProvider();
     void cancelQuery();
}
