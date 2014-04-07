/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

/**
 * Request to put and get Array Data.
 * The data is either taken from or put in the PVArray returned by ChannelArrayRequester.channelArrayConnect.
 * @author mrk
 *
 */
public interface ChannelArray extends ChannelRequest{
    /**
     * put to the remote array.
     * @param lastRequest Is this the last request.
     * @param offset The offset in the remote array, i.e. the PVArray returned by ChannelArrayRequester.channelArrayConnect.
     * @param count The number of elements to put.
     */
    void putArray(boolean lastRequest, int offset, int count);
    /**
     * get from the remote array.
     * @param lastRequest Is this the last request.
     * @param offset The offset in the remote array, i.e. the PVArray returned by ChannelArrayRequester.channelArrayConnect.
     * @param count The number of elements to get. 0 means "till the end of array".
     */
    void getArray(boolean lastRequest, int offset, int count);
    /**
     * Set the length and/or the capacity.
     * @param lastRequest Is this the last request.
     * @param length The new length.
     * @param capacity The new capacity. 0 means do not change.
     */
    void setLength(boolean lastRequest, int length, int capacity);
}
