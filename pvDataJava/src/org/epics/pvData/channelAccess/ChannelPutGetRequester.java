/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

/**
 * Requester for a putGet request.
 * @author mrk
 *
 */
public interface ChannelPutGetRequester extends ChannelPutRequester,ChannelGetRequester {}
