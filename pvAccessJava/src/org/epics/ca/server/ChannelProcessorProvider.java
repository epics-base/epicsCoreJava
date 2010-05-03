/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca.server;

import org.epics.pvData.pv.PVRecord;

/**
 * Interface implemented by code that can process a record,
 * @author mrk
 *
 */
public interface ChannelProcessorProvider {
    /**
     * Request to be the processor for a record.
     * @param pvRecord The record.
     * @param channelProcessorRequester The requester.
     * @return The ChannelProcess interface or null if the requester
     * can not process this record.
     */
    ChannelProcessor requestChannelProcessor(PVRecord pvRecord,ChannelProcessorRequester channelProcessorRequester);
}
