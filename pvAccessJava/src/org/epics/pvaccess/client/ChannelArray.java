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
     * @param offset The offset in the remote array, i.e. the PVArray returned by ChannelArrayRequester.channelArrayConnect.
     * @param count The number of elements to put.
     * @param stride 1 means all the elements from offset to count, 2 means every other, 3 means every third, etc.
     * @param lastRequest Is this the last request.
     */
    void putArray(int offset, int count, int stride, boolean lastRequest);
    /**
     * get from the remote array.
     * @param offset The offset in the remote array, i.e. the PVArray returned by ChannelArrayRequester.channelArrayConnect.
     * @param count The number of elements to get. 0 means "till the end of array".
     * @param stride 1 means all the elements from offset to count, 2 means every other, 3 means every third, etc.
     * @param lastRequest Is this the last request.
     */
    void getArray(int offset, int count, int stride, boolean lastRequest);
    
    /**
     * Get the length and the capacity.
     * @param lastRequest Is this the last request.
     */
    void getLength(boolean lastRequest);
    
    /**
     * Set the length and/or the capacity.
     * @param length The new length.
     * @param capacity The new capacity. 0 means do not change.
     * @param lastRequest Is this the last request.
     */
    void setLength(int length, int capacity, boolean lastRequest);
}
