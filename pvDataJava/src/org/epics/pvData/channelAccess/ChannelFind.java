/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

/**
 * @author mrk
 *
 */
public interface ChannelFind {
     ChannelProvider getChannelProvider();
     void cancelChannelFind();
}
